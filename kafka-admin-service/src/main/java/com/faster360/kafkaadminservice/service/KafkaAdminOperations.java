package com.faster360.kafkaadminservice.service;

import com.faster360.kafkaadminservice.config.TopicsProperties;
import com.faster360.kafkaadminservice.dto.CreateTopicRequest;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.GroupNotEmptyException;
import org.apache.kafka.common.errors.GroupIdNotFoundException;
import org.apache.kafka.common.errors.InvalidConfigurationException;
import org.apache.kafka.common.errors.InvalidPartitionsException;
import org.apache.kafka.common.errors.InvalidReplicationFactorException;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.faster360.kafkaadminservice.exception.TopicOperationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Capa de gestión de la plataforma Kafka: tópicos, configuraciones y consumer groups,
 * implementada sobre el cliente {@link Admin}.
 */
@Service
public class KafkaAdminOperations {

    private static final Logger log = LoggerFactory.getLogger(KafkaAdminOperations.class);
    private static final long TIMEOUT_SECONDS = 15;

    /** Únicas configuraciones de tópico que la API permite crear o modificar. */
    private static final Set<String> ALLOWED_CONFIGS = Set.of(
            "retention.ms", "retention.bytes", "cleanup.policy", "min.insync.replicas",
            "max.message.bytes", "segment.ms", "compression.type");

    private final Admin admin;
    /** En tópicos del sistema estas configuraciones no se tocan (afectan durabilidad y compactación). */
    private static final Set<String> LOCKED_ON_SYSTEM_TOPICS = Set.of("cleanup.policy", "min.insync.replicas");

    private final Set<String> protectedTopics;
    private final Set<String> protectedGroups;

    public KafkaAdminOperations(@org.springframework.context.annotation.Lazy Admin admin,
                                TopicsProperties topics,
                                @org.springframework.beans.factory.annotation.Value("${faster360.kafka.protected-groups}") List<String> protectedGroups) {
        this.admin = admin;
        this.protectedTopics = Set.of(topics.events().name(), topics.analytics().name(), topics.dlt().name());
        this.protectedGroups = Set.copyOf(protectedGroups);
    }

    private void validateConfigKeys(Map<String, String> configs) {
        if (configs == null) {
            return;
        }
        for (String key : configs.keySet()) {
            if (!ALLOWED_CONFIGS.contains(key)) {
                throw new TopicOperationException(HttpStatus.BAD_REQUEST,
                        "Configuración no permitida: '" + key + "'. Permitidas: " + ALLOWED_CONFIGS, null);
            }
        }
    }

    // ------------------------------------------------------------------ TÓPICOS

    public List<String> listTopics() {
        return new ArrayList<>(new TreeMap<>(await(admin.listTopics().namesToListings())).keySet());
    }

    public Map<String, Object> describeTopic(String name) {
        TopicDescription desc = await(admin.describeTopics(List.of(name)).allTopicNames()).get(name);
        Map<String, String> configs = getConfigs(name);

        List<Map<String, Object>> partitions = desc.partitions().stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("partition", p.partition());
            m.put("leader", p.leader() == null ? null : p.leader().id());
            m.put("replicas", p.replicas().stream().map(n -> n.id()).toList());
            m.put("isr", p.isr().stream().map(n -> n.id()).toList());
            return m;
        }).toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", desc.name());
        result.put("internal", desc.isInternal());
        result.put("partitionCount", desc.partitions().size());
        result.put("replicationFactor", desc.partitions().isEmpty() ? 0 : desc.partitions().get(0).replicas().size());
        result.put("partitions", partitions);
        result.put("configs", configs);
        return result;
    }

    public void createTopic(CreateTopicRequest req) {
        validateConfigKeys(req.configs());
        NewTopic topic = new NewTopic(req.name(), req.partitions(), req.replicationFactor());
        if (req.configs() != null && !req.configs().isEmpty()) {
            topic.configs(req.configs());
        }
        await(admin.createTopics(List.of(topic)).all());
        log.info("Tópico '{}' creado ({} particiones, RF={}, configs={})",
                req.name(), req.partitions(), req.replicationFactor(), req.configs());
    }

    public void deleteTopic(String name) {
        if (protectedTopics.contains(name)) {
            throw new TopicOperationException(HttpStatus.FORBIDDEN,
                    "El tópico '" + name + "' es parte del sistema y no puede eliminarse", null);
        }
        await(admin.deleteTopics(List.of(name)).all());
        log.info("Tópico '{}' eliminado", name);
    }

    /** Modifica configuraciones del tópico (retención, cleanup.policy, ...) sin reemplazar las demás. */
    public Map<String, String> updateTopicConfig(String name, Map<String, String> changes) {
        validateConfigKeys(changes);
        if (protectedTopics.contains(name)) {
            for (String key : changes.keySet()) {
                if (LOCKED_ON_SYSTEM_TOPICS.contains(key)) {
                    throw new TopicOperationException(HttpStatus.FORBIDDEN,
                            "'" + key + "' no puede modificarse en el tópico del sistema '" + name + "'", null);
                }
            }
        }
        ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, name);
        Collection<AlterConfigOp> ops = changes.entrySet().stream()
                .map(e -> new AlterConfigOp(new ConfigEntry(e.getKey(), e.getValue()), AlterConfigOp.OpType.SET))
                .toList();
        await(admin.incrementalAlterConfigs(Map.of(resource, ops)).all());
        log.info("Configuración del tópico '{}' actualizada: {}", name, changes);
        return getConfigs(name);
    }

    /** Aumenta el número de particiones (Kafka no permite reducirlas). */
    public void increasePartitions(String name, int totalPartitions) {
        if (protectedTopics.contains(name)) {
            // Cambiar el nº de particiones rompe el orden por clave de los eventos de una misma orden
            throw new TopicOperationException(HttpStatus.FORBIDDEN,
                    "No se pueden ampliar las particiones del tópico del sistema '" + name + "'", null);
        }
        await(admin.createPartitions(Map.of(name, NewPartitions.increaseTo(totalPartitions))).all());
        log.info("Tópico '{}' ampliado a {} particiones", name, totalPartitions);
    }

    private Map<String, String> getConfigs(String topic) {
        ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topic);
        var config = await(admin.describeConfigs(List.of(resource)).all()).get(resource);
        Map<String, String> result = new TreeMap<>();
        config.entries().stream()
                .filter(e -> !e.isDefault() || isKey(e.name()))
                .forEach(e -> result.put(e.name(), e.value()));
        return result;
    }

    private boolean isKey(String name) {
        return name.equals("retention.ms") || name.equals("cleanup.policy")
                || name.equals("min.insync.replicas") || name.equals("max.message.bytes");
    }

    // ----------------------------------------------------------- CONSUMER GROUPS

    public List<Map<String, Object>> listConsumerGroups() {
        Collection<ConsumerGroupListing> groups = await(admin.listConsumerGroups().all());
        return groups.stream().map(g -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("groupId", g.groupId());
            m.put("state", g.state().map(ConsumerGroupState::toString).orElse("UNKNOWN"));
            return m;
        }).toList();
    }

    /** Detalle del grupo: miembros, estado y lag (offset final - offset confirmado) por partición. */
    public Map<String, Object> describeConsumerGroup(String groupId) {
        ConsumerGroupDescription desc = await(admin.describeConsumerGroups(List.of(groupId)).all()).get(groupId);
        if (desc.state() == ConsumerGroupState.DEAD) {
            throw new TopicOperationException(HttpStatus.NOT_FOUND, "Consumer group no encontrado: " + groupId, null);
        }

        Map<TopicPartition, OffsetAndMetadata> committed =
                await(admin.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata());
        Map<TopicPartition, OffsetSpec> latestSpec = new LinkedHashMap<>();
        committed.keySet().forEach(tp -> latestSpec.put(tp, OffsetSpec.latest()));
        var latest = latestSpec.isEmpty() ? Map.<TopicPartition, org.apache.kafka.clients.admin.ListOffsetsResult.ListOffsetsResultInfo>of()
                : await(admin.listOffsets(latestSpec).all());

        List<Map<String, Object>> offsets = new ArrayList<>();
        long totalLag = 0;
        for (var entry : committed.entrySet()) {
            long current = entry.getValue().offset();
            long end = Optional.ofNullable(latest.get(entry.getKey())).map(i -> i.offset()).orElse(current);
            long lag = Math.max(0, end - current);
            totalLag += lag;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("topic", entry.getKey().topic());
            m.put("partition", entry.getKey().partition());
            m.put("currentOffset", current);
            m.put("endOffset", end);
            m.put("lag", lag);
            offsets.add(m);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("groupId", desc.groupId());
        result.put("state", desc.state().toString());
        result.put("partitionAssignor", desc.partitionAssignor());
        result.put("members", desc.members().stream().map(mem -> Map.of(
                "memberId", mem.consumerId(),
                "clientId", mem.clientId(),
                "host", mem.host(),
                "assignedPartitions", mem.assignment().topicPartitions().stream().map(TopicPartition::toString).toList()
        )).toList());
        result.put("totalLag", totalLag);
        result.put("offsets", offsets);
        return result;
    }

    public void deleteConsumerGroup(String groupId) {
        if (protectedGroups.contains(groupId)) {
            throw new TopicOperationException(HttpStatus.FORBIDDEN,
                    "El consumer group '" + groupId + "' es parte del sistema y no puede eliminarse", null);
        }
        await(admin.deleteConsumerGroups(List.of(groupId)).all());
        log.info("Consumer group '{}' eliminado", groupId);
    }

    // ------------------------------------------------------------------ UTIL

    /** Espera el resultado traduciendo errores de Kafka a respuestas HTTP coherentes. */
    private <T> T await(KafkaFuture<T> future) {
        try {
            return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TopicOperationException(HttpStatus.SERVICE_UNAVAILABLE, "Operación interrumpida", e);
        } catch (TimeoutException e) {
            throw new TopicOperationException(HttpStatus.GATEWAY_TIMEOUT,
                    "Kafka no respondió en " + TIMEOUT_SECONDS + "s. ¿Brokers levantados?", e);
        } catch (ExecutionException e) {
            Throwable c = e.getCause();
            if (c instanceof TopicExistsException) {
                throw new TopicOperationException(HttpStatus.CONFLICT, "El tópico ya existe", c);
            }
            if (c instanceof UnknownTopicOrPartitionException) {
                throw new TopicOperationException(HttpStatus.NOT_FOUND, "El tópico no existe", c);
            }
            if (c instanceof GroupIdNotFoundException) {
                throw new TopicOperationException(HttpStatus.NOT_FOUND, "El consumer group no existe", c);
            }
            if (c instanceof GroupNotEmptyException) {
                throw new TopicOperationException(HttpStatus.CONFLICT,
                        "El consumer group tiene miembros activos; deténgalos antes de eliminarlo", c);
            }
            if (c instanceof InvalidPartitionsException || c instanceof InvalidReplicationFactorException
                    || c instanceof InvalidConfigurationException) {
                throw new TopicOperationException(HttpStatus.BAD_REQUEST, c.getMessage(), c);
            }
            throw new TopicOperationException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error de Kafka: " + (c != null ? c.getMessage() : e.getMessage()), e);
        }
    }
}

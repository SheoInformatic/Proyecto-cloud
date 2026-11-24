package com.faster360.kafkaadminservice.controller;

import com.faster360.kafkaadminservice.dto.CreateTopicRequest;
import com.faster360.kafkaadminservice.dto.UpdatePartitionsRequest;
import com.faster360.kafkaadminservice.dto.UpdateTopicConfigRequest;
import com.faster360.kafkaadminservice.service.KafkaAdminOperations;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kafka")
@CrossOrigin(origins = "${faster360.admin.allowed-origin:http://localhost:4200}")
public class KafkaAdminController {

    private final KafkaAdminOperations ops;

    public KafkaAdminController(KafkaAdminOperations ops) {
        this.ops = ops;
    }

    // 1. TÓPICOS
    @GetMapping("/topics")
    public List<String> listTopics() {
        return ops.listTopics();
    }

    @GetMapping("/topics/{name}")
    public Map<String, Object> describeTopic(@PathVariable String name) {
        return ops.describeTopic(name);
    }

    @PostMapping("/topics")
    public ResponseEntity<Map<String, String>> createTopic(@Valid @RequestBody CreateTopicRequest request) {
        ops.createTopic(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Tópico creado exitosamente", "topic", request.name()));
    }

    @DeleteMapping("/topics/{name}")
    public Map<String, String> deleteTopic(@PathVariable String name) {
        ops.deleteTopic(name);
        return Map.of("message", "Tópico eliminado", "topic", name);
    }

    // 2. CONFIGURACIONES Y PARTICIONES
    @PutMapping("/topics/{name}/config")
    public Map<String, Object> updateConfig(@PathVariable String name,
                                            @Valid @RequestBody UpdateTopicConfigRequest request) {
        return Map.of("topic", name, "configs", ops.updateTopicConfig(name, request.configs()));
    }

    @PutMapping("/topics/{name}/partitions")
    public Map<String, Object> increasePartitions(@PathVariable String name,
                                                  @Valid @RequestBody UpdatePartitionsRequest request) {
        ops.increasePartitions(name, request.totalPartitions());
        return Map.of("message", "Particiones actualizadas", "topic", name, "totalPartitions", request.totalPartitions());
    }

    // 3. CONSUMER GROUPS
    @GetMapping("/consumer-groups")
    public List<Map<String, Object>> listGroups() {
        return ops.listConsumerGroups();
    }

    @GetMapping("/consumer-groups/{groupId}")
    public Map<String, Object> describeGroup(@PathVariable String groupId) {
        return ops.describeConsumerGroup(groupId);
    }

    @DeleteMapping("/consumer-groups/{groupId}")
    public Map<String, String> deleteGroup(@PathVariable String groupId) {
        ops.deleteConsumerGroup(groupId);
        return Map.of("message", "Consumer group eliminado", "groupId", groupId);
    }
}

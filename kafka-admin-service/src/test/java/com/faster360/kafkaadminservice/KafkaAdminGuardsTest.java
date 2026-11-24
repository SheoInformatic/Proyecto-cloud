package com.faster360.kafkaadminservice;

import com.faster360.kafkaadminservice.config.TopicsProperties;
import com.faster360.kafkaadminservice.dto.CreateTopicRequest;
import com.faster360.kafkaadminservice.exception.TopicOperationException;
import com.faster360.kafkaadminservice.service.KafkaAdminOperations;
import org.apache.kafka.clients.admin.Admin;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KafkaAdminGuardsTest {

    private static TopicsProperties.Topic topic(String name) {
        return new TopicsProperties.Topic(name, 3, (short) 3, 1000L, "delete", 2);
    }

    private final KafkaAdminOperations ops = new KafkaAdminOperations(
            Mockito.mock(Admin.class),
            new TopicsProperties(topic("sys.events"), topic("sys.analytics"), topic("sys.dlt")),
            java.util.List.of("sys-group"));

    @Test
    void systemTopicCriticalConfigsAndPartitionsAreLocked() {
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(TopicOperationException.class,
                () -> ops.updateTopicConfig("sys.analytics", Map.of("cleanup.policy", "delete"))).getStatus());
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(TopicOperationException.class,
                () -> ops.updateTopicConfig("sys.events", Map.of("min.insync.replicas", "1"))).getStatus());
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(TopicOperationException.class,
                () -> ops.increasePartitions("sys.events", 10)).getStatus());
    }

    @Test
    void systemConsumerGroupsCannotBeDeleted() {
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(TopicOperationException.class,
                () -> ops.deleteConsumerGroup("sys-group")).getStatus());
    }

    @Test
    void systemTopicsCannotBeDeleted() {
        var ex = assertThrows(TopicOperationException.class, () -> ops.deleteTopic("sys.events"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void disallowedConfigKeysAreRejected() {
        var ex = assertThrows(TopicOperationException.class,
                () -> ops.updateTopicConfig("t", Map.of("unclean.leader.election.enable", "true")));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());

        var ex2 = assertThrows(TopicOperationException.class,
                () -> ops.createTopic(new CreateTopicRequest("t", 1, (short) 1, Map.of("foo", "bar"))));
        assertEquals(HttpStatus.BAD_REQUEST, ex2.getStatus());
    }
}

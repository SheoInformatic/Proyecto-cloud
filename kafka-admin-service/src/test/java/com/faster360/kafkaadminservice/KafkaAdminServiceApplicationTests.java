package com.faster360.kafkaadminservice;

import com.faster360.kafkaadminservice.config.TopicsProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class KafkaAdminServiceApplicationTests {

	@Autowired
	private TopicsProperties topics;

	@Test
	void contextLoadsAndTopicsAreConfigured() {
		assertEquals("faster360.orders.events", topics.events().name());
		assertEquals("faster360.orders.analytics", topics.analytics().name());
		assertTrue(topics.events().partitions() >= 1);
		assertEquals("compact,delete", topics.analytics().cleanupPolicy());
	}
}

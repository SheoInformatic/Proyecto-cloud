package com.faster360.notificationticketservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Los listeners Kafka/RabbitMQ no se inician en el test (no hay brokers disponibles)
@SpringBootTest(properties = {
		"faster360.kafka.consumer.auto-startup=false",
		"spring.rabbitmq.listener.simple.auto-startup=false"
})
class NotificationTicketServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}

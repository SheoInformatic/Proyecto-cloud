package com.faster360.kafkaadminservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.faster360.kafkaadminservice.config.TopicsProperties;

@SpringBootApplication
@EnableConfigurationProperties(TopicsProperties.class)
public class KafkaAdminServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaAdminServiceApplication.class, args);
	}

}

package com.faster360.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@SpringBootTest
class OrderserviceApplicationTests {

	@org.springframework.beans.factory.annotation.Autowired
	private com.faster360.orderservice.service.OrderService orderService;

	@MockitoBean
	private RabbitTemplate rabbitTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void testCreateOrder() {
		com.faster360.orderservice.dto.CreateOrderRequest req = new com.faster360.orderservice.dto.CreateOrderRequest();
		req.setCustomerName("Sergio Velásquez");
		req.setCustomerEmail("sergiomarcelo3672@gmail.com");
		req.setTotalAmount(new java.math.BigDecimal("29990"));
		com.faster360.orderservice.domain.Order order = orderService.createOrder(req);
		org.junit.jupiter.api.Assertions.assertNotNull(order.getId());
	}
}


package com.faster360.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OrderserviceApplicationTests {

	@org.springframework.beans.factory.annotation.Autowired
	private com.faster360.orderservice.service.OrderService orderService;

	@Test
	void contextLoads() {
	}

	@Test
	void testCreateOrder() {
		com.faster360.orderservice.dto.CreateOrderRequest req = new com.faster360.orderservice.dto.CreateOrderRequest();
		req.setCustomerName("Sergio Velásquez");
		req.setCustomerEmail("sergio.velasquez@faster360.com");
		req.setTotalAmount(new java.math.BigDecimal("29990"));
		com.faster360.orderservice.domain.Order order = orderService.createOrder(req);
		org.junit.jupiter.api.Assertions.assertNotNull(order.getId());
	}
}


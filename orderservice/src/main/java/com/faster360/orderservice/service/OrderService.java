package com.faster360.orderservice.service;

import com.faster360.orderservice.config.RabbitMQProperties;
import com.faster360.orderservice.domain.Order;
import com.faster360.orderservice.domain.OrderStatus;
import com.faster360.orderservice.dto.CreateOrderRequest;
import com.faster360.orderservice.dto.OrderCreatedEvent;
import com.faster360.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties rabbitMQProperties;

    public OrderService(OrderRepository orderRepository,
                        RabbitTemplate rabbitTemplate,
                        RabbitMQProperties rabbitMQProperties) {
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitMQProperties = rabbitMQProperties;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        String generatedOrderNumber = UUID.randomUUID().toString();

        // 1. Persistencia síncrona en la base de datos (Cloud SQL)
        Order order = new Order(
                generatedOrderNumber,
                request.getCustomerName(),
                request.getCustomerEmail(),
                request.getTotalAmount(),
                OrderStatus.CREATED
        );
        Order savedOrder = orderRepository.save(order);
        log.info("Orden guardada exitosamente en BD con ID: {} y N°: {}", savedOrder.getId(), savedOrder.getOrderNumber());

        // 2. Preparación y publicación asíncrona hacia RabbitMQ
        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getOrderNumber(),
                savedOrder.getCustomerName(),
                savedOrder.getCustomerEmail(),
                savedOrder.getTotalAmount()
        );

        rabbitTemplate.convertAndSend(
                rabbitMQProperties.getExchange(),
                rabbitMQProperties.getRoutingKeys().getOrderCreated(),
                event
        );

        log.info("Evento OrderCreated publicado al exchange '{}' con routing key '{}'",
                rabbitMQProperties.getExchange(),
                rabbitMQProperties.getRoutingKeys().getOrderCreated());

        return savedOrder;
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
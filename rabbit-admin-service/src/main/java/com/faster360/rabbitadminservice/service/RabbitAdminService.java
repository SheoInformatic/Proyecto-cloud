package com.faster360.rabbitadminservice.service;

import com.faster360.rabbitadminservice.dto.CreateBindingRequest;
import com.faster360.rabbitadminservice.dto.CreateExchangeRequest;
import com.faster360.rabbitadminservice.dto.CreateQueueRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);

    private final RabbitAdmin rabbitAdmin;

    public RabbitAdminService(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    // --- GESTIÓN DE COLAS ---
    public String createQueue(CreateQueueRequest request) {
        Queue queue = new Queue(request.getName(), request.isDurable(), request.isExclusive(), request.isAutoDelete());
        String declaredQueue = rabbitAdmin.declareQueue(queue);
        log.info("Cola creada exitosamente: {}", declaredQueue);
        return declaredQueue;
    }

    public boolean deleteQueue(String queueName) {
        boolean deleted = rabbitAdmin.deleteQueue(queueName);
        log.info("Eliminación de la cola '{}': {}", queueName, deleted ? "Exitosa" : "No encontrada");
        return deleted;
    }

    // --- GESTIÓN DE EXCHANGES ---
    public void createExchange(CreateExchangeRequest request) {
        Exchange exchange;
        switch (request.getType().toLowerCase()) {
            case "topic":
                exchange = new TopicExchange(request.getName(), request.isDurable(), request.isAutoDelete());
                break;
            case "fanout":
                exchange = new FanoutExchange(request.getName(), request.isDurable(), request.isAutoDelete());
                break;
            case "headers":
                exchange = new HeadersExchange(request.getName(), request.isDurable(), request.isAutoDelete());
                break;
            case "direct":
            default:
                exchange = new DirectExchange(request.getName(), request.isDurable(), request.isAutoDelete());
                break;
        }
        rabbitAdmin.declareExchange(exchange);
        log.info("Exchange creado exitosamente: {} de tipo {}", request.getName(), request.getType());
    }

    public boolean deleteExchange(String exchangeName) {
        boolean deleted = rabbitAdmin.deleteExchange(exchangeName);
        log.info("Eliminación del exchange '{}': {}", exchangeName, deleted ? "Exitoso" : "No encontrado");
        return deleted;
    }

    // --- GESTIÓN DE BINDINGS ---
    public void createBinding(CreateBindingRequest request) {
        Binding binding = new Binding(
                request.getQueueName(),
                Binding.DestinationType.QUEUE,
                request.getExchangeName(),
                request.getRoutingKey(),
                null
        );
        rabbitAdmin.declareBinding(binding);
        log.info("Binding creado: Cola '{}' enlazada a Exchange '{}' con key '{}'",
                request.getQueueName(), request.getExchangeName(), request.getRoutingKey());
    }

    public void removeBinding(CreateBindingRequest request) {
        Binding binding = new Binding(
                request.getQueueName(),
                Binding.DestinationType.QUEUE,
                request.getExchangeName(),
                request.getRoutingKey(),
                null
        );
        rabbitAdmin.removeBinding(binding);
        log.info("Binding eliminado: Cola '{}' desenlazada de Exchange '{}'",
                request.getQueueName(), request.getExchangeName());
    }
}
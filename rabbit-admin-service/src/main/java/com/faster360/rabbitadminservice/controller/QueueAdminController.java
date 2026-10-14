package com.faster360.rabbitadminservice.controller;

import com.faster360.rabbitadminservice.dto.CreateBindingRequest;
import com.faster360.rabbitadminservice.dto.CreateExchangeRequest;
import com.faster360.rabbitadminservice.dto.CreateQueueRequest;
import com.faster360.rabbitadminservice.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class QueueAdminController {

    private final RabbitAdminService adminService;

    public QueueAdminController(RabbitAdminService adminService) {
        this.adminService = adminService;
    }

    // 1. ENDPOINTS DE COLAS (POST /queues, DELETE /queues/{name})
    @PostMapping("/queues")
    public ResponseEntity<Map<String, String>> createQueue(@Valid @RequestBody CreateQueueRequest request) {
        String created = adminService.createQueue(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Cola creada exitosamente", "queue", created));
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Map<String, Object>> deleteQueue(@PathVariable String name) {
        boolean deleted = adminService.deleteQueue(name);
        return ResponseEntity.ok(Map.of("message", "Operación ejecutada", "deleted", deleted));
    }

    // 2. ENDPOINTS DE EXCHANGES (POST /exchanges, DELETE /exchanges/{name})
    @PostMapping("/exchanges")
    public ResponseEntity<Map<String, String>> createExchange(@Valid @RequestBody CreateExchangeRequest request) {
        adminService.createExchange(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Exchange creado exitosamente", "exchange", request.getName()));
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Map<String, Object>> deleteExchange(@PathVariable String name) {
        boolean deleted = adminService.deleteExchange(name);
        return ResponseEntity.ok(Map.of("message", "Operación ejecutada", "deleted", deleted));
    }

    // 3. ENDPOINTS DE BINDINGS (POST /bindings, DELETE /bindings)
    @PostMapping("/bindings")
    public ResponseEntity<Map<String, String>> createBinding(@Valid @RequestBody CreateBindingRequest request) {
        adminService.createBinding(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Binding creado exitosamente"));
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Map<String, String>> removeBinding(@Valid @RequestBody CreateBindingRequest request) {
        adminService.removeBinding(request);
        return ResponseEntity.ok(Map.of("message", "Binding eliminado exitosamente"));
    }
}
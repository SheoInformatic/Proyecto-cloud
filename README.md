# Faster 360° — Arquitectura Cloud & Mensajería Asíncrona (EP3)

Plataforma e-commerce desacoplada basada en microservicios, mensajería asíncrona empresarial con **RabbitMQ**, persistencia en **PostgreSQL (Cloud SQL)** y autenticación **IDaaS Multi-Tenant** mediante **Google Cloud Platform (Identity Platform & Google OAuth 2.0)**.

---

## 🏛️ Arquitectura del Sistema

```mermaid
graph TD
    subgraph Frontend ["Capa de Presentación"]
        A[Angular 19 SPA :4200]
    end

    subgraph Security ["IDaaS & Autenticación"]
        GCP[Google Cloud Identity Platform / accounts.google.com]
        JWT[Spring Security JWT Validator]
    end

    subgraph Core ["Microservicios Backend (Spring Boot 3 / Java 17)"]
        OS[Order-Service :8081]
        NS[Notification & Ticket Service :8082]
        RA[Rabbit-Admin Service :8083]
    end

    subgraph Broker ["Broker de Mensajería (AMQP)"]
        EX["faster360.orders.exchange (Topic)"]
        QN["faster360.orders.notification.queue"]
        QT["faster360.orders.ticket.queue"]
        DLX["faster360.orders.dlx (Direct)"]
        QDLQ["faster360.orders.dlq (Dead Letter Queue)"]
    end

    subgraph Storage ["Persistencia"]
        DB[(PostgreSQL Cloud SQL :5432)]
    end

    A -->|1. OAuth 2.0| GCP
    GCP -->|2. JWT ID Token| A
    A -->|3. POST /api/orders + Bearer Token| OS
    OS -->|4. Validar Firma & Claims| JWT
    OS -->|5. Guardar Orden| DB
    OS -->|6. Publicar order.created| EX
    EX -->|Enrutamiento Pub-Sub| QN
    EX -->|Enrutamiento Pub-Sub| QT
    QN -->|Consumo con ACK manual| NS
    QT -->|Consumo con ACK manual| NS
    QN -.->|Fallo / basicNack| DLX
    DLX --> QDLQ
```

---

## 🔌 Puertos y Servicios

| Componente | Tecnología | Puerto | Acceso Local |
| :--- | :--- | :--- | :--- |
| **Frontend Web** | Angular 19 / TypeScript | `4200` | [http://localhost:4200](http://localhost:4200) |
| **Order Service** | Spring Boot / Java 17 | `8081` | [http://localhost:8081/api/orders](http://localhost:8081/api/orders) |
| **Notification & Ticket** | Spring Boot / Java 17 | `8082` | [http://localhost:8082](http://localhost:8082) |
| **Rabbit Admin Service** | Spring Boot / Java 17 | `8083` | [http://localhost:8083](http://localhost:8083) |
| **RabbitMQ Consola Web** | RabbitMQ 3 Management | `15672` | [http://localhost:15672](http://localhost:15672) (`guest`/`guest`) |
| **RabbitMQ Protocolo AMQP** | Protocolo AMQP 0-9-1 | `5672` | `amqp://guest:guest@localhost:5672` |
| **Base de Datos** | PostgreSQL 15 Alpine | `5432` | `jdbc:postgresql://localhost:5432/faster360_db` |

---

## 🚀 Cómo Ejecutar el Proyecto

### Opción 1: Inicio Automatizado (Recomendado para Windows)
El repositorio incluye un script lanzador que inicia Docker y todos los microservicios en ventanas de terminal independientes:

1. **Asegúrate de tener Docker Desktop abierto.**
2. Haz doble clic en el archivo:
   ```cmd
   iniciar-todo.bat
   ```
3. El script ejecutará automáticamente:
   * `docker compose up -d` (Levanta RabbitMQ y PostgreSQL).
   * Inicia `orderservice` en el puerto `8081`.
   * Inicia `notification-ticket-service` en el puerto `8082`.
   * Inicia `rabbit-admin-service` en el puerto `8083`.
   * Inicia `faster360-frontend` en el puerto `4200`.
4. Abre tu navegador en **[http://localhost:4200](http://localhost:4200)**.

> **Para detener todos los servicios limpiamente:**  
> Ejecuta con doble clic `detener-todo.bat`.

---

### Opción 2: Inicio Manual (Paso a Paso)

#### 1. Iniciar Infraestructura Docker (RabbitMQ y Base de Datos)
```bash
docker compose up -d
```

#### 2. Iniciar Microservicio de Órdenes (Productor)
```bash
cd orderservice
./mvnw spring-boot:run
```

#### 3. Iniciar Microservicio de Notificaciones y Tickets (Consumidor)
```bash
cd notification-ticket-service
./mvnw spring-boot:run
```

#### 4. Iniciar Frontend Angular
```bash
cd faster360-frontend
npm install
npm start
```

---

## 👥 Cuentas de Evaluación (IDaaS GCP Multi-Tenant)

El sistema cuenta con un allowlist estricto en frontend y backend ([`JwtTokenValidator.java`](orderservice/src/main/java/com/faster360/orderservice/security/JwtTokenValidator.java)) que solo admite los siguientes inquilinos y dominios oficiales:

| Usuario | Correo Electrónico | Inquilino GCP (Tenant ID) | Rol | Contraseña Demo |
| :--- | :--- | :--- | :--- | :--- |
| **Sergio Velásquez** | `sergiomarcelo3672@gmail.com` | `SergioVelasquez-dnfj9` | Cliente / Comprador | `Password123!` |
| **Omar Nieto** | `om.nieto@duocuc.cl` | `OmarNieto-npke9` | Cloud Architect / Admin | `AdminPassword123!` |

---

## 📬 Topología RabbitMQ y Manejo de Errores (DLQ)

* **Exchange:** `faster360.orders.exchange` (Tipo `topic`, Durable).
* **Routing Keys:** 
  * `order.created`: Enrutado hacia `faster360.orders.notification.queue` y `faster360.orders.ticket.queue`.
  * `order.dead-letter`: Enrutado hacia `faster360.orders.dlq`.
* **Confirmación de Entrega:** Se implementa **ACK Manual** (`channel.basicAck`).
* **Prueba de Dead Letter Queue (DLQ):** Si se genera un pedido con el correo `fail@faster360.com`, el consumidor ejecuta `channel.basicNack(deliveryTag, false, requeue = false)` desviando automáticamente el mensaje a la cola `faster360.orders.dlq`.

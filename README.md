# Faster 360° — Arquitectura Cloud, Mensajería Asíncrona y Streaming (EP3 + EP5)

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
| **Kafka Admin Service** | Spring Boot / Java 17 | `8084` | [http://localhost:8084/api/kafka/topics](http://localhost:8084/api/kafka/topics) |
| **Kafka-UI** | provectuslabs/kafka-ui | `8090` | [http://localhost:8090](http://localhost:8090) |
| **Kafka Brokers (x3)** | cp-kafka 7.6 + Zookeeper | `9092/9093/9094` | `localhost:9092,localhost:9093,localhost:9094` |
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

#### 1. Iniciar Infraestructura Docker
```bash
docker compose up -d                                         # PostgreSQL
docker compose -f docker-compose.rabbitmq-cluster.yml up -d  # RabbitMQ (clúster de 3 nodos)
docker compose -f docker-compose.kafka.yml up -d             # Zookeeper + 3 brokers Kafka
docker compose -f docker-compose.kafka-ui.yml up -d          # Kafka-UI (requiere el anterior)
```

#### 1b. Iniciar Kafka Admin (crea los tópicos; debe ir antes que productor y consumidores)
```bash
cd kafka-admin-service
./mvnw spring-boot:run
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

---

## 🌊 Streaming con Apache Kafka (EP5)

Kafka **complementa** a RabbitMQ: el flujo de colas (notificación, ticket y DLQ) no cambia; en paralelo `orderservice` publica los eventos en Kafka para procesamiento por streaming y analítica.

```mermaid
graph LR
    OS[orderservice :8081] -->|order.created| RMQ[(RabbitMQ)]
    OS -->|clave=orderNumber| T1[["faster360.orders.events<br/>3 part · RF3 · delete 7d"]]
    OS -->|clave=orderNumber| T2[["faster360.orders.analytics<br/>6 part · RF3 · compact,delete 30d"]]
    T1 -->|group order-events| NS[notification-ticket-service :8082]
    T2 -->|group analytics| NS
    NS -.->|3 reintentos o payload inválido| DLT[["faster360.orders.events.dlt"]]
    DLT -->|group dlt| NS
    KA[kafka-admin-service :8084] -->|AdminClient| T1
    KA -->|AdminClient| T2
    KA -->|AdminClient| DLT
```

### Infraestructura (docker-compose)
| Archivo | Contenido |
| :--- | :--- |
| `docker-compose.rabbitmq-cluster.yml` | Clúster RabbitMQ de 3 nodos (`rabbit1..3`, consolas 15672-15674) |
| `docker-compose.kafka.yml` | Zookeeper + 3 brokers (RF=3, `min.insync.replicas=2`, auto-creación de tópicos desactivada) |
| `docker-compose.kafka-ui.yml` | Kafka-UI en http://localhost:8090 para administrar brokers, tópicos y grupos |

### Tópicos (declarados en `kafka-admin-service/src/main/resources/application.yaml`)
| Tópico | Particiones | Réplicas | Retención | cleanup.policy | Uso |
| :--- | :-: | :-: | :-: | :--- | :--- |
| `faster360.orders.events` | 3 | 3 | 7 días | `delete` | Evento operacional por orden |
| `faster360.orders.analytics` | 6 | 3 | 30 días | `compact,delete` | Datos analíticos por orden |
| `faster360.orders.events.dlt` | 3 | 3 | 14 días | `delete` | Mensajes que agotaron los reintentos |

### Productores y consumidores
* **Productor** (`orderservice`): `OrderKafkaPublisher` + `KafkaProducerConfig`. DTOs `OrderCreatedEvent` y `OrderAnalyticsEvent`, serializer JSON propio, `acks=all`, idempotencia, reintentos y compresión `lz4`. Un fallo de Kafka se registra en logs pero no revierte la orden ni el flujo RabbitMQ.
* **Consumidores** (`notification-ticket-service`): `@KafkaListener` en `consumer/kafka/`, que solo adaptan el mensaje y delegan en `OrderEventProcessor` y `OrderAnalyticsAggregator` (lógica de dominio sin mensajería). Resumen analítico en `GET :8082/api/analytics/summary`.
* **Consumer groups:** `faster360-order-events-group`, `faster360-analytics-group` y `faster360-dlt-group`. Concurrencia 3 por listener: las particiones se reparten entre hilos y, al levantar más instancias del servicio, se redistribuyen con `CooperativeStickyAssignor`.

### Manejo de errores
`DefaultErrorHandler` con backoff exponencial (3 reintentos: 1s, 2s, 4s) y `DeadLetterPublishingRecoverer` hacia el DLT. Los payloads inválidos (`InvalidEventException`) y el JSON malformado (`ErrorHandlingDeserializer`) **no se reintentan**: van directo al DLT. `DeadLetterKafkaListener` los audita en logs con la causa original.
* Prueba de reintentos y DLT: crear una orden con el correo `fail@faster360.com` (falla transitoria simulada).

### Kafka Admin API (`kafka-admin-service`, puerto 8084)
| Método | Ruta | Acción |
| :--- | :--- | :--- |
| `GET` | `/api/kafka/topics` · `/api/kafka/topics/{name}` | Listar / detallar (particiones, líderes, ISR, configs) |
| `POST` | `/api/kafka/topics` | Crear tópico (`name`, `partitions`, `replicationFactor`, `configs`) |
| `PUT` | `/api/kafka/topics/{name}/config` | Modificar configuración (`retention.ms`, `cleanup.policy`, ...) |
| `PUT` | `/api/kafka/topics/{name}/partitions` | Aumentar particiones |
| `DELETE` | `/api/kafka/topics/{name}` | Eliminar tópico |
| `GET` | `/api/kafka/consumer-groups` · `/{groupId}` | Listar grupos / detalle con miembros y **lag** por partición |
| `DELETE` | `/api/kafka/consumer-groups/{groupId}` | Eliminar grupo (sin miembros activos) |

**Seguridad:** todas las rutas `/api/**` exigen `Authorization: Bearer <JWT del IDaaS>`. Sin token válido responde `401`; con token válido pero cuenta no administradora, `403`. Los administradores se definen en `faster360.security.admin-emails` (variable `KAFKA_ADMIN_EMAILS`, por defecto `om.nieto@duocuc.cl`). Se exigen los claims `exp`, `iss` y `email`, y se rechaza `alg: none`. Además, los tópicos del sistema no se pueden eliminar y solo se admiten configuraciones de una lista blanca (`retention.ms`, `cleanup.policy`, `min.insync.replicas`, ...). El CORS solo permite `http://localhost:4200`.
> Limitación: igual que en `orderservice`, el JWT se valida por estructura y claims, sin verificar la firma contra las claves públicas (JWKS) de Google.

`rabbit-admin-service` (:8083) usa el mismo esquema: exige JWT de administrador y las cuentas permitidas se definen en `RABBIT_ADMIN_EMAILS` (por defecto `om.nieto@duocuc.cl`).

**Requisitos para ejecutar:** Docker Desktop, JDK 17 con la variable `JAVA_HOME` definida (los scripts usan `mvnw`, que la necesita) y Node.js para el frontend.

Ejemplo:
```bash
curl -X PUT http://localhost:8084/api/kafka/topics/faster360.orders.events/config \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{"configs":{"retention.ms":"86400000"}}'
```

---

## 🔐 Limitaciones de seguridad conocidas

Este proyecto es académico y varias decisiones de EP3 se mantienen tal cual. Se documentan aquí para no darlas por resueltas:

| Limitación | Detalle | Cómo corregirla en producción |
| :--- | :--- | :--- |
| **JWT sin verificar la firma** | `orderservice`, `kafka-admin-service` y `rabbit-admin-service` validan estructura, expiración, emisor, correo y dominio, pero **no** la firma criptográfica contra las claves públicas (JWKS) de Google. Quien conozca el formato puede fabricar un token con el correo de un administrador | Verificar la firma con las claves públicas de Google y fijar `iss` y `aud` exactos |
| **Login demo en el navegador** | Para las cuentas de Sergio y Omar, el frontend compara la contraseña con un texto fijo y arma el token localmente (`loginWithGcpTenant`), sin pasar por GCP | Usar siempre el login real de GCP Identity Platform (`signInWithPassword`) |
| **Tenant no validado en el backend** | El `tenant_id` del token se lee y se registra en el log, pero ningún servicio lo compara con un valor esperado. El multi-tenant de GCP separa usuarios en GCP, pero el backend no lo hace cumplir | Comparar `firebase.tenant` con los tenants autorizados, tras verificar la firma |
| **Contraseñas de demostración visibles** | Las cuentas demo y sus contraseñas están en este README y en el código del frontend, y el repositorio es público | Cambiar o deshabilitar esas cuentas al terminar la evaluación y no versionar credenciales |
| **API key de GCP en el frontend** | Las API keys web de Identity Platform están pensadas para ser públicas, pero deben restringirse en GCP. No se encontró ningún `client_secret` ni llave privada en el repositorio | Restringir la key por dominio y por API en la consola de GCP |

Los servicios de administración sí exigen un JWT de una cuenta administradora (401 sin token, 403 sin rol), pero, por lo anterior, esa protección es de nivel demostrativo.

Here’s a **complete example** of how to create a **Kafka producer and consumer** in **Java 17 with Spring Boot**.

---

## ✅ Step-by-Step: Kafka with Spring Boot

---

### 1. **Add Dependencies (Maven)**

In your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
</dependencies>
```

---

### 2. **Application Properties**

```properties
# Kafka configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=my-consumer-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
```

> Make sure Kafka is running on `localhost:9092`, or adjust accordingly.

---

### 3. **Producer Configuration and Class**

#### Producer Service

```java
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}
```

---

### 4. **Consumer Class**

```java
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumerService {

    @KafkaListener(topics = "my-topic", groupId = "my-consumer-group")
    public void listen(ConsumerRecord<String, String> record) {
        System.out.printf("Consumed message: key=%s, value=%s%n", record.key(), record.value());
    }
}
```

---

### 5. **Example REST Controller to Test Producer**

```java
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/kafka")
public class KafkaController {

    private final KafkaProducerService producerService;

    public KafkaController(KafkaProducerService producerService) {
        this.producerService = producerService;
    }

    @PostMapping("/publish")
    public String publish(@RequestParam String message) {
        producerService.sendMessage("my-topic", message);
        return "Message sent to Kafka";
    }
}
```

---

### ✅ Result

* Send a message:

  ```
  curl -X POST "http://localhost:8080/kafka/publish?message=HelloKafka"
  ```
* See it printed by the consumer.

---

### 🧪 Optional: Docker Compose for Kafka

If you need Kafka locally:

```yaml
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    ports:
      - "9092:9092"
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

---

Let me know if you want to:

* Use JSON payloads
* Send objects instead of plain strings
* Use Avro/Protobuf
* Add unit tests for the producer/consumer

Great! Since `kafka-infra` is a **library module** (not a standalone Spring Boot app), you'll need to configure a **test-only Spring context** with embedded Kafka to run integration tests using **JSON payloads**.

---

## ✅ Integration Test Setup for `kafka-infra` (Library Module)

### 1. **Dependencies (add to `kafka-infra`'s `pom.xml`)**

```xml
<!-- Spring Kafka -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>

<!-- Embedded Kafka for tests -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- Jackson for JSON -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>

<!-- Spring Test -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-test</artifactId>
    <scope>test</scope>
</dependency>
```

---

### 2. **JSON Model (Example DTO)**

```java
public class MyMessage {
    private String id;
    private String content;

    // getters and setters
}
```

---

### 3. **Kafka Configuration Class (inside your lib)**

```java
@Configuration
public class KafkaJsonConfig {

    @Bean
    public ProducerFactory<String, MyMessage> producerFactory() {
        return new DefaultKafkaProducerFactory<>(Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
        ));
    }

    @Bean
    public KafkaTemplate<String, MyMessage> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, MyMessage> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
            ConsumerConfig.GROUP_ID_CONFIG, "test-group",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class,
            JsonDeserializer.TRUSTED_PACKAGES, "*"
        ), new StringDeserializer(), new JsonDeserializer<>(MyMessage.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MyMessage> kafkaListenerContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, MyMessage>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
```

---

### 4. **Integration Test Class**

```java
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = { "test-topic" }, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaInfraIntegrationTest {

    @Autowired
    private KafkaTemplate<String, MyMessage> kafkaTemplate;

    private final List<MyMessage> received = new ArrayList<>();

    @KafkaListener(topics = "test-topic", groupId = "test-group")
    public void listen(MyMessage message) {
        received.add(message);
    }

    @Test
    void testKafkaSendReceive() throws InterruptedException {
        MyMessage msg = new MyMessage();
        msg.setId("abc");
        msg.setContent("hello");

        kafkaTemplate.send("test-topic", msg);

        // Wait for the message to be consumed
        Awaitility.await().atMost(Duration.ofSeconds(10))
                .until(() -> !received.isEmpty());

        Assertions.assertEquals("abc", received.get(0).getId());
        Assertions.assertEquals("hello", received.get(0).getContent());
    }
}
```

> You can use [`awaitility`](https://github.com/awaitility/awaitility) if you want to avoid sleep-based waiting.

---

### 5. **Notes**

* Annotate listener method in the test class itself.
* `@EmbeddedKafka` spins up a broker during test runtime.
* Use JSON serialization via `JsonSerializer`/`JsonDeserializer`.

---

To **auto-register Kafka producers/consumers for any DTO** and use a **reusable `BaseKafkaMessage<T>` structure**, we can follow a clean, extensible architecture that lets your `kafka-infra` library support many event types.

---

## ✅ Step 1: Define a Reusable Base Message Wrapper

```java
package com.example.kafka.model;

public class BaseKafkaMessage<T> {
    private String eventType;
    private T payload;

    public BaseKafkaMessage() {}

    public BaseKafkaMessage(String eventType, T payload) {
        this.eventType = eventType;
        this.payload = payload;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }
}
```

This allows you to send different event types with a consistent envelope.

---

## ✅ Step 2: Kafka Producer – Generic and JSON-Aware

```java
package com.example.kafka.producer;

import com.example.kafka.model.BaseKafkaMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class GenericKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public GenericKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public <T> void send(String topic, BaseKafkaMessage<T> message) {
        kafkaTemplate.send(topic, message);
    }
}
```

---

## ✅ Step 3: Kafka Configuration – Generic Producer & Consumer

### Producer Config

```java
@Bean
public ProducerFactory<String, Object> genericProducerFactory() {
    return new DefaultKafkaProducerFactory<>(Map.of(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
    ));
}

@Bean
public KafkaTemplate<String, Object> kafkaTemplate() {
    return new KafkaTemplate<>(genericProducerFactory());
}
```

### Consumer Config (for BaseKafkaMessage)

```java
@Bean
public ConsumerFactory<String, BaseKafkaMessage<Object>> baseMessageConsumerFactory() {
    JsonDeserializer<BaseKafkaMessage<Object>> deserializer = new JsonDeserializer<>(BaseKafkaMessage.class);
    deserializer.addTrustedPackages("*"); // or specify package

    return new DefaultKafkaConsumerFactory<>(
        Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
            ConsumerConfig.GROUP_ID_CONFIG, "generic-consumer",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class
        ),
        new StringDeserializer(),
        deserializer
    );
}

@Bean(name = "baseMessageListenerFactory")
public ConcurrentKafkaListenerContainerFactory<String, BaseKafkaMessage<Object>> baseMessageListenerFactory() {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, BaseKafkaMessage<Object>>();
    factory.setConsumerFactory(baseMessageConsumerFactory());
    return factory;
}
```

---

## ✅ Step 4: Generic Kafka Consumer

```java
@Service
public class GenericKafkaConsumer {

    @KafkaListener(topics = "my-topic", groupId = "generic-consumer", containerFactory = "baseMessageListenerFactory")
    public void listen(BaseKafkaMessage<Object> message) {
        System.out.println("Received: " + message.getEventType());

        switch (message.getEventType()) {
            case "booking":
                BookingPayload booking = convert(message.getPayload(), BookingPayload.class);
                System.out.println("Booking payload: " + booking.getId());
                break;
            case "payment":
                PaymentPayload payment = convert(message.getPayload(), PaymentPayload.class);
                System.out.println("Payment payload: " + payment.getAmount());
                break;
            default:
                System.out.println("Unknown event: " + message.getEventType());
        }
    }

    private <T> T convert(Object obj, Class<T> targetClass) {
        return new ObjectMapper().convertValue(obj, targetClass);
    }
}
```

---

## ✅ Step 5: Example DTOs

```java
public class BookingPayload {
    private String id;
    private String userId;
    // Getters/setters
}

public class PaymentPayload {
    private String transactionId;
    private double amount;
    // Getters/setters
}
```

---

## 🔁 Usage Example

### Sending a message:

```java
BaseKafkaMessage<BookingPayload> msg = new BaseKafkaMessage<>(
    "booking",
    new BookingPayload("b123", "user999")
);
producer.send("my-topic", msg);
```

---

## ✅ Benefits

* 🧩 Reusable wrapper for all messages
* 🔄 Easily extendable for new DTOs
* 🧪 Suitable for both microservices and library modules
* 🔐 Type-safe deserialization with polymorphic support if needed

---

Would you like me to:

* Add **integration test** using `@EmbeddedKafka`?
* Add **schema registry support** (if using Confluent)?
* Extend to **Spring Cloud Stream** abstraction?

Let me know your direction.


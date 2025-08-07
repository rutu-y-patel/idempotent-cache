
## Idempotent Cache

**A plug-and-play Java/Spring Boot library for idempotent processing using Redis.**  
Designed to make your batch jobs, APIs, and event-driven systems safe, observable, and efficient with built-in deduplication, TTL, logging, and Prometheus/Micrometer metrics.

---

## Key Features

-  **Redis-backed cache** for deduplication
- ️ **Spring Boot auto-configuration** for minimal setup
- ️ **Configurable TTL and key prefix**
-  **Prometheus/Micrometer metrics via AOP**
-  **Seamless integration** with batch jobs, REST APIs, Kafka listeners, etc.
-  **Extensive logging** for cache operations

---

## Project Structure

```
idempotent-cache/       # Core reusable library
    ├─ src/
    ├─ pom.xml
batch/                  # Spring Batch demo application
    ├─ src/
    ├─ pom.xml
README.md
```

---
 Getting Started
---
### 1. Add the Dependency

Include the library in your `pom.xml`:

```xml
<dependency>
    <groupId>com.ms</groupId>
    <artifactId>idempotent-cache</artifactId>
    <version>1.1.9</version>
</dependency>
```

> **Requirements:** Java 17+, Spring Boot 3.2+, Redis

---

### 2. Configure Redis & Idempotency

Add the following to your `application.properties`:

```properties
spring.redis.host=localhost
spring.redis.port=<your-port>

# Optional settings
idempotency.key.ttl=72H
idempotency.key.prefix=idempotent-cache:
```

---

### 3. Required Dependencies

Ensure your application includes these:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

---

### 4. Example Usage

Use the service in your application logic:

```java
import com.ms.idempotentcache.cache.IdempotencyCacheService;
import org.springframework.beans.factory.annotation.Autowired;

@Autowired
private IdempotencyCacheService idempotencyCacheService;

public void processRecord(String contextId, String recordKey) {
    if (!idempotencyCacheService.isProcessed(contextId, recordKey)) {
        // Process the record
        idempotencyCacheService.markProcessed(contextId, recordKey);
    } else {
        // Handle duplicate
    }
}
```

- `contextId`: Logical grouping (e.g., `"employment-events:input.csv"`)
- `recordKey`: Unique identifier (e.g., `"123-45-6789:P0001:ABC:EMPLOYED"`)

---

## Observability & Metrics

Enable Prometheus metrics in `application.properties`:

```properties
management.endpoints.web.exposure.include=prometheus
management.endpoint.prometheus.enabled=true
management.endpoints.web.base-path=/actuator
```

### Available Metrics

| Metric Name                          | Type    | Description                          |
|-------------------------------------|---------|--------------------------------------|
| `idempotency_cache_hit_total`       | Counter | Cache hit count                      |
| `idempotency_cache_miss_total`      | Counter | Cache miss count                     |
| `idempotency_cache_markProcessed_total` | Counter | Records marked as processed          |
| `idempotency_cache_cleared_total`   | Counter | Cache clear operations               |

Includes all standard Spring Boot, JVM, Redis, and Batch metrics.

---

## How It Works

- **Auto-configuration**: Registers beans and AOP aspects automatically
- **IdempotencyCacheService**: Core service for cache operations
- **RedisIdempotencyCacheService**: Default Redis-based implementation
- **IdempotencyMetricsAspect**: Captures metrics via AOP
- **TTL & Prefix**: Customizable for environment-specific tuning

---

## Demo Application

Explore the `batch` module for a working Spring Batch job demo:

- Real file processing
- Per-record deduplication
- Integrated metrics

---

## Troubleshooting

- **Missing metrics?** Check for AOP and Prometheus dependencies
- **Bean not found?** Verify Redis config and library inclusion
- **Classpath issues?** Run `mvn clean install` to rebuild

---

## Contributing

We welcome contributions!  
Feel free to fork, star, open issues, or submit PRs.

---

## License

Licensed under the [MIT License](https://github.com/rutu-y-patel/idempotent-cache/blob/main/LICENSE).

---

## Maintainer

[rutu-y-patel](https://github.com/rutu-y-patel)

---

## Related Search Terms (SEO)

This library is useful if you’re looking for:
- Spring Boot idempotency
- Java idempotent processing
- Deduplication with Redis in Spring
- Prevent duplicate processing Spring Batch
- Idempotent REST API Spring Boot
- Redis deduplication strategy Java
- Idempotency cache library for microservices
- Idempotent Kafka consumer Spring
- Record deduplication Java
- Spring AOP metrics logging
- How to avoid double processing in batch jobs

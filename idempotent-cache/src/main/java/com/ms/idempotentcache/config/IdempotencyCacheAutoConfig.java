package com.ms.idempotentcache.config;

import com.ms.idempotentcache.aop.IdempotencyMetricsAspect;
import com.ms.idempotentcache.cache.IdempotencyCacheService;
import com.ms.idempotentcache.cache.RedisIdempotencyCacheService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@EnableConfigurationProperties(IdempotencyProperties.class)
public class IdempotencyCacheAutoConfig {

    @Bean
    public IdempotencyCacheService idempotencyCacheService(
            StringRedisTemplate redisTemplate,
            IdempotencyProperties properties
    ) {
        return new RedisIdempotencyCacheService(redisTemplate, properties);
    }

    @Bean
    @ConditionalOnClass(MeterRegistry.class)
    public IdempotencyMetricsAspect idempotencyMetricsAspect(MeterRegistry registry) {
        return new IdempotencyMetricsAspect(registry);
    }
}

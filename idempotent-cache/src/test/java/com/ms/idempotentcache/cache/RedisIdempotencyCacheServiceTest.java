package com.ms.idempotentcache.cache;

import static org.assertj.core.api.Assertions.assertThat;

import com.ms.idempotentcache.config.IdempotencyProperties;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Set;

@SpringBootTest
public class RedisIdempotencyCacheServiceTest {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    IdempotencyProperties properties;

    @Test
    void idempotencyLifecycleWorks() {
        var cache = new RedisIdempotencyCacheService(redisTemplate, properties);

        String contextId = "testContext";
        String key1 = "key-1";
        String key2 = "key-2";

        assertThat(cache.isProcessed(contextId, key1)).isFalse();

        cache.markProcessed(contextId, key1);
        assertThat(cache.isProcessed(contextId, key1)).isTrue();

        cache.markProcessed(contextId, Set.of(key2));
        assertThat(cache.isProcessed(contextId, key2)).isTrue();

        cache.clearCacheForContext(contextId);
        assertThat(cache.isProcessed(contextId, key1)).isFalse();
        assertThat(cache.isProcessed(contextId, key2)).isFalse();
    }

    @Test
    void throwsOnNullContextId() {
        var cache = new RedisIdempotencyCacheService(redisTemplate, properties);
        Assertions.assertThatThrownBy(() -> cache.markProcessed(null, "alienKey"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("contextId required");
    }
}

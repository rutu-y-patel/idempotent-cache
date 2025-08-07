package com.ms.idempotentcache.config;


import com.ms.idempotentcache.constant.IdempotencyConstant;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = IdempotencyConstant.IDEMPOTENCY_KEY)
public class IdempotencyProperties {

    private String prefix = IdempotencyConstant.IDEMPOTENT_CACHE;
    private Duration ttl = Duration.ofHours(72);

    // Getters and setters
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }

    public Duration getTtl() { return ttl; }
    public void setTtl(Duration ttl) { this.ttl = ttl; }
}

package com.ms.idempotentcache.cache;

import com.ms.idempotentcache.config.IdempotencyProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.Set;

/**
 * Redis-backed implementation of the IdempotencyCacheService interface.
 * <p>
 * Each logical context (file, job, request, etc.) is mapped to a unique Redis key (prefix + contextId).
 * Each processed record/event is a member of a Redis Set for that context.
 *
 * Example:
 *   Key: idempotent-cache:context-1   (Set of processed recordKeys for context-1)
 *   Key: idempotent-cache:context-2   (Set of processed recordKeys for context-2)
 *
 * Supports TTL (expiration) and bulk operations.
 */

public class RedisIdempotencyCacheService implements IdempotencyCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisIdempotencyCacheService.class);

    private final StringRedisTemplate redisTemplate;
    private final SetOperations<String, String> setOps;
    private final IdempotencyProperties properties;

    public RedisIdempotencyCacheService(StringRedisTemplate redisTemplate, IdempotencyProperties properties) {
        this.redisTemplate = redisTemplate;
        this.setOps = redisTemplate.opsForSet();
        this.properties = properties;
    }

    /**
     * Checks if the record key exists for the given context.
     */
    @Override
    public boolean isProcessed(String contextId, String recordKey) {
        requireContextId(contextId);
        requireRecordKey(recordKey);
        boolean result = Boolean.TRUE.equals(setOps.isMember(redisKey(contextId), recordKey));
        log.debug("Check if contextId='{}' recordKey='{}' is processed: {}", contextId, recordKey, result);
        return result;
    }

    /**
     * Marks the record key as processed for the given context.
     */
    @Override
    public void markProcessed(String contextId, String recordKey) {
        requireContextId(contextId);
        requireRecordKey(recordKey);
        setOps.add(redisKey(contextId), recordKey);
        redisTemplate.expire(redisKey(contextId), properties.getTtl());
        log.info("Marked contextId='{}' recordKey='{}' as processed (TTL={})", contextId, recordKey, properties.getTtl());
    }

    /**
     * Marks multiple records as processed (bulk insert) for the given context.
     */
    @Override
    public void markProcessed(String contextId, Set<String> recordKeys) {
        requireContextId(contextId);
        requireRecordKeys(recordKeys);
        setOps.add(redisKey(contextId), recordKeys.toArray(new String[0]));
        redisTemplate.expire(redisKey(contextId), properties.getTtl());
        log.info("Marked contextId='{}' recordKeysCount={} as processed (TTL={})", contextId, recordKeys.size(), properties.getTtl());
    }

    /**
     * Removes all cached record keys for the context (called after successful processing).
     */
    @Override
    public void clearCacheForContext(String contextId) {
        requireContextId(contextId);
        redisTemplate.delete(redisKey(contextId));
        log.info("Cleared cache for contextId='{}'", contextId);
    }

    private String redisKey(String contextId) {
        return properties.getPrefix() + contextId;
    }

    private void requireContextId(String contextId) {
        if (!StringUtils.hasText(contextId)) {
            log.warn("contextId required but was '{}'", contextId);
            throw new IllegalArgumentException("contextId required");
        }
    }

    private void requireRecordKey(String recordKey) {
        if (!StringUtils.hasText(recordKey)) {
            log.warn("record key required but was '{}'", recordKey);
            throw new IllegalArgumentException("recordKey required");
        }
    }

    private void requireRecordKeys(Set<String> recordKeys) {
        if (Objects.isNull(recordKeys) || recordKeys.isEmpty()) {
            log.warn("record key set required but was '{}'", recordKeys);
            throw new IllegalArgumentException("recordKeys required and must not be empty");
        }
        for (String key : recordKeys) {
            requireRecordKey(key);
        }
    }
}

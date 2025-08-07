package com.ms.idempotentcache.cache;

import java.util.Set;

public interface IdempotencyCacheService {

    /**
     * Checks if the record key exists for the given context.
     */
    boolean isProcessed(String contextId, String recordKey);

    /**
     * Marks the record key as processed for the given context.
     */
    void markProcessed(String contextId, String recordKey);

    /**
     * Marks multiple records as processed (bulk insert).
     */
    void markProcessed(String contextId, Set<String> recordKeys);

    /**
     * Removes all cached record keys for the context (called after successful processing).
     */
    void clearCacheForContext(String contextId);
}

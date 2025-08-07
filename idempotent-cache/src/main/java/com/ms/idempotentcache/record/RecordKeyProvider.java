package com.ms.idempotentcache.record;


@FunctionalInterface
public interface RecordKeyProvider<T> {

    /**
     * Extracts a unique key for the given record(for deduplication/idempotency).
     * Example: could be hash of entire record, or certain fields
     */
    String getKey(T record);
}

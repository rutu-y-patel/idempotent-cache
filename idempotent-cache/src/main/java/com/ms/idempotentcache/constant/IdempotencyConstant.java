package com.ms.idempotentcache.constant;

public class IdempotencyConstant {
    public static final String IDEMPOTENCY_CACHE_HIT = "idempotency.cache.hit";
    public static final String IDEMPOTENCY_CACHE_MISS = "idempotency.cache.miss";
    public static final String IDEMPOTENCY_CACHE_MARK_PROCESSED = "idempotency.cache.markProcessed";
    public static final String IDEMPOTENCY_CACHE_CLEARED = "idempotency.cache.cleared";
    public static final String AOP_IS_PROCESSED_POINTCUT_EXPRESSION = "execution(* com.ms.idempotentcache.cache.IdempotencyCacheService.isProcessed(..))";

    public static final String AOP_MARK_PROCESSED_POINTCUT_EXPRESSION = "execution(* com.ms.idempotentcache.cache.IdempotencyCacheService.markProcessed(..))";
    public static final String AOP_CLEAR_CACHE_FOR_CONTEXT_POINTCUT_EXPRESSION = "execution(* com.ms.idempotentcache.cache.IdempotencyCacheService.clearCacheForContext(..))";
    public static final String IDEMPOTENCY_KEY = "idempotency.key";
    public static final String IDEMPOTENT_CACHE = "idempotent-cache:";
}

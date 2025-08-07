package com.ms.idempotentcache.aop;


import com.ms.idempotentcache.constant.IdempotencyConstant;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * Aspect for collecting metrics on idempotency cache operations.
 * <p>
 * Tracks:
 * - cache hits and misses for isProcessed()
 * - calls to markProcessed()
 * - cache clear operations
 * <p>
 * All metrics are available via Micrometer and Spring Boot Actuator endpoints.
 */

@Aspect
@Component
public class IdempotencyMetricsAspect {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyMetricsAspect.class);

    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private final Counter markProcessedCounter;
    private final Counter clearCacheCounter;

    /**
     * Registers counters with the provided MeterRegistry.
     *
     * @param meterRegistry The registry used for collecting metrics.
     */
    public IdempotencyMetricsAspect(MeterRegistry meterRegistry) {
        this.cacheHitCounter = meterRegistry.counter(IdempotencyConstant.IDEMPOTENCY_CACHE_HIT);
        this.cacheMissCounter = meterRegistry.counter(IdempotencyConstant.IDEMPOTENCY_CACHE_MISS);
        this.markProcessedCounter = meterRegistry.counter(IdempotencyConstant.IDEMPOTENCY_CACHE_MARK_PROCESSED);
        this.clearCacheCounter = meterRegistry.counter(IdempotencyConstant.IDEMPOTENCY_CACHE_CLEARED);
    }

    /**
     * Tracks cache hits and misses for idempotency checks.
     * Increments 'hit' if true, 'miss' otherwise.
     */
    @Around(IdempotencyConstant.AOP_IS_PROCESSED_POINTCUT_EXPRESSION)
    public Object aroundIsProcessed(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        if (result instanceof Boolean && (Boolean) result) {
            cacheHitCounter.increment();
        } else {
            cacheMissCounter.increment();
        }
        return result;
    }

    /**
     * Tracks successful processing/marking of records.
     */
    @After(IdempotencyConstant.AOP_MARK_PROCESSED_POINTCUT_EXPRESSION)
    public void afterMarkProcessed() {
        markProcessedCounter.increment();
    }

    /**
     * Tracks cache clear operations.
     */
    @After(IdempotencyConstant.AOP_CLEAR_CACHE_FOR_CONTEXT_POINTCUT_EXPRESSION)
    public void afterClearCacheForContext() {
        clearCacheCounter.increment();
    }

}
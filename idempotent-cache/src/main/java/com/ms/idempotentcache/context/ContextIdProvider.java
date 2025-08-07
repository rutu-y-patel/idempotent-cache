package com.ms.idempotentcache.context;


@FunctionalInterface
public interface ContextIdProvider<C> {
    /**
     * Returns a unique context identifier for the given object.
     * <p>
     * This identifier will be used as the idempotency context key, ensuring that
     * deduplication and processing status is tracked per logical context.
     * </p>
     *
     * @param context the context object (file, job, message, etc.)
     * @return a unique string representing the idempotency context for this object
     */
    String getContextId(C context);

}

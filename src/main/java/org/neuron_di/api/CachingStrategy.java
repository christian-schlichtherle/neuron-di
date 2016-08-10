package org.neuron_di.api;

/**
 * Enumerates strategies for caching dependencies at an injection point
 * (synapse).
 */
public enum CachingStrategy {

    /**
     * Does not cache the return value of the annotated method:
     * A subsequent call by any thread may return another instance.
     */
    DISABLED,

    /**
     * Caches the return value of the annotated method:
     * Although not strictly required, a subsequent call by the same thread
     * should return the same instance.
     * A subsequent call by any other thread may return another instance.
     * This definition recursively applies to all threads.
     */
    NOT_THREAD_SAFE,

    /**
     * Caches the return value of the annotated method:
     * A subsequent call by the same thread must return the same instance.
     * Although not strictly required, a subsequent call by any other thread
     * should return another instance.
     * This definition recursively applies to all threads.
     */
    THREAD_LOCAL,

    /**
     * Caches the return value of the annotated method:
     * A subsequent call by any thread must return the same instance.
     */
    THREAD_SAFE
}

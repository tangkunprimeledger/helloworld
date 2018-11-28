package com.higgs.trust.evmcontract.facade;

/**
 * A factory interface used to create an executor.
 *
 * @param <T> the type of execution context
 * @param <V> the type parameter of executor
 *
 * @author Chen Jiawei
 * @date 2018-11-15
 */
public interface ExecutorFactory<T, V> {
    /**
     * Creates an executor.
     *
     * @param context execution context owned by the new executor
     * @return the created executor
     */
    Executor<V> createExecutor(T context);
}

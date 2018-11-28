package com.higgs.trust.evmcontract.facade;

/**
 * An object that implements the {@code Executor} interface performs
 * a series of actions, and returns caller the operation result.
 *
 * @param <T> the type of the operation result
 *
 * @author Chen Jiawei
 * @date 2018-11-15
 */
public interface Executor<T> {
    /**
     * Performs a series of actions and returns the result.
     *
     * @return the operation result
     */
    T execute();
}

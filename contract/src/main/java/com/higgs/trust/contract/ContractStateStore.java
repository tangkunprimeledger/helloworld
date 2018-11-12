package com.higgs.trust.contract;

/**
 * contract state key-value db store interface
 *
 * @author duhongming
 * @date 2018-04-09
 */
public interface ContractStateStore {
    /**
     * store contract state
     *
     * @param key
     * @param state
     */
    void put(String key, Object state);

    /**
     * get by key
     *
     * @param key
     * @return value
     */
    Object get(String key);

    /**
     * remove by key
     *
     * @param key
     */
    void remove(String key);
}

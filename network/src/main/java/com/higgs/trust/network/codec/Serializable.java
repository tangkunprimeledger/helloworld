package com.higgs.trust.network.codec;

/**
 * @author duhongming
 * @date 2018/8/22
 */
public interface Serializable {
    /**
     * 序列化
     * @return
     */
    byte[] serialize();

    /**
     * 反序列化
     * @param data
     */
    void deserialize(byte[] data);
}

package com.higgs.trust.consensus.core.command;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.consensus.util.DeflateUtil;
import lombok.ToString;

import java.util.zip.DataFormatException;

/**
 * @author cwy
 */
@ToString public abstract class AbstractConsensusCommand<T> implements ConsensusCommand<T> {
    private static final long serialVersionUID = 1L;
    private T value;
    private Long traceId;
    private byte[] bytes;

    public AbstractConsensusCommand(T value) {
        this.value = value;
    }

    public AbstractConsensusCommand(byte[] bytes){
        this.bytes = bytes;
    }



    @Override public T get() {
        return this.value;
    }

    public byte[] getValueBytes() {
        return this.bytes;
    }

    public T getValueFromByte(Class<T> clazz) throws DataFormatException {
        byte[] decom = DeflateUtil.uncompress(bytes);
        T result = JSON.parseObject(new String(decom),clazz);
        return result;
//        return ProtobufUtil.deserializer(decom,clazz);
    }

    public Long getTraceId() {
        return traceId;
    }

    public void setTraceId(Long traceId) {
        this.traceId = traceId;
    }

}

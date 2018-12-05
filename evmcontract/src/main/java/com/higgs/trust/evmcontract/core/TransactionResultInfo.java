package com.higgs.trust.evmcontract.core;

import com.higgs.trust.evmcontract.util.ByteUtil;
import com.higgs.trust.evmcontract.util.RLP;
import com.higgs.trust.evmcontract.util.RLPElement;
import com.higgs.trust.evmcontract.util.RLPList;
import com.higgs.trust.evmcontract.vm.LogInfo;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author duhongming
 * @date 2018/12/4
 */
public class TransactionResultInfo {
    private long blockHeight;
    private byte[] txHash;
    private int index;
    private Bloom bloomFilter;
    private List<LogInfo> logInfoList;
    private byte[] executionResult = ByteUtil.EMPTY_BYTE_ARRAY;
    private String error = "";

    private byte[] rlpEncoded;


    public TransactionResultInfo(long blockHeight, byte[] txHash, int index, Bloom bloomFilter, List<LogInfo> logInfoList, byte[] executionResult) {
        this.blockHeight = blockHeight;
        this.txHash = txHash;
        this.index = index;
        this.bloomFilter = bloomFilter;
        this.logInfoList = logInfoList;
        this.executionResult = executionResult;
    }

    public TransactionResultInfo(final byte[] rlp) {
        RLPList rlpList = RLP.decode2(rlp);
        blockHeight = RLP.decodeInt(rlpList.get(0).getRLPData(), 0);
        txHash = rlpList.get(1).getRLPData();
        //index = new BigInteger(1, rlpList.get(2).getRLPData()).intValue();
        index = RLP.decodeInt(rlpList.get(2).getRLPData(), 0);
        bloomFilter = new Bloom(rlpList.get(3).getRLPData());

        List<LogInfo> logInfos = new ArrayList<>();
        for (RLPElement logInfoEl : (RLPList) rlpList.get(4)) {
            LogInfo logInfo = new LogInfo(logInfoEl.getRLPData());
            logInfos.add(logInfo);
        }
        logInfoList = logInfos;

        executionResult = rlpList.get(5).getRLPData();
        rlpEncoded = rlp;
    }

    public byte[] getEncoded() {
        if (rlpEncoded != null) {
            return rlpEncoded;
        }

        byte[] bloomFilterRLP = RLP.encodeElement(this.bloomFilter.getData());
        final byte[] logInfoListRLP;
        if (logInfoList != null) {
            byte[][] logInfoListE = new byte[logInfoList.size()][];
            int i = 0;
            for (LogInfo logInfo : logInfoList) {
                logInfoListE[i] = logInfo.getEncoded();
                ++i;
            }
            logInfoListRLP = RLP.encodeList(logInfoListE);
        } else {
            logInfoListRLP = RLP.encodeList();
        }


        rlpEncoded = RLP.encodeList(
                RLP.encodeInt((int)blockHeight),
                RLP.encodeElement(txHash),
                RLP.encodeInt(index),
                bloomFilterRLP,
                logInfoListRLP,
                RLP.encodeElement(executionResult)
        );

        return rlpEncoded;
    }


    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(int blockHeight) {
        this.blockHeight = blockHeight;
    }

    public byte[] getTxHash() {
        return txHash;
    }

    public void setTxHash(byte[] txHash) {
        this.txHash = txHash;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Bloom getBloomFilter() {
        return bloomFilter;
    }

    public void setBloomFilter(Bloom bloomFilter) {
        this.bloomFilter = bloomFilter;
    }

    public List<LogInfo> getLogInfoList() {
        return logInfoList;
    }

    public void setLogInfoList(List<LogInfo> logInfoList) {
        this.logInfoList = logInfoList;
    }

    public byte[] getExecutionResult() {
        return executionResult;
    }

    public void setExecutionResult(byte[] executionResult) {
        this.executionResult = executionResult;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public byte[] getRlpEncoded() {
        return rlpEncoded;
    }

    public void setRlpEncoded(byte[] rlpEncoded) {
        this.rlpEncoded = rlpEncoded;
    }
}

package com.higgs.trust.evmcontract.core;

import com.higgs.trust.evmcontract.util.ByteUtil;
import com.higgs.trust.evmcontract.util.RLP;
import com.higgs.trust.evmcontract.util.RLPElement;
import com.higgs.trust.evmcontract.util.RLPList;
import com.higgs.trust.evmcontract.vm.LogInfo;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private byte[] createdAddress = ByteUtil.EMPTY_BYTE_ARRAY;

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
        if (rlp.length == 0) {
            return;
        }

        RLPList rlpList = (RLPList) RLP.decode2(rlp).get(0);
        blockHeight = RLP.decodeInt(rlpList.get(0).getRLPData(), 0);
        txHash = rlpList.get(1).getRLPData();
        index = RLP.decodeInt(rlpList.get(2).getRLPData(), 0);
        bloomFilter = new Bloom(rlpList.get(3).getRLPData());

        List<LogInfo> logInfos = new ArrayList<>();
        for (RLPElement logInfoEl : (RLPList) rlpList.get(4)) {
            LogInfo logInfo = new LogInfo(logInfoEl.getRLPData());
            logInfos.add(logInfo);
        }
        logInfoList = logInfos;

        executionResult = rlpList.get(5).getRLPData();
        createdAddress = rlpList.get(6).getRLPData();
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
                RLP.encodeInt((int) blockHeight),
                RLP.encodeElement(txHash),
                RLP.encodeInt(index),
                bloomFilterRLP,
                logInfoListRLP,
                RLP.encodeElement(executionResult),
                RLP.encodeElement(createdAddress)
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

    public byte[] getCreatedAddress() {
        return createdAddress;
    }

    public void setCreatedAddress(byte[] createdAddress) {
        this.createdAddress = createdAddress;
    }

    public byte[] getRlpEncoded() {
        return rlpEncoded;
    }

    public void setRlpEncoded(byte[] rlpEncoded) {
        this.rlpEncoded = rlpEncoded;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>(6);
        map.put("txId", new String(txHash));
        map.put("blockHeight", blockHeight);
        map.put("result", StringUtils.isEmpty(error) ? "Success" : "Error");
        map.put("error", error);
        map.put("logInfoList", logInfoList);
        if (createdAddress.length > 0) {
            map.put("createdAddress", Hex.toHexString(createdAddress));
        }
        return map;
    }

    public static void main(String[] args) {
        String s = new String(ByteUtil.EMPTY_BYTE_ARRAY);
        byte[] data = RLP.encodeList( RLP.encodeInt(1), RLP.encodeInt(2));
        byte[][] data2 = new byte[2][];
        data2[0] = RLP.encodeInt(1);
        data2[1] = RLP.encodeInt(2);
        RLPList list = RLP.unwrapList(data);

        RLPList list2 = RLP.unwrapList(RLP.encodeList(data2));
        System.out.println(list);
    }
}

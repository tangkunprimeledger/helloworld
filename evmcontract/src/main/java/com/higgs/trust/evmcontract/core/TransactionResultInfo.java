package com.higgs.trust.evmcontract.core;

import com.higgs.trust.evmcontract.crypto.HashUtil;
import com.higgs.trust.evmcontract.solidity.Abi;
import com.higgs.trust.evmcontract.util.ByteUtil;
import com.higgs.trust.evmcontract.util.RLP;
import com.higgs.trust.evmcontract.util.RLPElement;
import com.higgs.trust.evmcontract.util.RLPList;
import com.higgs.trust.evmcontract.vm.LogInfo;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.encoders.Hex;

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
    private byte[] result = ByteUtil.EMPTY_BYTE_ARRAY;
    private String error = "";
    private byte[] createdAddress = ByteUtil.EMPTY_BYTE_ARRAY;
    private String invokeMethod = "";

    private byte[] rlpEncoded;


    public TransactionResultInfo(long blockHeight, byte[] txHash, int index, Bloom bloomFilter, List<LogInfo> logInfoList, byte[] result) {
        this.blockHeight = blockHeight;
        this.txHash = txHash;
        this.index = index;
        this.bloomFilter = bloomFilter;
        this.logInfoList = logInfoList;
        this.result = result == null ? ByteUtil.EMPTY_BYTE_ARRAY : result;
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

        result = rlpList.get(5).getRLPData();
        createdAddress = rlpList.get(6).getRLPData();
        error = decodeString(rlpList.get(7).getRLPData());
        invokeMethod = decodeString(rlpList.get(8).getRLPData());
        rlpEncoded = rlp;
    }

    private String decodeString(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        return new String(data);
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
                RLP.encodeElement(result),
                RLP.encodeElement(createdAddress),
                RLP.encodeString(error == null ? "" : error),
                RLP.encodeString(invokeMethod == null ? "" : invokeMethod)
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

    public byte[] getResult() {
        return result;
    }

    public void setResult(byte[] result) {
        this.result = result;
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

    public String getInvokeMethod() {
        return invokeMethod;
    }

    public void setInvokeMethod(String invokeMethod) {
        this.invokeMethod = invokeMethod;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>(9);
        map.put("txId", new String(txHash));
        map.put("blockHeight", blockHeight);
        map.put("success", StringUtils.isEmpty(error) ? true : false);
        map.put("error", error);
        map.put("logInfoList", logInfoList);
        if (result != null) {
            if(StringUtils.isNotEmpty(invokeMethod)) {
                Abi.Function func = Abi.Function.of(invokeMethod);
                map.put("result", func.decodeResult(result));
            } else {
                map.put("result", Hex.toHexString(result));
            }
        }
        if (createdAddress != null && createdAddress.length > 0) {
            map.put("createdAddress", Hex.toHexString(createdAddress));
        }
        if (StringUtils.isNotEmpty(invokeMethod)) {
            map.put("invokeMethod", invokeMethod);
        }

        return map;
    }
}

package com.higgs.trust.evmcontract.facade;

import com.higgs.trust.evmcontract.core.Bloom;
import com.higgs.trust.evmcontract.util.ByteArraySet;
import com.higgs.trust.evmcontract.util.RLP;
import com.higgs.trust.evmcontract.vm.DataWord;
import com.higgs.trust.evmcontract.vm.LogInfo;
import com.higgs.trust.evmcontract.vm.program.InternalTransaction;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * An instance of this class is used to save result of contract execution.
 *
 * @author Chen Jiawei
 * @date 2018-11-15
 */
public class ContractExecutionResult {

    private static final ThreadLocal<ContractExecutionResult> currentResult = new ThreadLocal<>();

    /**
     * Address list of accounts participating in the contract execution.
     */
    @Getter
    private ByteArraySet touchedAccountAddresses = new ByteArraySet();

    /**
     * Hash of transaction which contains the contract.
     */
    @Getter
    @Setter
    private byte[] transactionHash;

    /**
     * Amount of asset transferred to receiver.
     */
    @Getter
    @Setter
    private byte[] value;

    /**
     * Returned event records.
     */
    @Getter
    private List<LogInfo> logInfoList;

    public void setLogInfoList(List<LogInfo> logInfoList) {
        if (logInfoList == null) {
            return;
        }
        this.logInfoList = logInfoList;
        for (LogInfo loginfo : logInfoList) {
            bloomFilter.or(loginfo.getBloom());
        }
    }

    /**
     * Bloom filter for transaction.
     */
    @Getter
    private Bloom bloomFilter = new Bloom();

    /**
     * Byte code stored after contract execution.
     */
    @Getter
    @Setter
    private byte[] result;

    /**
     * Deleted account address during contract execution.
     */
    @Getter
    @Setter
    private Set<DataWord> deleteAccounts;

    /**
     * Internal transaction happened during contract execution.
     */
    @Getter
    @Setter
    private List<InternalTransaction> internalTransactions;

    /**
     * Exception thrown during contract execution.
     */
    @Getter
    @Setter
    private RuntimeException exception;

    /**
     * Root hash of global state after contract execution.
     */
    @Getter
    @Setter
    private byte[] stateRoot;

    /**
     * Revert or exception information, null if contract is executed
     * successfully.
     */
    @Getter
    @Setter
    private String errorMessage;

    /**
     * If returning revert.
     * boolean Getter is not supported.
     */
    @Setter
    private boolean revert;

    public boolean getRevert() {
        return revert;
    }

    /**
     * Time cost for contract execution.
     */
    @Getter
    @Setter
    private long timeCost;

    public byte[] getReceiptTrieEncoded() {
        byte[] bloomRLP = RLP.encodeElement(this.bloomFilter.getData());
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
        return RLP.encodeList(RLP.encodeElement(value), bloomRLP, logInfoListRLP, RLP.encodeElement(result));
    }


    /**
     * Set the result to ThreadLocal
     * @param result
     */
    public static void setCurrentResult(ContractExecutionResult result) {
        currentResult.set(result);
    }

    /**
     * Get result from ThreadLocal
     * @return
     */
    public static ContractExecutionResult getCurrentResult() {
        return currentResult.get();
    }

    /**
     * Clear result on ThreadLocal
     */
    public static void clearCurrentResult() {
        currentResult.remove();
    }
}

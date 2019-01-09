package com.higgs.trust.evmcontract.facade;

import com.higgs.trust.evmcontract.config.SystemProperties;
import com.higgs.trust.evmcontract.core.Repository;
import com.higgs.trust.evmcontract.db.BlockStore;
import com.higgs.trust.evmcontract.facade.constant.Constant;
import lombok.Getter;
import org.spongycastle.util.encoders.Hex;

/**
 * Context for contract execution. All fields should be filled when an instance
 * of this class is created. All needed data for executing contract comes from
 * this context, not any other source.
 *
 * @author Chen Jiawei
 * @date 2018-11-15
 */
public class ContractExecutionContext {
    /**
     * Type of contract.
     */
    @Getter
    private ContractTypeEnum contractType;

    /**
     * Hash of transaction, it is used in EVM program.
     */
    @Getter
    private byte[] transactionHash;

    /**
     * Transaction nonce. A counter used to make sure each transaction can only
     * be processed once.
     */
    @Getter
    private byte[] nonce;

    /**
     * Address of sender who emits the transaction.
     */
    @Getter
    private byte[] senderAddress;

    /**
     * Address of receiver. It is a contract address if the transaction is
     * contract-related, or an address of external account if the transaction
     * is asset-transfer.
     */
    @Getter
    private byte[] receiverAddress;

    /**
     * Gas price the sender is willing pay for transaction execution. To the
     * project, fee is not provided. After introducing EVM, gasPrice is
     * reserved and set as zero meaning that no fee is required for contract
     * execution.
     */
    @Getter
    private byte[] gasPrice = Constant.TRANSACTION_GAS_PRICE;

    /**
     * Maximum gas amount the sender is willing pay for transaction execution.
     * To original Trust, contract execution is controlled by step limit.
     * After introducing EVM, gasLimit is reserved and enough for execution of
     * normal contract.
     */
    @Getter
    private byte[] gasLimit = Constant.TRANSACTION_GAS_LIMIT;

    /**
     * Amount of asset transferred to receiver.
     */
    @Getter
    private byte[] value;

    /**
     * Payload, simple to say is byte code.
     */
    @Getter
    private byte[] data;

    /**
     * Hash of parent block.
     */
    @Getter
    private byte[] parentHash;

    /**
     * Address of miner generates the block.
     */
    @Getter
    private byte[] minerAddress;

    /**
     * Timestamp of the block.
     */
    @Getter
    private long timestamp;

    /**
     * Height of the block.
     */
    @Getter
    private long number;

    /**
     * Difficulty of the block. To the project, it is meaningless because
     * consensus protocol is not of PoW, just reserved for Solidity program.
     */
    @Getter
    private byte[] difficulty = Constant.BLOCK_DIFFICULTY;

    /**
     * Maximum gas amount the block allows for transactions execution. To
     * the project, it is not useful, just reserved for Solidity program.
     */
    @Getter
    private byte[] gasLimitBlock = Constant.BLOCK_GAS_LIMIT;

    /**
     * For contract execution, {@link BlockStore#getBlockHashByNumber(long, byte[])}
     * must be implemented, and other methods of this object are necessary. To the
     * project, parameter byte[] is unnecessary because of no forks, parameter Long
     * alone is needed, and known as block height.
     */
    @Getter
    private BlockStore blockStore;

    /**
     * Snapshot from root repository of previous block. Used as cache during
     * process block.
     */
    @Getter
    private Repository blockRepository;

    /**
     * System configuration.
     */
    @Getter
    private SystemProperties systemProperties = SystemProperties.getDefault();

    public ContractExecutionContext(ContractTypeEnum contractType, byte[] transactionHash, byte[] nonce,
                                    byte[] senderAddress, byte[] receiverAddress, byte[] value, byte[] data,
                                    byte[] parentHash, byte[] minerAddress, long timestamp, long number,
                                    BlockStore blockStore, Repository blockRepository) {
        this.contractType = contractType;
        this.transactionHash = transactionHash;
        this.nonce = nonce;
        this.senderAddress = senderAddress;
        this.receiverAddress = receiverAddress;
        this.value = value;
        this.data = data;
        this.parentHash = parentHash;
        this.minerAddress = minerAddress;
        this.timestamp = timestamp;
        this.number = number;
        this.blockStore = blockStore;
        this.blockRepository = blockRepository;
    }

    @Override
    public String toString() {
        return "ContractExecutionContext [transactionHash=" + defaultNull(transactionHash) +
                ", contractType=" + contractType +
                ", data=" + defaultNull(data) +
                ", senderAddress=" + defaultNull(senderAddress) +
                ", receiverAddress=" + defaultNull(receiverAddress) +
                ", parentHash=" + defaultNull(parentHash) +
                ", timestamp=" + timestamp +
                ", number=" + number +
                ", difficulty=" + defaultNull(difficulty) + "]";
    }

    private String defaultNull(byte[] bytes) {
        return (bytes != null ? Hex.toHexString(bytes) : null);
    }
}

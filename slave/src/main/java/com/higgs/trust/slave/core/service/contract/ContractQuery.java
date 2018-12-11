package com.higgs.trust.slave.core.service.contract;

import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.evmcontract.crypto.HashUtil;
import com.higgs.trust.evmcontract.facade.*;
import com.higgs.trust.evmcontract.facade.compile.ContractInvocation;
import com.higgs.trust.evmcontract.vm.DataWord;
import com.higgs.trust.slave.core.Blockchain;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Chen Jiawei
 * @date 2018-11-29
 */
@Service
public class ContractQuery {
    @Autowired
    Blockchain blockchain;
    @Autowired
    NodeState nodeState;

    private static final Pattern BLOCK_HASH_PATTERN = Pattern.compile("^[0-9a-zA-Z]{64}$");

    public List<?> executeQuery(byte[] contractAddress, String methodSignature, Object... args) {
        try {
            Profiler.enter(String.format("query contract at %s", Hex.toHexString(contractAddress)));

            ContractInvocation contractInvocation = new ContractInvocation();
            byte[] data = contractInvocation.getBytecodeForInvokeContract(methodSignature, args);

            ContractExecutionContext context = buildContractExecutionContext(contractAddress, data);
            ExecutorFactory<ContractExecutionContext, ContractExecutionResult> factory = new ContractExecutorFactory();
            Executor<ContractExecutionResult> executor = factory.createExecutor(context);
            ContractExecutionResult result = executor.execute();

            return contractInvocation.decodeResult(result.getResult());
        } finally {
            Profiler.release();
        }
    }

    private ContractExecutionContext buildContractExecutionContext(byte[] receiverAddress, byte[] data) {
        ContractTypeEnum contractType = ContractTypeEnum.CUSTOMER_CONTRACT_QUERYING;
        byte[] nodeNameBytes = HashUtil.sha256(nodeState.getNodeName().getBytes());
        // every one can query, even if no account exists.
        byte[] senderAddress = nodeNameBytes;
        byte[] nonce = blockchain.getRepository().getNonce(senderAddress).toByteArray();
        byte[] transactionHash = HashUtil.sha256(
                ("10000000" + Hex.toHexString(nodeNameBytes) + System.currentTimeMillis()).getBytes());
        byte[] value = new DataWord(0).getData();

        long number = blockchain.getLastBlockHeader().getHeight();
        String previousHash = blockchain.getLastBlockHeader().getPreviousHash();
        if (!BLOCK_HASH_PATTERN.matcher(previousHash).matches()) {
            throw new IllegalArgumentException("Block hash should be a hex string of 32 bytes");
        }
        byte[] parentHash = Hex.decode(previousHash);
        byte[] minerAddress = nodeNameBytes;
        // Trust use mills second and evm use second.
        long timestamp = blockchain.getLastBlockHeader().getBlockTime() / 1000;

        return new ContractExecutionContext(contractType, transactionHash, nonce, senderAddress, receiverAddress,
                value, data, parentHash, minerAddress, timestamp, number,
                blockchain.getBlockStore(), blockchain.getRepository());
    }
}

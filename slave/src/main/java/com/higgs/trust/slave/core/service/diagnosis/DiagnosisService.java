package com.higgs.trust.slave.core.service.diagnosis;

import com.higgs.trust.evmcontract.core.Repository;
import com.higgs.trust.slave.api.vo.diagnosis.ContractCodeVO;
import com.higgs.trust.slave.core.Blockchain;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * @author Chen Jiawei
 * @date 2019-01-21
 */
@Slf4j
@Service
public class DiagnosisService {
    @Autowired
    private Blockchain blockchain;

    private static final Pattern CONTRACT_ADDRESS = Pattern.compile("^[0-9a-fA-F]{40}$");

    /**
     * Query contract code with the specified address and the block height.
     *
     * @param address contract address, a hex string with 40 characters
     * @param height  height of block in which contract exists, if
     *                null, query is on the latest block
     * @return the contract code
     */
    public ContractCodeVO queryContractCode(String address, Long height) {
        if (!CONTRACT_ADDRESS.matcher(address).matches()) {
            throw new IllegalArgumentException("Given contract address must be a hex string, 40 characters in length");
        }

        long blockHeight = -1;
        if (height != null) {
            blockHeight = height;
        }

        Repository blockRepository =
                blockHeight <= 1 ? blockchain.getRepository() : blockchain.getRepositorySnapshot(blockHeight);
        byte[] code = blockRepository.getCode(Hex.decode(address));

        ContractCodeVO contractCode = new ContractCodeVO();
        contractCode.setAddress(address);
        contractCode.setHeight(height);
        contractCode.setCode(Hex.toHexString(code));

        return contractCode;
    }
}

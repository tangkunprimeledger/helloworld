package com.higgs.trust.evmcontract.facade.compile;

import com.higgs.trust.evmcontract.solidity.Abi;
import com.higgs.trust.evmcontract.solidity.compiler.CompilationResult;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;

/**
 * @author Chen Jiawei
 * @date 2018-11-29
 */
public class ContractCreation {
    public static byte[] encodeInput(
            CompilationResult.ContractMetadata metadata, String constructorSignature, Object... args) {
        return Abi.Constructor.of(constructorSignature, Hex.decode(metadata.bin), args);
    }

    public static byte[] getBytecodeForDeployContract(String filePath, String contractName, String constructorSignature, Object... args) throws IOException {
        CompilationResult compilationResult = CompileManager.getCompilationResultByFile(filePath);
        CompilationResult.ContractMetadata metadata = compilationResult.getContract(contractName);

        return encodeInput(metadata, constructorSignature, args);
    }
}

package com.higgs.trust.evmcontract.demo;

import com.higgs.trust.evmcontract.solidity.Abi;
import com.higgs.trust.evmcontract.solidity.SolidityType;
import com.higgs.trust.evmcontract.solidity.compiler.CompilationResult;
import com.higgs.trust.evmcontract.solidity.compiler.SolidityCompiler;
import com.higgs.trust.evmcontract.util.ByteUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.higgs.trust.evmcontract.solidity.compiler.SolidityCompiler.Options.*;

/**
 * @author duhongming
 * @date 2018/11/19
 */
public class TestContract {

    private ContractService contractService;

    @Before
    public void onBefore() {
        contractService = new ContractService(null);
    }

    @After
    public void onAfter() {
        contractService.destroy();
    }

    @Test
    public void testCreateContract() throws IOException {
        Path source = Paths.get("src","test/resources/contract/Test.sol");

        SolidityCompiler.Option allowPathsOption = new SolidityCompiler.Options.AllowPaths(Collections.singletonList(source.getParent().getParent()));
        SolidityCompiler.Result res = SolidityCompiler.compile(source.toFile(), true, ABI, BIN, INTERFACE, METADATA, allowPathsOption);
        CompilationResult result = CompilationResult.parse(res.output);
        CompilationResult.ContractMetadata metadata = result.getContract("Test");

        List<String> userList = new ArrayList<>(2000);
        List<Integer> userAmountList = new ArrayList<>(userList.size());
        // ca35b7d915458ef540ade6068dfe2f44e8fa733c
        BigInteger bi = new BigInteger("1154414090619811796818182302139415280051214250812");
        for(int i = 0; i < 2000; i++) {
            userList.add(Hex.toHexString(ByteUtil.bigIntegerToBytes(bi.add(BigInteger.valueOf(i)), 20)));
            userAmountList.add(i);
        }

        BigInteger n = BigInteger.ZERO;
        byte[] code = Abi.Constructor.of("Test(address[], uint[])",
                Hex.decode(metadata.bin), userList, userAmountList);
        byte[] receiveAddress =  Hex.decode("");//13978aee95f38490e9769c39b2773ed763d9cd5f
        byte[] value = Hex.decode("");          //10000000000000000 2386f26fc10000"

        for(int i = 0; i < 10; i++) {
            byte[] nonce = BigIntegers.asUnsignedByteArray(n.add(BigInteger.valueOf(i + 0)));
            byte[] address = contractService.createContract(nonce, receiveAddress, value, code);
            if (i % 50 == 0) {
                contractService.commit();
                System.out.println("Create Contract count:" + (i + 1));
            }
        }
        contractService.commit();

    }

    @Test
    public void testInvoke() {
        // address e01c10fd900939d1eab56ee373ea5e2bd4e2cfb3

        // root hash
        // b708c55184e5573e5ea974bd1bde4c6d8d8f19f2c98efe5cf28270e48fdb3661
        // fa752e04857d03135ce311c9fb27b72316c92d43fb44a24114fccb995b797ce9

        byte[] contractAddress = Hex.decode("e01c10fd900939d1eab56ee373ea5e2bd4e2cfb3");
        byte[] value = Hex.decode(""); //10000000000000000 2386f26fc10000"
        byte[] nonce = BigIntegers.asUnsignedByteArray(BigInteger.ZERO);

        //call contract
        //byte[] data = Hex.decode("b2ab5154"); // putVal
        Abi.Function func = Abi.Function.of("(address[], uint[], string, uint) getUserInfo()");
        byte[] invokeFuncData = func.encode();

//        Abi.Function func = Abi.Function.of("(uint) balanceOf(address)");
//        byte[] invokeFuncData = func.encode("ca35b7d915458ef540ade6068dfe2f44e8fa733c");

        long startTime = System.currentTimeMillis();
        byte[] resultData = contractService.invokeContract(nonce, contractAddress, value, invokeFuncData);
        System.out.println("耗时：" + (System.currentTimeMillis() - startTime));

        startTime = System.currentTimeMillis();
        List<?> result = func.decodeResult(resultData, false);
        System.out.println("耗时1：" + (System.currentTimeMillis() - startTime));

//        startTime = System.currentTimeMillis();
//        List<?> result2 = func.decodeResult(resultData, false);
//        System.out.println("耗时2：" + (System.currentTimeMillis() - startTime));

        System.out.println(result);
        System.out.println(Abi.dumpResult(result));
        contractService.commit();
    }


    public static void main(String[] args) {
        List<Abi.Entry.Param> inputs = new ArrayList<>();
        List<Abi.Entry.Param> outputs = new ArrayList<>();
        Abi.Entry.Param p1 = new Abi.Entry.Param();
        p1.type = new SolidityType.IntType("uint256");
        Abi.Entry.Param p2 = new Abi.Entry.Param();
        p2.type = new SolidityType.IntType("uint256");
        inputs.add(p1);
        inputs.add(p2);

        Abi.Function func = new Abi.Function(false, "putVal", inputs, outputs, false);
        byte[] sign = func.encode();
        func.encode();


        System.out.println(Hex.toHexString(sign));
        System.out.println(Hex.toHexString(Abi.Function.of("putVal(uint256,uint256)").encode(1, 2)));
    }
}

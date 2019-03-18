package com.higgs.trust.evmcontract.solidity;

import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author duhongming
 * @date 2018/11/22
 */

public class AbiTest {

    @Test
    public void simpleTest() throws IOException {
        String contractAbi = "[{"
                + "\"name\":\"simpleFunction\","
                + "\"constant\":true,"
                + "\"payable\":true,"
                + "\"type\":\"function\","
                + "\"inputs\": [{\"name\":\"_in\", \"type\":\"bytes32\"}],"
                + "\"outputs\":[{\"name\":\"_out\",\"type\":\"string\"}]}]";

        Abi abi = Abi.fromJson(contractAbi);
        assertEquals(abi.size(), 1);

        Abi.Entry onlyFunc = abi.get(0);
        assertEquals(onlyFunc.type, Abi.Entry.Type.function);
        assertEquals(onlyFunc.inputs.size(), 1);
        assertEquals(onlyFunc.outputs.size(), 1);
        assertTrue(onlyFunc.payable);
        assertTrue(onlyFunc.constant);

    }
}

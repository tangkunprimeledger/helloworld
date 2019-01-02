package com.higgs.trust.evmcontract.solidity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Chen Jiawei
 * @date 2019-01-02
 */
public class FunctionParserTest {
    private static final FunctionParser functionParser = FunctionParser.getInstance();

    @Test
    public void testParse_01() {
        String functionSignature = "(uint, address, uint) run(address, bytes32)";
        Abi.Function function = functionParser.parse(functionSignature);

        assertEquals(Abi.Entry.Type.function, function.type);
        assertEquals("run", function.name);
        assertEquals(2, function.inputs.size());
        assertEquals("address", function.inputs.get(0).type.getName());
        assertEquals("bytes32", function.inputs.get(1).type.getName());
        assertEquals(3, function.outputs.size());
        assertEquals("uint", function.outputs.get(0).type.getName());
        assertEquals("address", function.outputs.get(1).type.getName());
        assertEquals("uint", function.outputs.get(2).type.getName());
    }

    @Test
    public void testParse_02() {
        String functionSignature = "(uint, address, uint)run(address, bytes32)";
        Abi.Function function = functionParser.parse(functionSignature);

        assertEquals(Abi.Entry.Type.function, function.type);
        assertEquals("run", function.name);
        assertEquals(2, function.inputs.size());
        assertEquals("address", function.inputs.get(0).type.getName());
        assertEquals("bytes32", function.inputs.get(1).type.getName());
        assertEquals(3, function.outputs.size());
        assertEquals("uint", function.outputs.get(0).type.getName());
        assertEquals("address", function.outputs.get(1).type.getName());
        assertEquals("uint", function.outputs.get(2).type.getName());
    }

    @Test
    public void testParse_03() {
        String functionSignature = "  (  uint  ,  address  ,  uint  )  run   (  address   ,   bytes32    )";
        Abi.Function function = functionParser.parse(functionSignature);

        assertEquals(Abi.Entry.Type.function, function.type);
        assertEquals("run", function.name);
        assertEquals(2, function.inputs.size());
        assertEquals("address", function.inputs.get(0).type.getName());
        assertEquals("bytes32", function.inputs.get(1).type.getName());
        assertEquals(3, function.outputs.size());
        assertEquals("uint", function.outputs.get(0).type.getName());
        assertEquals("address", function.outputs.get(1).type.getName());
        assertEquals("uint", function.outputs.get(2).type.getName());
    }

    @Test
    public void testParse_04() {
        String functionSignature = "(uint, address, uint) run";
        Abi.Function function = functionParser.parse(functionSignature);

        assertEquals(Abi.Entry.Type.function, function.type);
        assertEquals("run", function.name);
        assertEquals(0, function.inputs.size());
        assertEquals(3, function.outputs.size());
        assertEquals("uint", function.outputs.get(0).type.getName());
        assertEquals("address", function.outputs.get(1).type.getName());
        assertEquals("uint", function.outputs.get(2).type.getName());
    }

    @Test
    public void testParse_05() {
        String functionSignature = "run(address, bytes32)";
        Abi.Function function = functionParser.parse(functionSignature);

        assertEquals(Abi.Entry.Type.function, function.type);
        assertEquals("run", function.name);
        assertEquals(2, function.inputs.size());
        assertEquals("address", function.inputs.get(0).type.getName());
        assertEquals("bytes32", function.inputs.get(1).type.getName());
        assertEquals(0, function.outputs.size());
    }

    @Test
    public void testParse_06() {
        String functionSignature = "    run   ";
        Abi.Function function = functionParser.parse(functionSignature);

        assertEquals(Abi.Entry.Type.function, function.type);
        assertEquals("run", function.name);
        assertEquals(0, function.inputs.size());
        assertEquals(0, function.outputs.size());
    }

    @Test
    public void testParse_07() {
        String functionSignature = "run";
        Abi.Function function = functionParser.parse(functionSignature);

        assertEquals(Abi.Entry.Type.function, function.type);
        assertEquals("run", function.name);
        assertEquals(0, function.inputs.size());
        assertEquals(0, function.outputs.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_08() {
        String functionSignature = "(uint, address, uint) run pay(address, bytes32)";
        Abi.Function function = functionParser.parse(functionSignature);

        assertEquals(Abi.Entry.Type.function, function.type);
        assertEquals("run", function.name);
        assertEquals(2, function.inputs.size());
        assertEquals("address", function.inputs.get(0).type.getName());
        assertEquals("bytes32", function.inputs.get(1).type.getName());
        assertEquals(3, function.outputs.size());
        assertEquals("uint", function.outputs.get(0).type.getName());
        assertEquals("address", function.outputs.get(1).type.getName());
        assertEquals("uint", function.outputs.get(2).type.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_09() {
        String functionSignature = "(ui nt, address, uint) run(address, bytes32)";
        Abi.Function function = functionParser.parse(functionSignature);

        assertEquals(Abi.Entry.Type.function, function.type);
        assertEquals("run", function.name);
        assertEquals(2, function.inputs.size());
        assertEquals("address", function.inputs.get(0).type.getName());
        assertEquals("bytes32", function.inputs.get(1).type.getName());
        assertEquals(3, function.outputs.size());
        assertEquals("uint", function.outputs.get(0).type.getName());
        assertEquals("address", function.outputs.get(1).type.getName());
        assertEquals("uint", function.outputs.get(2).type.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_10() {
        String functionSignature = "(uint, address, uint) run(addr ess, bytes32)";
        Abi.Function function = functionParser.parse(functionSignature);

        assertEquals(Abi.Entry.Type.function, function.type);
        assertEquals("run", function.name);
        assertEquals(2, function.inputs.size());
        assertEquals("address", function.inputs.get(0).type.getName());
        assertEquals("bytes32", function.inputs.get(1).type.getName());
        assertEquals(3, function.outputs.size());
        assertEquals("uint", function.outputs.get(0).type.getName());
        assertEquals("address", function.outputs.get(1).type.getName());
        assertEquals("uint", function.outputs.get(2).type.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_11() {
        String functionSignature = "(uint, address, uint)() run(address, bytes32)";
        Abi.Function function = functionParser.parse(functionSignature);

        assertEquals(Abi.Entry.Type.function, function.type);
        assertEquals("run", function.name);
        assertEquals(2, function.inputs.size());
        assertEquals("address", function.inputs.get(0).type.getName());
        assertEquals("bytes32", function.inputs.get(1).type.getName());
        assertEquals(3, function.outputs.size());
        assertEquals("uint", function.outputs.get(0).type.getName());
        assertEquals("address", function.outputs.get(1).type.getName());
        assertEquals("uint", function.outputs.get(2).type.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_12() {
        String functionSignature = "(uint, address, uint) run()(address, bytes32)";
        Abi.Function function = functionParser.parse(functionSignature);

        assertEquals(Abi.Entry.Type.function, function.type);
        assertEquals("run", function.name);
        assertEquals(2, function.inputs.size());
        assertEquals("address", function.inputs.get(0).type.getName());
        assertEquals("bytes32", function.inputs.get(1).type.getName());
        assertEquals(3, function.outputs.size());
        assertEquals("uint", function.outputs.get(0).type.getName());
        assertEquals("address", function.outputs.get(1).type.getName());
        assertEquals("uint", function.outputs.get(2).type.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_13() {
        String functionSignature = "(uint, address, uint) run(address, -bytes32)";
        Abi.Function function = functionParser.parse(functionSignature);

        assertEquals(Abi.Entry.Type.function, function.type);
        assertEquals("run", function.name);
        assertEquals(2, function.inputs.size());
        assertEquals("address", function.inputs.get(0).type.getName());
        assertEquals("bytes32", function.inputs.get(1).type.getName());
        assertEquals(3, function.outputs.size());
        assertEquals("uint", function.outputs.get(0).type.getName());
        assertEquals("address", function.outputs.get(1).type.getName());
        assertEquals("uint", function.outputs.get(2).type.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_14() {
        String functionSignature = "(uint, ) run(address, bytes32)";
        Abi.Function function = functionParser.parse(functionSignature);

        assertEquals(Abi.Entry.Type.function, function.type);
        assertEquals("run", function.name);
        assertEquals(2, function.inputs.size());
        assertEquals("address", function.inputs.get(0).type.getName());
        assertEquals("bytes32", function.inputs.get(1).type.getName());
        assertEquals(3, function.outputs.size());
        assertEquals("uint", function.outputs.get(0).type.getName());
        assertEquals("address", function.outputs.get(1).type.getName());
        assertEquals("uint", function.outputs.get(2).type.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_15() {
        String functionSignature = "(uint, address, uint) run(address, bytes32) pay()";
        Abi.Function function = functionParser.parse(functionSignature);

        assertEquals(Abi.Entry.Type.function, function.type);
        assertEquals("run", function.name);
        assertEquals(2, function.inputs.size());
        assertEquals("address", function.inputs.get(0).type.getName());
        assertEquals("bytes32", function.inputs.get(1).type.getName());
        assertEquals(3, function.outputs.size());
        assertEquals("uint", function.outputs.get(0).type.getName());
        assertEquals("address", function.outputs.get(1).type.getName());
        assertEquals("uint", function.outputs.get(2).type.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_16() {
        String functionSignature = "run(uint, address, uint) (address, bytes32)";
        Abi.Function function = functionParser.parse(functionSignature);

        assertEquals(Abi.Entry.Type.function, function.type);
        assertEquals("run", function.name);
        assertEquals(2, function.inputs.size());
        assertEquals("address", function.inputs.get(0).type.getName());
        assertEquals("bytes32", function.inputs.get(1).type.getName());
        assertEquals(3, function.outputs.size());
        assertEquals("uint", function.outputs.get(0).type.getName());
        assertEquals("address", function.outputs.get(1).type.getName());
        assertEquals("uint", function.outputs.get(2).type.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_17() {
        String functionSignature = "(uint, address,, uint) run(address, bytes32)";
        Abi.Function function = functionParser.parse(functionSignature);

        assertEquals(Abi.Entry.Type.function, function.type);
        assertEquals("run", function.name);
        assertEquals(2, function.inputs.size());
        assertEquals("address", function.inputs.get(0).type.getName());
        assertEquals("bytes32", function.inputs.get(1).type.getName());
        assertEquals(3, function.outputs.size());
        assertEquals("uint", function.outputs.get(0).type.getName());
        assertEquals("address", function.outputs.get(1).type.getName());
        assertEquals("uint", function.outputs.get(2).type.getName());
    }
}
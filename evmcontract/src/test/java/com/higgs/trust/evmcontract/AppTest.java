package com.higgs.trust.evmcontract;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    @Test
    public void testList() {

//        List list = new ArrayList<>();
//
//        list.add(BigInteger.valueOf(20L));
//        list.add("1111");
//
//        list.add("2222".getBytes());
//        System.out.println(list);

        String str = null;
        StringBuilder sb = new StringBuilder();
        method(str,sb);

        System.out.println(str);
        System.out.println("sb"+sb.toString());
    }

    public void method(String str,StringBuilder sb){
        str += "xxxxxxxxxxx";
        sb = new StringBuilder();
        sb.append("xxxxxxxxxxx");
    }
}

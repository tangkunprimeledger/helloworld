package com.higgs.trust.evmcontract;

import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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

        List list = new ArrayList<>();

        list.add(BigInteger.valueOf(20L));
        list.add("1111");

        list.add("2222".getBytes());
        System.out.println(list);

    }
}

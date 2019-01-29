package com.higgs.trust.evmcontract;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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

    public void method(String str,StringBuilder sb){
        str += "xxxxxxxxxxx";
        sb = new StringBuilder();
        sb.append("xxxxxxxxxxx");

    }

    @Test
    public void passByValue(){
        String str = null;
        StringBuilder sb = new StringBuilder();
        method(str,sb);

        System.out.println(str);
        System.out.println("sb"+sb.toString());
    }

    /**
     * test ExecutorService
     */
    @Test
    public void testExecutorService(){

        ExecutorService pool = new ThreadPoolExecutor(1,
                4,
                100L,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(),
                new ThreadFactoryBuilder().setNameFormat("com.higgsblock.trie-calc-thread-%d").build(),
                new ThreadPoolExecutor.AbortPolicy());

        pool.execute(() -> System.out.print(Thread.currentThread().getName()));
        pool.shutdown();
    }
}

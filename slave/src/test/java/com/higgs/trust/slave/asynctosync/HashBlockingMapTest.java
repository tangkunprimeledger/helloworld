package com.higgs.trust.slave.asynctosync;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class HashBlockingMapTest {

    @Test public void testPut() throws Exception {
    }

    @Test public void testTake() throws Exception {
    }

    @Test public void testPoll() throws Exception {
    }

    public static void main(String[] args) throws InterruptedException {
//        BlockingMap<String> hashBlockingMap = new HashBlockingMap<String>();
//
//        hashBlockingMap.put("123", "test");
//
//        long start = System.currentTimeMillis();
//        String str =  hashBlockingMap.poll("1234", 1);
//
//        System.out.println(System.currentTimeMillis() - start);
//        System.out.println(str);
//
////        System.out.println(hashBlockingMap.poll("123", 2000));
//
        int i = 4;
        while (true) {
            switch (i) {
                case 0:
                    System.out.println(0);
                    i++;
                    break;
                case 1:
                    System.out.println(1);
                    i++;
                    break;
                case 2:
                    System.out.println(2);
                    i++;
                    break;
                case 3:
                    System.out.println(3 + "out");
                    i++;
                    return;
                case 4:
                    System.out.println(4);
                    i++;
                    break;
                case 5:
                    System.out.println(5);
                    i++;
                    return;
                case 6:
                    System.out.println(6);
                    i++;
                    break;
                default:
                    return;
            }
        }

    }
}
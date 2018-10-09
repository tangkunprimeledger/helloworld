package com.higgs.trust.network.utils;

import java.util.UUID;

/**
 * @author duhongming
 * @date 2018/9/5
 */
public class RandomUtil {
    public static int randomInt(int max) {
        return (int) Math.round(Math.random() * max);
    }

    public static long randomLong(long max) {
        return Math.round(Math.random() * max);
    }

    public static double randomDouble(long max) {
        return Math.random() * max;
    }

    public static String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}

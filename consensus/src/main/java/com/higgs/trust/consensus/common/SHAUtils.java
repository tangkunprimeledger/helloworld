package com.higgs.trust.consensus.common;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class SHAUtils {
    private static HashFunction function = Hashing.sha256();

    public static String sha256(String src){
        return function.hashString(src, Charsets.UTF_8).toString();
    }
}

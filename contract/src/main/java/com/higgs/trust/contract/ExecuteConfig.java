package com.higgs.trust.contract;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author duhongming
 * @date 2018/6/11
 */
public class ExecuteConfig {

    private final Set<String> allowedClasses;
    private int instructionCountQuota = 100000;

    public static boolean DEBUG = false;

    public ExecuteConfig() {
        allowedClasses = new HashSet<>();
    }

    public ExecuteConfig allow(final String fullClassName) {
        if (StringUtils.isEmpty(fullClassName)) {
            throw new IllegalArgumentException("fullClassName");
        }
        allowedClasses.add(fullClassName);
        return this;
    }

    public ExecuteConfig allow(final Class clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz");
        }
        this.allow(clazz.getName());
        return this;
    }

    public  Set<String> getAllowedClasses() {
        return allowedClasses;
    }

    /**
     * get the instruction count quota
     *
     * @return
     */
    public int getInstructionCountQuota() {
        return instructionCountQuota;
    }

    /**
     * set the instruction count quota when contract executing,
     * throw QuotaExceededException if quota exceeded
     *
     * @param instructionCountQuota
     * @return
     */
    public ExecuteConfig setInstructionCountQuota(final int instructionCountQuota) {
        this.instructionCountQuota = instructionCountQuota;
        return this;
    }
}
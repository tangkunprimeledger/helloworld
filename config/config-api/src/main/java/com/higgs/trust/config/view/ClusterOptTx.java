/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author suimi
 * @date 2018/9/4
 */
public class ClusterOptTx {

    private String nodeName;

    private String pubKey;

    private String selfSign;

    private Operation operation;

    private List<SignatureInfo> signatureList;

    public String getSelfSignValue() {
        return "";
    }

    public String getSignatureValue() {
        return "";
    }

    public enum Operation {
        JOIN, LEAVE
    }

    @Data @AllArgsConstructor @NoArgsConstructor public static class SignatureInfo {
        private String signer;
        private String sign;
    }

}

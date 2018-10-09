/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.testng.annotations.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author suimi
 * @date 2018/9/7
 */
@Slf4j public class TrustPubliceKeyConfigGenertor {

    @Test public void genertor() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        String[] nodeNames = new String[] {"TRUST-NODEA", "TRUST-NODEB", "TRUST-NODEC", "TRUST-NODED","TRUST-NODEE"};
        List<Node> nodes = new ArrayList<>();
        Arrays.stream(nodeNames).forEach(nodeName -> {
            KeyPair bizKey = keyPairGen.generateKeyPair();
            KeyPair conKey = keyPairGen.generateKeyPair();
            nodes.add(Node.builder().name(nodeName).bizKey(bizKey).conKey(conKey).build());
            System.out.println("\n===========" + nodeName + "==========");
            System.out.println("higgs.trust.keys.bizPublicKey=" + Base64.encodeBase64String(bizKey.getPublic().getEncoded()));
            System.out.println("higgs.trust.keys.bizPrivateKey=" + Base64.encodeBase64String(bizKey.getPrivate().getEncoded()));
            System.out.println("higgs.trust.keys.consensusPublicKey=" + Base64.encodeBase64String(conKey.getPublic().getEncoded()));
            System.out.println("higgs.trust.keys.consensusPrivateKey=" + Base64.encodeBase64String(conKey.getPrivate().getEncoded()));
        });
        buildGeniusBlock(nodes);
    }

    private void buildGeniusBlock(List<Node> nodes) {
        List<Action> actions = new ArrayList<>();
        nodes.forEach(node -> {
            List<Key> keys = new ArrayList<>();
            keys.add(Key.builder().type("biz")
                .publicKey(Base64.encodeBase64String(node.getBizKey().getPublic().getEncoded()))
                .build());
            keys.add(Key.builder().type("consensus")
                .publicKey(Base64.encodeBase64String(node.getConKey().getPublic().getEncoded()))
                .build());
            actions.add(Action.builder().nodeName(node.name).keys(keys).build());
        });

        GeniusBlock geniusBlock = GeniusBlock.builder()
            .height(1)
            .version(1)
            .previousHash("0000000000000000000000000000000000000000000000000000000000000000")
            .numberOfTransactions(1)
            .transactions(Collections.singletonList(Transaction.builder().actions(actions).build())).build();
        String jsonString = JSON.toJSONString(geniusBlock, true);

        System.out.println("\n===========geniusBlock========\n");
        System.out.println(jsonString);

    }

    @Data @Builder public static class Node {
        private String name;
        private KeyPair bizKey;
        private KeyPair conKey;
    }
    @Builder@Data
    public static class GeniusBlock{
        private long height = 1;
        private int version = 1;
        private String previousHash = "0000000000000000000000000000000000000000000000000000000000000000";
        private int numberOfTransactions = 1;
        private List<Transaction> transactions;

    }

    @Builder@Data
    public static class Transaction{
        private List<Action> actions;
    }

    @Builder@Data
    public static class Action{
        private String nodeName;
        private List<Key> keys;
    }

    @Builder@Data
    public static class Key{
        private String publicKey;
        private String type;
    }
}

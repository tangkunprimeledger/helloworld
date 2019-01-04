package com.higgs.trust.rs.tx.multinodes;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.SignedTransaction;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;

/**
 * @author Chen Jiawei
 * @date 2019-01-03
 */
public class InvokeTransactionSender {
    public static void main(String[] args) {
        List<ISignedTransactionPoster> posters = TransactionSender.POSTERS;
        String senderAddress = TransactionSender.SENDER_ADDRESS;
        TransactionBuilder builder = new TransactionBuilder();
        SecureRandom random = new SecureRandom();
        String contractReceiverAddress;
        ISignedTransactionPoster poster;

        String transactionSenderId = "TRUST-TEST0";
        String contractMethodSignature = "() addOne()";
        Object[] methodArgs = new Object[0];

        try {
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                contractReceiverAddress = TransactionSender.CONTRACT_ADDRESSES.get(random.nextInt(TransactionSender.CONTRACT_ADDRESSES.size()));
                poster = posters.get(random.nextInt(posters.size()));
                SignedTransaction signedTransaction = builder.generateSignedTransactionWithContractInvocation(
                        senderAddress, contractReceiverAddress, transactionSenderId, contractMethodSignature, methodArgs);
                System.out.println(JSON.toJSONString(signedTransaction, true));
                RespData respData = poster.post(signedTransaction).execute().body();
                System.out.println(respData.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

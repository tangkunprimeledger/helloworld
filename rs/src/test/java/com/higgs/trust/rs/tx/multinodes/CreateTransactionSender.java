package com.higgs.trust.rs.tx.multinodes;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.contract.ContractCreationV2Action;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.List;

/**
 * @author Chen Jiawei
 * @date 2019-01-03
 */
public class CreateTransactionSender {
    public static void main(String[] args) {
        List<ISignedTransactionPoster> posters = TransactionSender.POSTERS;
        String senderAddress = TransactionSender.SENDER_ADDRESS;
        TransactionBuilder builder = new TransactionBuilder();
        SecureRandom random = new SecureRandom();
        ISignedTransactionPoster poster;

        String transactionSenderId = "TRUST-TEST0";
        String contractFileAbsolutePath = Paths.get("rs/src/test/resources/contracts/DataWrapper.sol").toFile().getAbsolutePath();
        String contractName = "DataWrapper";
        String constructorSignature = "DataWrapper()";
        Object[] constructorArgs = new Object[0];

        try {
            for (int i = 0; i < 10; i++) {
                poster = posters.get(random.nextInt(posters.size()));
                SignedTransaction signedTransaction = builder.generateSignedTransactionWithContractCreation(
                        senderAddress, transactionSenderId, contractFileAbsolutePath,
                        contractName, constructorSignature, constructorArgs);
                TransactionSender.setContractAddress(((ContractCreationV2Action) signedTransaction.getCoreTx().getActionList().get(0)).getTo());
                System.out.println(JSON.toJSONString(signedTransaction, true));
                RespData respData = poster.post(signedTransaction).execute().body();
                System.out.println(respData.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

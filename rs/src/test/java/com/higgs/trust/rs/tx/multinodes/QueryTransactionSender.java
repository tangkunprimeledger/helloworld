package com.higgs.trust.rs.tx.multinodes;

import com.higgs.trust.rs.core.bo.ContractQueryRequestV2;
import com.higgs.trust.slave.api.vo.RespData;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;

/**
 * @author Chen Jiawei
 * @date 2019-01-04
 */
public class QueryTransactionSender {
    public static void main(String[] args) {
        List<ISignedTransactionPoster> posters = TransactionSender.POSTERS;
        SecureRandom random = new SecureRandom();
        String contractReceiverAddress;
        ISignedTransactionPoster poster;

        try {
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                ContractQueryRequestV2 contractQueryRequestV2 = new ContractQueryRequestV2();
                contractQueryRequestV2.setBlockHeight(-1L);
                contractQueryRequestV2.setMethodSignature("(uint256) get()");
                contractQueryRequestV2.setParameters(new Object[0]);
                contractReceiverAddress = TransactionSender.CONTRACT_ADDRESSES.get(random.nextInt(TransactionSender.CONTRACT_ADDRESSES.size()));
                contractQueryRequestV2.setAddress(contractReceiverAddress);

                poster = posters.get(random.nextInt(posters.size()));
                RespData respData = poster.post(contractQueryRequestV2).execute().body();
                System.out.println(respData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}

package com.higgs.trust.slave.core.service.contract;

import com.google.common.collect.Lists;
import com.higgs.trust.common.crypto.KeyPair;
import com.higgs.trust.common.utils.CryptoUtil;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.model.bo.utxo.Sign;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.List;

/**
 * UTXOContextService test
 *
 * @author lingchao
 * @create 2018年09月03日14:50
 */
public class UTXOContextServiceTest extends BaseTest {

    @Autowired
    private UTXOContextService utxoContextService;

    @Test
    public void verifySignature() {
        String masssage = "lingchao";
        List<Sign> signList = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            Sign sign = new Sign();
            KeyPair keyPair = CryptoUtil.getBizCrypto().generateKeyPair();
            sign.setPubKey(keyPair.getPubKey());
            sign.setSignature(CryptoUtil.getBizCrypto().sign(masssage, keyPair.getPriKey()));
            signList.add(sign);
        }

        System.out.println("verify result: "+ utxoContextService.verifySignature(signList, masssage));

    }

}

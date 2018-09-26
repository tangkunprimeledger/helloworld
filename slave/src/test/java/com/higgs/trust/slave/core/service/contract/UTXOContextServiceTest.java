package com.higgs.trust.slave.core.service.contract;

import com.google.common.collect.Lists;
import com.higgs.trust.common.crypto.KeyPair;
import com.higgs.trust.common.enums.CryptoTypeEnum;
import com.higgs.trust.config.crypto.CryptoUtil;
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

        List<Sign> signListSM = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            Sign sign = new Sign();
            KeyPair keyPair = CryptoUtil.getBizCrypto(CryptoTypeEnum.SM.getCode()).generateKeyPair();
            sign.setPubKey(keyPair.getPubKey());
            sign.setSignature(CryptoUtil.getBizCrypto(CryptoTypeEnum.SM.getCode()).sign(masssage, keyPair.getPriKey()));
            sign.setCryptoType(CryptoTypeEnum.SM.getCode());
            signListSM.add(sign);
        }

        System.out.println("verify signListSM result: "+ utxoContextService.verifySignature(signListSM, masssage));

        List<Sign> signListRSA = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            Sign sign = new Sign();
            KeyPair keyPair = CryptoUtil.getBizCrypto(CryptoTypeEnum.RSA.getCode()).generateKeyPair();
            sign.setPubKey(keyPair.getPubKey());
            sign.setSignature(CryptoUtil.getBizCrypto(CryptoTypeEnum.RSA.getCode()).sign(masssage, keyPair.getPriKey()));
            sign.setCryptoType(CryptoTypeEnum.RSA.getCode());
            signListRSA.add(sign);
        }
        System.out.println("verify signListRSA result: "+ utxoContextService.verifySignature(signListRSA, masssage));

        List<Sign> signListECC = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            Sign sign = new Sign();
            KeyPair keyPair = CryptoUtil.getBizCrypto(CryptoTypeEnum.ECC.getCode()).generateKeyPair();
            sign.setPubKey(keyPair.getPubKey());
            sign.setSignature(CryptoUtil.getBizCrypto(CryptoTypeEnum.ECC.getCode()).sign(masssage, keyPair.getPriKey()));
            sign.setCryptoType(CryptoTypeEnum.ECC.getCode());
            signListECC.add(sign);
        }
        System.out.println("verify signListECC result: "+ utxoContextService.verifySignature(signListECC, masssage));
    }

}

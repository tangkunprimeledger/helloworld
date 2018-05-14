package com.higgs.trust.rs.custom.biz.api.impl.mm;

import cn.primeledger.pl.crypto.CryptoUtils;
import cn.primeledger.pl.crypto.ECKey;
import cn.primeledger.pl.wallet.dock.biz.CasCrypto;
import cn.primeledger.pl.wallet.dock.biz.impl.CasCryptoImpl;
import cn.primeledger.pl.wallet.dock.bo.CasDecryptReponse;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.rs.custom.model.bo.identity.Identity;
import com.higgs.trust.rs.custom.model.bo.identity.IdentityRequest;
import org.junit.Test;

import java.util.UUID;

/*
 * @desc TODO
 * @author WangQuanzhou
 * @date 2018/2/19 16:04
 */
public class TestCrypto {

    public static final String pubKey = "0299ab5170051e5d8568543709348e7804110f126c45b305042c1e964c327d728e";
    public static final String priKey = "f37a12485f23960ddd922b9ced5467806cbb393ef24660305ddd0abfdf987c72";
    public static final String aesKey = "cbs+jcajndcwje-9";

    public static final String LOCALURL= "localhost";
    //    public static final String REMOTEURL= "10.200.172.221";
    public static final String REMOTEURL= "47.92.181.78";


    @Test
    public void testStorageIdentity() {

        IdentityRequest identityRequestBO = new IdentityRequest();
        for (int i = 0;i<1;i++)
        {
            identityRequestBO.setReqNo(UUID.randomUUID().toString());
            //            identityRequestBO.setReqNo("2222");
            //            identityRequestBO.setKey(UUID.randomUUID().toString());
            identityRequestBO.setKey("120");
            identityRequestBO.setValue(UUID.randomUUID().toString());
            identityRequestBO.setFlag("000");

            CasCrypto casCrypto = new CasCryptoImpl();
            String url = "http://localhost:7070/v1/identity/storage";
            //    String url = "http://"+REMOTEURL+":7070/v1/identity/storage";
            CasDecryptReponse casDecryptReponse = casCrypto.execute(identityRequestBO, priKey, pubKey, aesKey, url);
            System.out.println("请求返回数据对象：" + casDecryptReponse);
        }

    }


    @Test
    public void testQueryByKey() {

        Identity identityBO = new Identity();
        CasCrypto casCrypto = new CasCryptoImpl();
        //        identityBO.setKey("cf65377b-5fab-4e49-b895-36ee0e6e46b4");
        identityBO.setKey("2");
        //        String url = "http://localhost:7070/v1/identity/query/key";
        String url = "http://"+REMOTEURL+":7070/v1/identity/query/key";
        CasDecryptReponse casDecryptReponse = casCrypto.execute(identityBO, priKey, pubKey, aesKey, url);
        System.out.println("请求返回数据对象：" + casDecryptReponse);
    }


    @Test
    public void testQueryByReqNo() {

        Identity identityBO = new Identity();
        CasCrypto casCrypto = new CasCryptoImpl();
        //        identityBO.setReqNo("5cd0e4ad-fc2a-4d9c-be76-f90a819b67e6");
        identityBO.setReqNo("c1bd4a62-f79b-48ea-a126-b32dbd04265e");
        //        String url = "http://localhost:7070/v1/identity/query/reqNo";
        String url = "http://"+REMOTEURL+":7070/v1/identity/query/reqNo";
        CasDecryptReponse casDecryptReponse = casCrypto.execute(identityBO, priKey, pubKey, aesKey, url);
        System.out.println("请求返回数据对象：" + casDecryptReponse);
    }

    @Test
    public void testPubkey() {

        CasCrypto casCrypto = new CasCryptoImpl();
        String url = "http://"+REMOTEURL+":7070/status.html";
        CasDecryptReponse casDecryptReponse = casCrypto.execute(new JSONObject(), priKey, pubKey, aesKey, url);
        System.out.println("请求返回数据对象：" + casDecryptReponse);
    }

    @Test
    public void generateKeys(){
        for (int i=0;i<5;i++){
            ECKey ecKey = new ECKey();
            System.out.println("私钥："+ecKey.getPrivKey());
            System.out.println("公钥："+ecKey.getPubKey());
        }
    }

    @Test public void kyyyk() {
        ECKey priEcKey = ECKey.fromPrivate(CryptoUtils.HEX.decode(priKey));
        System.out.println(priEcKey.getPrivateKeyAsHex());
    }



}

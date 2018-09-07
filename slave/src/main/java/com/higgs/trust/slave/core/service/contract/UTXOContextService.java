package com.higgs.trust.slave.core.service.contract;

import com.higgs.trust.config.crypto.CryptoUtil;
import com.higgs.trust.contract.ContractApiService;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.service.datahandler.utxo.UTXOSnapshotHandler;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.Sign;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.UTXO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import com.higgs.trust.zkproof.EncryptAmount;

@Slf4j
@Service
public class UTXOContextService extends ContractApiService {

    @Autowired
    private UTXOSnapshotHandler utxoSnapshotHandler;


    public UTXOAction getAction() {
        return getContextData(UTXOExecuteContextData.class).getAction();
    }

    /**
     * get utxo action type
     *
     * @param name
     * @return
     */
    public UTXOActionTypeEnum getUTXOActionType(String name) {
        return UTXOActionTypeEnum.getUTXOActionTypeEnumByName(name);
    }

    /**
     * query UTXO list
     *
     * @param inputList
     * @return
     */
    public List<UTXO> queryUTXOList(List<TxIn> inputList) {
        log.info("When process UTXO contract  querying queryTxOutList by inputList:{}", inputList);
        return utxoSnapshotHandler.queryUTXOList(inputList);
    }

    /**
     * add two big decimal data
     * usually used by add  amount
     * return  augend + addend
     *
     * @param addend
     * @param augend
     * @return
     */
    public static BigDecimal add(String augend, String addend) {
        BigDecimal a = null;
        BigDecimal b = null;
        try {
            a = new BigDecimal(augend);
            b = new BigDecimal(addend);
        } catch (Throwable e) {
            log.error("UTXO context NumberFormatException in for method add, when execute contract");
            throw new SlaveException(SlaveErrorEnum.SLAVE_UTXO_CONTRACT_PROCESS_FAIL_ERROR);
        }
        return a.add(b);
    }

    public static void initPubKey(String pubKey) {
        //String pubKey = "{\"P\":\"26zbXL2Zud9xQhBRCcUwfe2wteD8rfZSAJu4UtLy2KJehKvPr25GU6hkUamTWbMcVufJbfjbMyJJrpNaZyDgNw5T72qGCB3p98PdHryamsU2qxZuoPzTQD9ZgmpdVe9aRwQ6vgdtSf6aKVhyG6fZ5kkz13jewS2HcGjjTbYjfFUyJARpRzUr4v9NvyjamL5h5eQUM6QhsVRULGKVZwrqvFeMcK42dMLNKAgCfnEBx2stKeoybbPUn5NpH3wRfUhEZr6fjXSg97aRsW8VHoMahMbKjdA8txynA8yWcLwEHRbYBauX4cVc4bjxNaLEy4wx5Zkf1Mh3w7y14zBqxzJMJWfwMtJXonyvzvm\",\"Q\":\"22k9HfiJDXX2pvKGSdsp2SAwGwHgDucEQAcgHTQg9tt4bZson3CnWairvhSc3NCU9ZBC5xmED8D1n9ZbjadgGunTaya7W4qFXFAT4icsKvCHGF7oFy71MDUGBSiVXuWt79QpaVv9p4ugFgdqWKgKpyKamgMYBCysVaMDneiXpJzxdf54GspNi7gfvm6ZKjfqdPB6BphRkpBNhub2yfT4hz9bfzZZ3xMg2hvvHXS8rGyZqyRZGe8WLoDRhULmzte314Sqx2NnM87Ywg6C6FqG7zfQ3gQqsMhdqSsjbMM2CWLde4f8GmZVrgKj1N7v25L1nJ5kochN3Wp4tiqKRxW8JAR8mbSHHLQa3wP\",\"key_type\":\"BGN\",\"param\":\"27wfRdp8w7fJyycReKGTjSTaAZRVCAAUUJDxyNMhzn4mvqhfmtt1vn4F7diood6xmTw3NpAaavEUn1AXweYAsMsPn1C9apxm5tjD9fn7pGnBoxkos8RmhTRGuyc9Ax39mennDeWBu9aqpYDoh9quh2GesQbcg2HkgMpfZDChXXJNPbhUzsjinFYxiWSy6yraUGwUyD59VsxP4JJ33v7t1e8wzY4CcQaWjBjjV5YmWJExFVGXinfZ5j55mqkCr4fAvzxjAmS9wcnr9VMskRJ1FELXYTarq9j4BSBswvNEafPKHsqSZedY8obJ7bKyomx1HmBoxeHNnR4X4KvMF4EXa9fz1PrGNfVj62yM2t7QyrYr2hXbS7ZHgmE1WJh4C12GX4xXwpaVkjwWdhpAVUpsq6vwMjqcxkfm5ZbCkzLMA3GGt4fvzr8byHNZNSCxsMBUhZkJNs3m6BbCNgmRhvtDJUBM4t5fRknx8Vtt1G2Vm9JYqVdcnef6pLhpGhtVf2iBi7djJJi6WSQdBuGWDwTPbtu8gskqCGo6xPXiGV9LiCUaCwUByxZfNcRA3JvtbtMmHF5TFHHrVv8Q2SoteWDpUPkAz85k5r3FX5Ex1o581JeToK8qUNcwu6pFtbV5P7yBWUgwh1UoJuKQvQUGDc1VUDtjMssjbuA49UHfLDxQZvcj6kJ9mBfhHqEeLNX73H4AzENR7hJkUYHeeiswX5SeFL56SS4kUYA7pk1cR323m6McyfDD5zpjKLSRCtrGJPqnPAMVS3FPKtJUrsT7k5iPvB2Mz3H9ztZZaQb56b45kA1jRPCb37Q1QMUoHRiN5YYHPhHeUiAR29drA9o4MErdK9PrEA9prKDu46NxvGjCb5GSsgxDuP758dF5NTmtNata899WaZeoXSrkxhqyPJW8x7eNUw7VmXfETax1ne3c7C34TMw1iYfccC7CTXkGCuF4BYBHybLM4F8VWmvPi2rzWFwP1xLLK5FBfbQfVAJD7dMACP\",\"n\":\"1EyhB6AWZ7DFfdKv6yQoRZNDmehNRx4aUixGA2PZnu43bEBD76Xxorvm2aDAskmBr5zz3V4xccZsc1GWtMRo8UnefAgNUNQtmLFcjXboVmAsWNaMxhFfufivvXRpDPNnLEbNtUC66YPz52rKtcJd9zxdf2G1mYaeKrTcubdYL8VSgEtU\"}";
        EncryptAmount.setHomomorphicEncryptionKey(pubKey);
    }

    public static String cipherAdd (String em1, String em2){
        if (em1.equals("0")){
            return em2;
        }
        return EncryptAmount.cipherAdd(em1, em2);
    }

    /**
     * subtract two data
     * usually used by subtract  amount
     * return  minuend -  reduction
     *
     * @param minuend
     * @param reduction
     * @return
     */
    public BigDecimal subtract(String minuend, String reduction) {
        BigDecimal a = null;
        BigDecimal b = null;
        try {
            a = new BigDecimal(minuend);
            b = new BigDecimal(reduction);
        } catch (Throwable e) {
            log.error("UTXO context NumberFormatException in for method subtract, when execute contract");
            throw new SlaveException(SlaveErrorEnum.SLAVE_UTXO_CONTRACT_PROCESS_FAIL_ERROR);
        }
        return a.subtract(b);
    }

    /**
     * compare two big decimal
     * if a > b  then return 1
     * if a == b then return 0
     * if a < b then return -1
     *
     * @param a
     * @param b
     * @return
     */
    public int compare(String a, String b) {
        BigDecimal a0 = null;
        BigDecimal b0 = null;
        try {
            a0 = new BigDecimal(a);
            b0 = new BigDecimal(b);
        } catch (Throwable e) {
            log.error("UTXO context NumberFormatException in for method compare, when execute contract");
            throw new SlaveException(SlaveErrorEnum.SLAVE_UTXO_CONTRACT_PROCESS_FAIL_ERROR);
        }
        return a0.compareTo(b0);
    }


    public int cipherCompare (String a, String b){
        if (EncryptAmount.cipherCompare(a, b)){
            return 0;
        }
        return 1;
    }


    /**
     * verify UTXO Signature list
     * all the Signature is sign from the same message with different private key
     *
     * @param signList
     * @param message
     * @return
     */
    public boolean verifySignature(List<Sign> signList, String message) {
        if (CollectionUtils.isEmpty(signList) || null == message) {
            log.error("Verify UTXO Signature list for signList or message is null error!");
            return false;
        }
        for (Sign sign : signList) {
            if (StringUtils.isBlank(sign.getPubKey()) || StringUtils.isBlank(sign.getSignature())) {
                log.error("UTXO sign info :{} for PubKey or Signature is null error!", sign);
                return false;
            }
            if (!CryptoUtil.getBizCrypto().verify(message, sign.getSignature(), sign.getPubKey())) {
                log.error("UTXO verify message :{} for Signature :{} with pubKey :{}  failed error!", message, sign.getSignature(), sign.getPubKey());
                return false;
            }
        }
        return true;
    }
}


package com.higgs.trust.slave.model.bo.utxo;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.validator.constraints.NotBlank;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sign info for  UTXO
 * every signature is for the same message with different priKey
 * @author lingchao
 * @create 2018年09月03日10:41
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Sign  extends BaseBO{

    /**
     * public key for the signature
     */
    @NotBlank
    private String pubKey;

    /**
     * signature  for the UTXO  self define  message
     */
    @NotBlank
    private  String signature;

    /**
     * crypto type RSA, SM, ECC
     */
    @NotBlank
    private String cryptoType;

    /**
     * make map of sign data
     * key:pubKey
     * value:sign
     * @param signList
     * @return
     */
    public static Map<String, String> makeSignMap(List<Sign> signList) {
        if (CollectionUtils.isEmpty(signList)) {
            return new HashMap<>();
        }
        return signList.stream().collect(Collectors.toMap(Sign::getPubKey, Sign::getSignature));
    }
}

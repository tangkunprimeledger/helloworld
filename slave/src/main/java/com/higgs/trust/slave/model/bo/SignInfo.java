package com.higgs.trust.slave.model.bo;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author liuyu
 * @description
 * @date 2018-06-06
 */
@Getter @Setter public class SignInfo extends BaseBO {
    /**
     * who`s sign,rs-name
     */
    private String owner;
    /**
     * the sign data
     */
    private String sign;

    /**
     * make map of sign data
     * key:owner
     * value:sign
     *
     * @param signInfos
     * @return
     */
    public static Map<String, String> makeSignMap(List<SignInfo> signInfos) {
        if (CollectionUtils.isEmpty(signInfos)) {
            return new HashMap<>();
        }
        return signInfos.stream().collect(Collectors.toMap(SignInfo::getOwner, SignInfo::getSign));
    }
}

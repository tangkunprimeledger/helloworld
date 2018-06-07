package com.higgs.trust.slave.model.bo.manage;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author tangfashuang
 * @date 2018/04/12 18:24
 * @desc rs pubKey BO
 */
@Getter
@Setter
@NoArgsConstructor
public class RsPubKey extends BaseBO {
    /**
     * rs id
     */
    private String rsId;

    /**
     * public key
     */
    private String pubKey;
}

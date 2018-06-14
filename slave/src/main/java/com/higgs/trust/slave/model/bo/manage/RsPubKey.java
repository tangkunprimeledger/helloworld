package com.higgs.trust.slave.model.bo.manage;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

/**
 * @author tangfashuang
 *
 */
@Getter
@Setter
public class RsPubKey extends BaseBO{
    /**
     * rs id
     */
    private String rsId;

    /**
     * public key
     */
    private String pubKey;
}

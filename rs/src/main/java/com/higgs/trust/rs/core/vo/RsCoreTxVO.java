package com.higgs.trust.rs.core.vo;

import com.higgs.trust.slave.model.bo.CoreTransaction;
import lombok.Getter;
import lombok.Setter;

/**
 * @author liuyu
 * @description
 * @date 2018-06-22
 */
@Getter @Setter public class RsCoreTxVO extends CoreTransaction {
    /**
     * 是否等待集群共识完成
     */
    private boolean forEnd;
}

package com.higgs.trust.rs.core.task;

import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author liuyu
 * @description
 * @date 2018-09-18
 */
@Getter
@Setter
@AllArgsConstructor
public class TxIdBO {
    private String txId;
    private CoreTxStatusEnum statusEnum;
}

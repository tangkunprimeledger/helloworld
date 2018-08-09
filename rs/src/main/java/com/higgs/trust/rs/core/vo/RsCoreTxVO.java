package com.higgs.trust.rs.core.vo;

import com.higgs.trust.rs.core.api.enums.CoreTxResultEnum;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.bo.CoreTxBO;
import lombok.Getter;
import lombok.Setter;

/**
 * @author liuyu
 * @description
 * @date 2018-06-26
 */
@Setter @Getter public class RsCoreTxVO extends CoreTxBO {
    /**
     * process status
     */
    private CoreTxStatusEnum status;
    /**
     * the tx execute result 0:fail 1:success
     */
    private CoreTxResultEnum executeResult;
    /**
     * execute error code
     */
    private String errorCode;
    /**
     * execute error msg
     */
    private String errorMsg;
}

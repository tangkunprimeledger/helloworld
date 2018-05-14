package com.higgs.trust.rs.custom.model.bo;

import com.higgs.trust.rs.common.BaseBO;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter public class BankChainRequest extends BaseBO {

    private static final long serialVersionUID = -6584282315308460388L;

    private String reqNo;//   请求唯一标识
    private String bizType;//   请求业务类型  目前只有  STORAGE_IDENTITY-存证
    private String status;//   请求状态：INIT-初始态  PROCESSING-处理中   SUCCESS-处理成功
    private String respCode;//   响应码：对应 RespCodeEnum
    private String respMsg;//   响应信息

}



package com.higgs.trust.rs.custom.util.converter;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.rs.custom.api.enums.RequestEnum;
import com.higgs.trust.rs.custom.api.enums.RespCodeEnum;
import com.higgs.trust.rs.core.dao.po.RequestPO;
import com.higgs.trust.rs.custom.vo.BillCreateVO;
import com.higgs.trust.rs.custom.vo.BillTransferVO;

/**
 * Request convertor
 *
 * @author lingchao
 * @create 2018年05月13日17:04
 */
public class RequestConvertor {
    /**
     * build RequestPO
     * @param billCreateVO
     * @return
     */
    public static RequestPO buildRequestPO(BillCreateVO billCreateVO) {
        RequestPO requestPO = new RequestPO();
        requestPO.setRequestId(billCreateVO.getRequestId());
        requestPO.setData(JSON.toJSONString(billCreateVO));
        requestPO.setRespCode(RespCodeEnum.CREATE_BILL_PROCESS.getRespCode());
        requestPO.setRespMsg(RespCodeEnum.CREATE_BILL_PROCESS.getMsg());
        requestPO.setStatus(RequestEnum.PROCESS.getCode());
        return requestPO;
    }

    /**
     * build RequestPO
     * @param billTransferVO
     * @return
     */
    public static RequestPO buildRequestPO(BillTransferVO billTransferVO) {
        RequestPO requestPO = new RequestPO();
        requestPO.setRequestId(billTransferVO.getRequestId());
        requestPO.setData(JSON.toJSONString(billTransferVO));
        requestPO.setRespCode(RespCodeEnum.TRANSFER_BILL_PROCESS.getRespCode());
        requestPO.setRespMsg(RespCodeEnum.TRANSFER_BILL_PROCESS.getMsg());
        requestPO.setStatus(RequestEnum.PROCESS.getCode());
        return requestPO;
    }
}

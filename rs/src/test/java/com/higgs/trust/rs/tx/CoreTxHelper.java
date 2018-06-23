package com.higgs.trust.rs.tx;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.higgs.trust.common.utils.OkHttpClientManager;
import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.model.bo.action.Action;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-06-22
 */
@Slf4j public class CoreTxHelper {
    /**
     * 交易提交接口地址
     */
    public static String TX_URL = "http://127.0.0.1:7070/submitTx";
    /**
     * 交易发起方，需跟配置一致，否则RS回调时可能会有问题
     */
    public static String SENDER = "TRUST-TEST1";

    static {
        //JSON auto detect class type
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        //JSON不做循环引用检测
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.DisableCircularReferenceDetect.getMask();
        //JSON输出NULL属性
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteMapNullValue.getMask();
        //toJSONString的时候对一级key进行按照字母排序
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.SortField.getMask();
        //toJSONString的时候对嵌套结果进行按照字母排序
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.MapSortField.getMask();
        //toJSONString的时候记录Class的name
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteClassName.getMask();
    }

    /**
     * 构建 基本 交易对象
     *
     * @param txId
     * @param actions
     * @return
     */
    public static RsCoreTxVO makeSimpleTx(String txId, List<Action> actions) {
        return makeSimpleTx(txId, InitPolicyEnum.NA.getPolicyId(), actions, SENDER);
    }

    /**
     * 构建 基本 交易对象
     *
     * @param txId
     * @param policyId
     * @param actions
     * @param sender
     * @return
     */
    public static RsCoreTxVO makeSimpleTx(String txId, String policyId, List<Action> actions, String sender) {
        RsCoreTxVO vo = new RsCoreTxVO();
        vo.setTxId(txId);
        vo.setPolicyId(policyId);
        vo.setSender(sender);
        vo.setVersion(VersionEnum.V1.getCode());
        vo.setLockTime(null);
        vo.setBizModel(null);
        vo.setSendTime(new Date());
        vo.setActionList(actions);
        return vo;
    }

    /**
     * 发送交易请求
     *
     * @param tx
     */
    public static void post(RsCoreTxVO tx) {
        String requestJSON = JSON.toJSONString(tx);
        log.info("[post]requestJSON:{}", requestJSON);
        try {
            String resultString = OkHttpClientManager.postAsString(TX_URL, requestJSON, 10000L);
            JSONObject respData = JSON.parseObject(resultString);
            if (!StringUtils.equals(respData.getString("respCode"), "000000")) {
                log.error("[post]request has error:{}", respData);
                throw new RuntimeException("request has error " + respData);
            }
            log.info("[post]respCode:{}", respData.getString("respCode"));
            log.info("[post]msg:{}", respData.getString("msg"));
        } catch (Throwable t) {
            log.error("[post] has error", t);
        }
    }
}

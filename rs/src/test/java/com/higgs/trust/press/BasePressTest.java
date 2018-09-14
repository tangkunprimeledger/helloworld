package com.higgs.trust.press;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.OkHttpClientManager;
import com.higgs.trust.slave.api.vo.RespData;

/**
 * @author liuyu
 * @description
 * @date 2018-09-11
 */
public class BasePressTest {
//    private static String BASE_URL = "http://10.200.173.194:7071/";
    private static String BASE_URL = "http://10.200.172.98:7070/";
    /**
     INSERT INTO `biz_type` (`policy_id`, `biz_type`, `create_time`) VALUES ('CREATE_CURRENCY', 'CREATE_CURRENCY', '2018-6-21 15:15:25');
     INSERT INTO `biz_type` (`policy_id`, `biz_type`, `create_time`) VALUES ('OPEN_MY_ACCOUNT', 'OPEN_MY_ACCOUNT', '2018-6-21 15:15:25');
     INSERT INTO `biz_type` (`policy_id`, `biz_type`, `create_time`) VALUES ('ACCOUNTING', 'ACCOUNTING', '2018-6-21 15:15:25');
     INSERT INTO `biz_type` (`policy_id`, `biz_type`, `create_time`) VALUES ('FREEZE', 'FREEZE', '2018-6-21 15:15:25');
     INSERT INTO `biz_type` (`policy_id`, `biz_type`, `create_time`) VALUES ('UNFREEZE', 'UNFREEZE', '2018-6-21 15:15:25');
     **/
    /**
     * send by http post
     *
     * @param api
     * @param params
     */
    public void send(String api, String params) {
        //        System.out.println(("[send]req:" + params));
        try {
            Long startTime = System.currentTimeMillis();
            String resultString = OkHttpClientManager.postAsString(BASE_URL + api, params, 10000L);
            RespData respData = JSON.parseObject(resultString, RespData.class);
            Long endTime = System.currentTimeMillis();
//            System.out.println("[send]resp.code:" + respData.getRespCode());
            System.out.println("[send]used:" + (endTime - startTime) + "ms");
        } catch (Throwable t) {
            System.out.println("[send] has error" + t);
        }
    }
}

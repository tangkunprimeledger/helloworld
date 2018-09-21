package com.higgs.trust.rs.core.integration;

import com.higgs.trust.network.HttpClient;
import com.higgs.trust.network.NetworkManage;
import com.higgs.trust.rs.core.vo.NodeOptVO;
import com.higgs.trust.slave.api.vo.RespData;
import org.springframework.stereotype.Component;

/**
 * @author duhongming
 * @date 2018/9/14
 */
@Component
public class HttpNodeClient implements NodeClient {
    @Override
    public RespData<String> nodeJoin(String nodeName, NodeOptVO vo) {
        String url = "/node/join";
        HttpClient httpClient = NetworkManage.getInstance().httpClient();
        return httpClient.postJson(nodeName, url, vo, RespData.class);
    }
}

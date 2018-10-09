package com.higgs.trust.rs.core.integration;

import com.alibaba.fastjson.TypeReference;
import com.higgs.trust.config.view.IClusterViewManager;
import com.higgs.trust.network.HttpClient;
import com.higgs.trust.network.NetworkManage;
import com.higgs.trust.slave.api.vo.CaVO;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.ca.Ca;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * @author duhongming
 * @date 2018/9/14
 */
@Component
public class HttpCaClient implements CaClient {
    private static Type respDataStringType = new TypeReference<RespData<String>>(){}.getType();
    private static Type respDataCaType = new TypeReference<RespData<Ca>>(){}.getType();
    private static Type respDataMapType = new TypeReference<RespData<Map>>(){}.getType();

    @Autowired
    private IClusterViewManager viewManager;

    @Override
    public RespData<String> caAuth(String nodeNameReg, List<CaVO> list) {
        String url = "/ca/auth";
        HttpClient httpClient = NetworkManage.getInstance().httpClient();
        List<String> names = viewManager.getCurrentView().getNodeNames();
        String nodeName = httpClient.getRandomPeer(names).getNodeName();
        return httpClient.postJson(nodeName, url, list, respDataStringType);
    }

    @Override
    public RespData<Ca> acquireCA(String nodeNameReg, String user) {
        String url = String.format("/ca/get?user=%s", user);
        HttpClient httpClient = NetworkManage.getInstance().httpClient();
        List<String> names = viewManager.getCurrentView().getNodeNames();
        String nodeName = httpClient.getRandomPeer(names).getNodeName();
        return httpClient.postJson(nodeName, url, null, respDataCaType);
    }

    @Override
    public RespData<Map> syncCluster(String nodeNameReg) {
        String url = "/ca/sync";
        HttpClient httpClient = NetworkManage.getInstance().httpClient();
        List<String> names = viewManager.getCurrentView().getNodeNames();
        String nodeName = httpClient.getRandomPeer(names).getNodeName();
        return httpClient.postJson(nodeName, url, null, respDataMapType);
    }
}

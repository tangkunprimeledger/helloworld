package com.higgs.trust.rs.core.integration;

import com.alibaba.fastjson.TypeReference;
import com.higgs.trust.network.HttpClient;
import com.higgs.trust.network.NetworkManage;
import com.higgs.trust.rs.core.bo.VoteReceipt;
import com.higgs.trust.rs.core.vo.ReceiptRequest;
import com.higgs.trust.rs.core.vo.VotingRequest;
import com.higgs.trust.slave.api.vo.RespData;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author duhongming
 * @date 2018/9/14
 */
@Component
public class HttpServiceProviderClient implements ServiceProviderClient {

    private static Type respDataStringType = new TypeReference<RespData<String>>(){}.getType();

    @Override
    public VoteReceipt voting(String nodeName, VotingRequest votingRequest) {
        String url = "/voting";
        HttpClient httpClient = NetworkManage.getInstance().httpClient();
        return httpClient.postJson(nodeName, url, votingRequest, VoteReceipt.class);
    }

    @Override
    public RespData<String> receipting(String nodeName, ReceiptRequest receiptRequest) {
        String url = "/receipting";
        HttpClient httpClient = NetworkManage.getInstance().httpClient();
        return httpClient.postJson(nodeName, url, receiptRequest, respDataStringType);
    }
}

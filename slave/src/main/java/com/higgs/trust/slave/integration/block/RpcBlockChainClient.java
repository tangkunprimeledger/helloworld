package com.higgs.trust.slave.integration.block;

import com.higgs.trust.config.view.IClusterViewManager;
import com.higgs.trust.network.HttpClient;
import com.higgs.trust.network.NetworkManage;
import com.higgs.trust.slave.api.rpc.request.BlockRequest;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.api.vo.TransactionVO;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author duhongming
 * @date 2018/9/18
 */
@ConditionalOnProperty(name = "network.rpc", havingValue = "netty", matchIfMissing = true)
@Component
@Slf4j
public class RpcBlockChainClient implements BlockChainClient {

    private static final String ACTION_TYPE_BLOCK_HEADER_GET = "block/header/get";
    private static final String ACTION_TYPE_BLOCK_GET = "block/get";


    @Autowired
    private IClusterViewManager viewManager;

    @Autowired
    private NetworkManage networkManage;

    private static Type respDataListType;

    static {
        try {
            respDataListType = (new Object() {
                public RespData<List<TransactionVO>> respDataList;
            }).getClass().getField("respDataList").getGenericType();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public RpcBlockChainClient() {
        log.info("Use RpcBlockChainClient");
    }

    @Override
    public List<BlockHeader> getBlockHeaders(String nodeNameReg, long startHeight, int size) {
        BlockRequest request = new BlockRequest(startHeight, size);
        List<String> names = viewManager.getCurrentView().getNodeNames();
        return networkManage.rpcClient().randomSendAndReceive(names, ACTION_TYPE_BLOCK_HEADER_GET, request);
    }

    @Override
    public List<BlockHeader> getBlockHeadersFromNode(String nodeName, long startHeight, int size) {
        BlockRequest request = new BlockRequest(startHeight, size);
        return networkManage.rpcClient().sendAndReceive(nodeName, ACTION_TYPE_BLOCK_HEADER_GET, request);
    }

    @Override
    public List<Block> getBlocks(String nodeNameReg, long startHeight, int size) {
        BlockRequest request = new BlockRequest(startHeight, size);
        List<String> names = viewManager.getCurrentView().getNodeNames();
        return networkManage.rpcClient().randomSendAndReceive(names, ACTION_TYPE_BLOCK_GET, request);
    }

    @Override
    public List<Block> getBlocksFromNode(String nodeName, long startHeight, int size) {
        BlockRequest request = new BlockRequest(startHeight, size);
        return networkManage.rpcClient().sendAndReceive(nodeName, ACTION_TYPE_BLOCK_GET, request);
    }

    @Override
    public RespData<List<TransactionVO>> submitToMaster(String nodeName, List<SignedTransaction> transactions) {
        String url = "/transaction/master/submit";
        HttpClient httpClient = NetworkManage.getInstance().httpClient();
        return httpClient.postJson(nodeName, url, transactions, respDataListType);
    }

}

package com.higgs.trust.rs.core.controller.explorer;

import com.higgs.trust.rs.core.api.RsBlockChainService;
import com.higgs.trust.slave.api.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author liuyu
 * @description 区块链浏览器接口
 * @date 2018-07-13
 */
@CrossOrigin @RequestMapping(value = "/explorer") @RestController @Slf4j public class ExplorerController {
    @Autowired RsBlockChainService rsBlockChainService;
    @Autowired ExplorerCache explorerCache;

    /**
     * query block info by page
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/queryBlocksByPage") @ResponseBody public RespData<List<BlockVO>> queryBlocksByPage(
        int pageNo, int pageSize) {
        QueryBlockVO req = new QueryBlockVO();
        req.setPageNo(pageNo);
        req.setPageSize(pageSize);

        RespData<List<BlockVO>> respData = explorerCache.get(req, RespData.class);
        if (respData != null) {
            return respData;
        } else {
            respData = new RespData<>();
        }

        List<BlockVO> list = rsBlockChainService.queryBlocksByPage(req);
        respData.setData(list);

        explorerCache.put(req, respData);

        return respData;
    }

    /**
     * query transaction by condition and page
     *
     * @param req
     * @return
     */
    @RequestMapping(value = "/queryTxsByPage") @ResponseBody public RespData<List<CoreTransactionVO>> queryTxsByPage(
        QueryTransactionVO req) {

        RespData<List<CoreTransactionVO>> respData = explorerCache.get(req, RespData.class);
        if (respData != null) {
            return respData;
        } else {
            respData = new RespData<>();
        }

        List<CoreTransactionVO> list = rsBlockChainService.queryTxsByPage(req);
        respData.setData(list);

        explorerCache.put(req, respData);

        return respData;
    }

    /**
     * query block info by height
     *
     * @param height
     * @return
     */
    @RequestMapping(value = "/queryBlockByHeight") @ResponseBody public RespData<BlockVO> queryBlockByHeight(
        Long height) {
        RespData<BlockVO> respData = explorerCache.get(height, RespData.class);
        if (respData != null) {
            return respData;
        } else {
            respData = new RespData<>();
        }

        BlockVO blockVO = rsBlockChainService.queryBlockByHeight(height);
        respData.setData(blockVO);

        explorerCache.put(height, respData);
        return respData;
    }

    /**
     * query tx info by tx_id
     *
     * @param txId
     * @return
     */
    @RequestMapping(value = "/queryTxById") @ResponseBody public RespData<CoreTransactionVO> queryTxById(String txId) {
        RespData<CoreTransactionVO> respData = explorerCache.get(txId, RespData.class);
        if (respData != null) {
            return respData;
        } else {
            respData = new RespData<>();
        }
        CoreTransactionVO coreTransactionVO = rsBlockChainService.queryTxById(txId);
        respData.setData(coreTransactionVO);

        explorerCache.put(txId, respData);
        return respData;
    }
}

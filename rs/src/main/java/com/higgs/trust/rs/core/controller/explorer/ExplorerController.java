package com.higgs.trust.rs.core.controller.explorer;

import com.higgs.trust.rs.core.api.RsBlockChainService;
import com.higgs.trust.rs.core.controller.explorer.vo.QueryBlockByHeightVO;
import com.higgs.trust.rs.core.controller.explorer.vo.QueryTxVO;
import com.higgs.trust.slave.api.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
     * @return
     */
    @RequestMapping(value = "/queryBlocksByPage", method = RequestMethod.POST) @ResponseBody
    public RespData<List<BlockVO>> queryBlocksByPage(@RequestBody QueryBlockVO req) {

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
    @RequestMapping(value = "/queryTxsByPage", method = RequestMethod.POST) @ResponseBody
    public RespData<List<CoreTransactionVO>> queryTxsByPage(@RequestBody QueryTransactionVO req) {

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
     * @param vo
     * @return
     */
    @RequestMapping(value = "/queryBlockByHeight", method = RequestMethod.POST) @ResponseBody
    public RespData<BlockVO> queryBlockByHeight(@RequestBody QueryBlockByHeightVO vo) {
        Long height = vo.getHeight();
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
     * @param vo
     * @return
     */
    @RequestMapping(value = "/queryTxById", method = RequestMethod.POST) @ResponseBody
    public RespData<CoreTransactionVO> queryTxById(@RequestBody QueryTxVO vo) {
        String txId = vo.getTxId();
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



    /**
     * query tx info by tx_id
     *
     * @param vo
     * @return
     */
    @RequestMapping(value = "/queryUTXO", method = RequestMethod.POST) @ResponseBody
    public RespData<List<UTXOVO>> queryUTXO(@RequestBody QueryTxVO vo) {
        String txId = vo.getTxId();
        String key = txId + "|" + "queryUTXO";
        RespData<List<UTXOVO>> respData = explorerCache.get(key, RespData.class);
        if (respData != null) {
            return respData;
        } else {
            respData = new RespData<>();
        }
        List<UTXOVO> UTXOs = rsBlockChainService.queryUTXO(txId);
        respData.setData(UTXOs);

        explorerCache.put(txId, respData);
        return respData;
    }
}

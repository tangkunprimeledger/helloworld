package com.higgs.trust.rs.core.controller.explorer;

import com.higgs.trust.rs.core.api.ContractService;
import com.higgs.trust.rs.core.api.RsBlockChainService;
import com.higgs.trust.rs.core.controller.explorer.vo.QueryBlockByHeightVO;
import com.higgs.trust.rs.core.controller.explorer.vo.QueryTxVO;
import com.higgs.trust.slave.api.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author liuyu
 * @description 区块链浏览器接口
 * @date 2018-07-13
 */
@CrossOrigin
@RequestMapping(value = "/explorer")
@RestController
@Slf4j
public class ExplorerController {
    @Autowired
    private RsBlockChainService rsBlockChainService;
    @Autowired
    private ExplorerCache explorerCache;
    @Autowired
    private ContractService contractService;


    /**
     * query block info by page
     *
     * @return
     */
    @RequestMapping(value = "/queryBlocksByPage", method = RequestMethod.POST)
    public RespData<List<BlockVO>> queryBlocksByPage(@RequestBody @Valid QueryBlockVO req) {
        ExplorerCache.CacheKey key = new ExplorerCache.CacheKey("queryBlocksByPage", req);

        RespData<List<BlockVO>> respData = explorerCache.get(key, RespData.class);
        if (respData != null) {
            return respData;
        }
        respData = new RespData<>();
        List<BlockVO> list = rsBlockChainService.queryBlocksByPage(req);
        respData.setData(list);

        explorerCache.put(key, respData);

        return respData;
    }

    /**
     * query transaction by condition and page
     *
     * @param req
     * @return
     */
    @RequestMapping(value = "/queryTxsByPage", method = RequestMethod.POST)
    public RespData<List<CoreTransactionVO>> queryTxsByPage(@RequestBody @Valid QueryTransactionVO req) {
        ExplorerCache.CacheKey key = new ExplorerCache.CacheKey("queryTxsByPage", req);
        RespData<List<CoreTransactionVO>> respData = explorerCache.get(key, RespData.class);
        if (respData != null) {
            return respData;
        }

        respData = new RespData<>();

        List<CoreTransactionVO> list = rsBlockChainService.queryTxsByPage(req);
        respData.setData(list);

        explorerCache.put(key, respData);

        return respData;
    }

    /**
     * query block info by height
     *
     * @param vo
     * @return
     */
    @RequestMapping(value = "/queryBlockByHeight", method = RequestMethod.POST)
    public RespData<BlockVO> queryBlockByHeight(@RequestBody @Valid QueryBlockByHeightVO vo) {
        Long height = vo.getHeight();
        ExplorerCache.CacheKey key = new ExplorerCache.CacheKey("queryBlockByHeight", height);
        RespData<BlockVO> respData = explorerCache.get(key, RespData.class);
        if (respData != null) {
            return respData;
        }

        respData = new RespData<>();

        BlockVO blockVO = rsBlockChainService.queryBlockByHeight(height);
        respData.setData(blockVO);

        explorerCache.put(key, respData);
        return respData;
    }

    /**
     * query tx info by tx_id
     *
     * @param vo
     * @return
     */
    @RequestMapping(value = "/queryTxById", method = RequestMethod.POST)
    public RespData<CoreTransactionVO> queryTxById(@RequestBody @Valid  QueryTxVO vo) {
        String txId = vo.getTxId();
        ExplorerCache.CacheKey key = new ExplorerCache.CacheKey("queryTxById", txId);
        RespData<CoreTransactionVO> respData = explorerCache.get(key, RespData.class);
        if (respData != null) {
            return respData;
        }
        respData = new RespData<>();

        CoreTransactionVO coreTransactionVO = rsBlockChainService.queryTxById(txId);
        respData.setData(coreTransactionVO);

        explorerCache.put(key, respData);
        return respData;
    }


    /**
     * query UTXO s
     *
     * @param vo
     * @return
     */
    @RequestMapping(value = "/queryUTXO", method = RequestMethod.POST)
    public RespData<List<UTXOVO>> queryUTXO(@RequestBody @Valid QueryTxVO vo) {
        String txId = vo.getTxId();
        ExplorerCache.CacheKey key = new ExplorerCache.CacheKey("queryUTXO", txId);
        RespData<List<UTXOVO>> respData = explorerCache.get(key, RespData.class);
        if (respData != null) {
            return respData;
        }
        respData = new RespData<>();

        List<UTXOVO> UTXOs = rsBlockChainService.queryUTXO(txId);
        respData.setData(UTXOs);

        explorerCache.put(key, respData);
        return respData;
    }


    /**
     * query account by page
     *
     * @param req
     * @return
     */
    @RequestMapping(value = "/queryAccountsByPage", method = RequestMethod.POST)
    public RespData<List<AccountInfoVO>> queryAccountsByPage(@RequestBody @Valid QueryAccountVO req) {
        ExplorerCache.CacheKey key = new ExplorerCache.CacheKey("queryAccountsByPage", req);
        RespData<List<AccountInfoVO>> respData = explorerCache.get(key, RespData.class);
        if (respData != null) {
            return respData;
        }

        respData = new RespData<>();

        List<AccountInfoVO> list = rsBlockChainService.queryAccountsByPage(req);
        respData.setData(list);

        explorerCache.put(key, respData);

        return respData;
    }

    /**
     * query contract by page
     *
     * @param req
     * @return
     */
    @RequestMapping(value = "/queryContractsByPage", method = RequestMethod.POST)
    public RespData<List<ContractVO>> queryContractsByPage(@RequestBody @Valid QueryContractVO req) {
        ExplorerCache.CacheKey key = new ExplorerCache.CacheKey("queryContractsByPage", req);
        RespData<List<ContractVO>> respData = explorerCache.get(key, RespData.class);
        if (respData != null) {
            return respData;
        }

        respData = new RespData<>();

        List<ContractVO> list = contractService.queryContractsByPage(req);
        respData.setData(list);

        explorerCache.put(key, respData);

        return respData;
    }

    @RequestMapping(value = "/queryPeersInfo", method = RequestMethod.POST)
    public RespData<List<NodeInfoVO>> queryPeersInfo() {
        ExplorerCache.CacheKey key = new ExplorerCache.CacheKey("queryPeersInfo", null);
        RespData<List<NodeInfoVO>> respData = explorerCache.get(key, RespData.class);
        if (respData != null) {
            return respData;
        }
        respData = new RespData<>();

        List<NodeInfoVO> peersInfo = rsBlockChainService.queryPeersInfo();
        respData.setData(peersInfo);
        explorerCache.put(key, respData);
        return respData;
    }
}

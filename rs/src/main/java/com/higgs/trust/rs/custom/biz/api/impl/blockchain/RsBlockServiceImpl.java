package com.higgs.trust.rs.custom.biz.api.impl.blockchain;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.rs.core.api.RsBlockChainService;
import com.higgs.trust.rs.custom.api.blockchain.BlockService;
import com.higgs.trust.rs.custom.api.vo.Page;
import com.higgs.trust.rs.custom.api.vo.blockchain.*;
import com.higgs.trust.rs.custom.model.RespData;
import com.higgs.trust.rs.custom.model.convertor.PageConvert;
import com.higgs.trust.slave.api.vo.PageVO;
import com.higgs.trust.slave.api.vo.QueryBlockVO;
import com.higgs.trust.slave.api.vo.QueryTransactionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author tangfashuang
 * @date 2018/05/13 16:27
 * @desc block service
 */
@Service
@Slf4j
public class RsBlockServiceImpl implements BlockService {

    @Autowired
    private RsBlockChainService blockChainService;

    @Override public RespData queryBlock(BlockQueryVO req) {
        RespData respData = new RespData();

        PageVO pageVO = blockChainService.queryBlock(BeanConvertor.convertBean(req, QueryBlockVO.class));

        Page<BlockVO> page = null;
        if (null != pageVO) {
            page = PageConvert.convertPageVOToPage(pageVO, BlockVO.class);
        }

        respData.setData(page);

        log.info("[RsBlockChainService.queryBlock] query result: {}", respData.getData());
        return respData;
    }

    @Override public RespData queryTx(TxQueryVO req) {
        RespData respData = new RespData();

        PageVO pageVO = blockChainService.queryTransaction(BeanConvertor.convertBean(req, QueryTransactionVO.class));
        Page<CoreTxVO> page = null;
        if (pageVO != null) {
            page = PageConvert.convertPageVOToPage(pageVO, CoreTxVO.class);
        }

        respData.setData(page);
        log.info("[RsBlockChainService.queryTx] query result: {}", respData.getData());
        return respData;
    }

    @Override public RespData queryUtxo(String txId) {
        RespData respData = new RespData();
        respData.setData(BeanConvertor.convertList(
            blockChainService.queryUtxo(txId), UtxoVO.class));

        log.info("[RsBlockChainService.queryUtxo] query result: {}", respData.getData());
        return respData;
    }
}

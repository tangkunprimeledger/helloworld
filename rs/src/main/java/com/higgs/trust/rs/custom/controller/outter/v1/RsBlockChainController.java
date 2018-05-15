package com.higgs.trust.rs.custom.controller.outter.v1;

import com.higgs.trust.rs.custom.api.blockchain.AccountInfoService;
import com.higgs.trust.rs.custom.api.blockchain.BlockService;
import com.higgs.trust.rs.custom.api.enums.RespCodeEnum;
import com.higgs.trust.rs.custom.api.vo.blockchain.AccountQueryVO;
import com.higgs.trust.rs.custom.api.vo.blockchain.BlockQueryVO;
import com.higgs.trust.rs.custom.api.vo.blockchain.TxQueryVO;
import com.higgs.trust.rs.custom.model.RespData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author tangfashuang
 * @date 2018/05/13 15:15
 */
@RequestMapping(value = "/v1/blockchain")
@RestController
@Slf4j
@CrossOrigin
public class RsBlockChainController {
    @Autowired
    private AccountInfoService accountInfoService;

    @Autowired
    private BlockService blockService;

    @RequestMapping(value = "/block/query", method = RequestMethod.POST)
    public RespData queryBlock(@RequestBody BlockQueryVO req) {
       log.info("[BlockChainController.queryBlock] query block request receive: {}", req);

       if(null == req) {
           RespData respData = new RespData();
           respData.setCode(RespCodeEnum.PARAM_NOT_VALID);
           return respData;
       }

       return blockService.queryBlock(req);
    }

    @RequestMapping(value = "/transaction/query", method = RequestMethod.POST)
    public RespData queryTransaction(@RequestBody TxQueryVO req) {
        log.info("[BlockChainController.queryTransaction] query transaction request receive: {}", req);

        if(null == req) {
            RespData respData = new RespData();
            respData.setCode(RespCodeEnum.PARAM_NOT_VALID);
            return respData;
        }

        return blockService.queryTx(req);
    }

    @RequestMapping(value = "/account/query", method = RequestMethod.POST)
    public RespData queryAccount(@RequestBody AccountQueryVO req) {
        log.info("[BlockChainController.queryAccount] query account request receive: {}", req);

        if(null == req) {
            RespData respData = new RespData();
            respData.setCode(RespCodeEnum.PARAM_NOT_VALID);
            return respData;
        }

        return accountInfoService.queryAccount(req);
    }

    @RequestMapping(value = "/utxo/query", method = RequestMethod.GET)
    public RespData queryUtxo(String txId) {
        log.info("[BlockChainController.queryUtxo] query UTXO request receive: txId={}", txId);

        if(StringUtils.isBlank(txId)) {
            RespData respData = new RespData();
            respData.setCode(RespCodeEnum.PARAM_NOT_VALID);
            return respData;
        }

        return blockService.queryUtxo(txId);
    }
}

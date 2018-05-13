package com.higgs.trust.rs.custom.biz.api.impl.blockchain;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.rs.core.api.RsBlockChainService;
import com.higgs.trust.rs.custom.api.blockchain.AccountInfoService;
import com.higgs.trust.rs.custom.api.vo.Page;
import com.higgs.trust.rs.custom.api.vo.blockchain.AccountQueryVO;
import com.higgs.trust.rs.custom.api.vo.blockchain.AccountVO;
import com.higgs.trust.rs.custom.model.RespData;
import com.higgs.trust.rs.custom.model.convertor.PageConvert;
import com.higgs.trust.slave.api.vo.AccountInfoVO;
import com.higgs.trust.slave.api.vo.PageVO;
import com.higgs.trust.slave.api.vo.QueryAccountVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * @author tangfashuang
 * @date 2018/05/13 16:26
 * @desc account service
 */
@Service
@Slf4j
public class AccountInfoServiceImpl implements AccountInfoService {
    @Autowired
    private RsBlockChainService rsBlockChainService;

    @Override public RespData queryAccount(AccountQueryVO req) {
        RespData respData = new RespData();

        QueryAccountVO vo = BeanConvertor.convertBean(req, QueryAccountVO.class);
        PageVO<AccountInfoVO> pageVO = rsBlockChainService.queryAccount(vo);
        Page<AccountVO> page = null;
        if (pageVO != null) {
            page = PageConvert.convertPageVOToPage(pageVO, AccountVO.class);
        }

        respData.setData(page);
        log.info("[AccountInfoServiceImpl.queryAccount] query result: {}", respData.getData());
        return respData;
    }
}

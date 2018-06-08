package com.higgs.trust.slave.core.service.action.ca;

import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.ca.CaRepository;
import com.higgs.trust.slave.core.repository.config.ClusterConfigRepository;
import com.higgs.trust.slave.core.repository.config.ClusterNodeRepository;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.datahandler.ca.CaSnapshotHandler;
import com.higgs.trust.slave.model.bo.config.ClusterConfig;
import com.higgs.trust.slave.model.bo.context.ActionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author WangQuanzhou
 * @desc init ca handler
 * @date 2018/6/6 10:25
 */
@Slf4j @Component public class CaInitHandler implements ActionHandler {

    @Autowired ClusterConfigRepository clusterConfigRepository;
    @Autowired ClusterNodeRepository clusterNodeRepository;
    @Autowired BlockRepository blockRepository;
    @Autowired TransactionTemplate txRequired;

    /**
     * the storage for the action
     *
     * @param actionData
     */
    @Override public void process(ActionData actionData) {
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                log.info("[process] transaction start，reqNo={}", reqNo);
                bankChainRequestDAO.updateRequest(bankChainRequestPO);

                // 将回调的数据存入identity表
                if (respData.isSuccess()) {
                    identityDAO.insertIdentity(identityPO);
                }
                log.info("[process] transaction success，reqNo={}", reqNo);
            }
        });
    } catch (Throwable e) {
        log.error("[process] store identity data error", e);
        throw new BankChainException(BankChainExceptionCodeEnum.IdentityCallbackProcessException,
            "[process] store identity data error");
    }
    }
}

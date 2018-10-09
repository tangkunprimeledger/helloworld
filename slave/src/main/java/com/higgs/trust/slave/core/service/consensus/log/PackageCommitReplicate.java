package com.higgs.trust.slave.core.service.consensus.log;

import com.higgs.trust.common.utils.TraceUtils;
import com.higgs.trust.consensus.annotation.Replicator;
import com.higgs.trust.consensus.core.ConsensusCommit;
import com.higgs.trust.slave.api.vo.PackageVO;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidateResult;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.service.pack.PackageProcess;
import com.higgs.trust.slave.core.service.pack.PackageService;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.consensus.PackageCommand;
import com.higgs.trust.slave.model.convert.PackageConvert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.zip.DataFormatException;

@Replicator @Slf4j @Component public class PackageCommitReplicate implements ApplicationContextAware, InitializingBean {

    @Autowired PackageService packageService;

    @Autowired ExecutorService packageThreadPool;

    @Autowired PackageProcess packageProcess;

    private ApplicationContext applicationContext;

    private List<PackageListener> listeners = new ArrayList<>();

    /**
     * package has been replicated by raft/copycat-smart/pbft/etc
     *
     * @param commit
     * @return
     */
    public void packageReplicated(ConsensusCommit<PackageCommand> commit) {
        // validate param
        if (null == commit) {
            log.error(
                "[LogReplicateHandler.packageReplicated]param validate failed, cause package command commit is null ");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        PackageVO packageVO = null;
        try {
            packageVO = commit.operation().getValueFromByte(PackageVO.class);
        } catch (DataFormatException e) {
            log.error("[LogReplicateHandler.packageReplicated]param validate failed, decompress package error:{}",e.getCause());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        // validate param
        if (packageVO == null) {
            log.error("[LogReplicateHandler.packageReplicated]param validate failed, cause package is null ");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        Long height = packageVO.getHeight();
        log.debug("package reached consensus, height: {}", height);

        Span span = TraceUtils.createSpan();
        try {
            BeanValidateResult result = BeanValidator.validate(packageVO);
            if (!result.isSuccess()) {
                log.error(
                    "[LogReplicateHandler.packageReplicated]param validate failed, cause: " + result.getFirstMsg());
                throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
            }

            // receive package
            Package pack = PackageConvert.convertPackVOToPack(packageVO);
            try {
                packageService.receive(pack);
                listeners.forEach(listener -> listener.received(pack));
            } catch (SlaveException e) {
                //idempotent as success, other exceptions make the consensus layer retry
                if (e.getCode() != SlaveErrorEnum.SLAVE_IDEMPOTENT) {
                    throw e;
                }
            }
        } finally {
            TraceUtils.closeSpan(span);
        }
        commit.close();
    }

    @Override public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override public void afterPropertiesSet() {
        Map<String, PackageListener> beansOfType = applicationContext.getBeansOfType(PackageListener.class);
        listeners.addAll(beansOfType.values());
    }
}

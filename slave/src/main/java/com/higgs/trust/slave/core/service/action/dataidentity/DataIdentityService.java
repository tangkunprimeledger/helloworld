package com.higgs.trust.slave.core.service.action.dataidentity;

import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.slave.core.service.datahandler.dataidentity.DataIdentityHandler;
import com.higgs.trust.slave.core.service.datahandler.dataidentity.DataIdentitySnapshotHandler;
import com.higgs.trust.slave.model.bo.DataIdentity;
import com.higgs.trust.slave.model.bo.action.DataIdentityAction;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.convert.DataIdentityConvert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * dataidentity repository
 *
 * @author lingchao
 * @create 2018年03月28日13:09
 */
@Slf4j
@Service
public class DataIdentityService {
    @Autowired
    private DataIdentitySnapshotHandler dataIdentitySnapshotHandler;

    /**
     * deal action with different TxProcessTypeEnum (data from db of snapshot)
     *
     * @param actionData
     */
    public void process(ActionData actionData) {
        // convert action and validate it
        DataIdentityAction dataIdentityAction = (DataIdentityAction) actionData.getCurrentAction();
        log.info("[ DataIdentityAction.validate] is start,params:{}", dataIdentityAction);

        //data operate type
        DataIdentityHandler dataIdentityHandler = dataIdentitySnapshotHandler;

        //validate idempotent
        DataIdentity dataIdentity = dataIdentityHandler.getDataIdentity(dataIdentityAction.getIdentity());
        if (null != dataIdentity) {
            log.error("DataIdentity：{} idempotent exception for identity:{}", dataIdentity, dataIdentityAction.getIdentity());
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        }

        Profiler.enter("[DataIdentity.save]");
        dataIdentity = DataIdentityConvert.buildDataIdentity(dataIdentityAction.getIdentity(), dataIdentityAction.getChainOwner(), dataIdentityAction.getDataOwner());
        dataIdentityHandler.saveDataIdentity(dataIdentity);
        Profiler.release();
    }

    /**
     * validate dataIdentityPOList weather data owner and chain owner are legal
     *
     * @param rsList
     * @param dataIdentityList
     * @return
     */
    public boolean validate(List<String> rsList, List<DataIdentity> dataIdentityList) {
        if (log.isDebugEnabled()) {
            log.debug("Start to validate data attribution");
        }
        // validate params
        if (CollectionUtils.isEmpty(rsList) || CollectionUtils.isEmpty(dataIdentityList)) {
            log.error("RsList or dataIdentityPOList can not null. RsList is {} , dataIdentityPOList is ", rsList, dataIdentityList);
            return false;
        }

        // dataidentity rs set and dataidentity chain set
        Set<String> dataIdentityChainSet = new HashSet<>();
        Set<String> dataIdentityRsSet = new HashSet<>();
        for (DataIdentity dataIdentity : dataIdentityList) {
            dataIdentityChainSet.add(dataIdentity.getChainOwner());
            dataIdentityRsSet.add(dataIdentity.getDataOwner());
        }

        // validate only one chain
        if (CollectionUtils.isNotEmpty(dataIdentityChainSet) && dataIdentityChainSet.size() > 1) {
            log.error("Chain owner  is illegal, the chain owner is not only one .Chain owners are: {}", dataIdentityChainSet);
            return false;
        }

        // validate dataIdentity rs can not be more than rsList
        if (rsList.size() < dataIdentityRsSet.size()) {
            log.error("Chain owner  is illegal, the data owners are more than rsList .The data owners  are: {} , rsList is :{}", dataIdentityRsSet, rsList);
            return false;
        }

        // validate weather dataidentity rs in rsList，if it is not in break.

        for (String dataIdentityRs : dataIdentityRsSet) {
            if (!rsList.contains(dataIdentityRs)) {
                log.error("Data owner  is illegal, the data owner  {} is not in  policy rs list :{}.", dataIdentityRs, rsList);
                return false;
            }
        }
        return true;
    }
}

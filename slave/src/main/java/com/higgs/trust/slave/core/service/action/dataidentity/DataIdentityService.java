package com.higgs.trust.slave.core.service.action.dataidentity;

import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.service.datahandler.dataidentity.DataIdentityDBHandler;
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

import java.util.*;

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
    @Autowired
    private DataIdentityDBHandler dataIdentityDBHandler;


    /**
     * deal action with different TxProcessTypeEnum (data from db of snapshot)
     *
     * @param actionData
     * @param processTypeEnum
     */
    public void process(ActionData actionData, TxProcessTypeEnum processTypeEnum) {
        // convert action and validate it
        DataIdentityAction dataIdentityAction = (DataIdentityAction) actionData.getCurrentAction();
        log.info("[ DataIdentityAction.validate] is start,params:{}", dataIdentityAction);
        try {
            BeanValidator.validate(dataIdentityAction).failThrow();
        } catch (IllegalArgumentException e) {
            log.error("Convert and validate dataIdentityAction is error .msg={}", e.getMessage());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR, e);
        }

        //data operate type
        DataIdentityHandler dataIdentityHandler = null;
        if (TxProcessTypeEnum.VALIDATE.equals(processTypeEnum)) {
            dataIdentityHandler = dataIdentitySnapshotHandler;
        }
        if (TxProcessTypeEnum.PERSIST.equals(processTypeEnum)) {
            dataIdentityHandler = dataIdentityDBHandler;
        }

        //validate idempotent
        DataIdentity dataIdentity = dataIdentityHandler.getDataIdentity(dataIdentityAction.getIdentity());
        if (null != dataIdentity) {
            log.error("DataIdentity idempotent exception ");
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        }

        dataIdentity = DataIdentityConvert.buildDataIdentity(dataIdentityAction.getIdentity(), dataIdentityAction.getChainOwner(), dataIdentityAction.getDataOwner());
        dataIdentityHandler.saveDataIdentity(dataIdentity);

    }

    /**
     * validate dataIdentityPOList weather data owner and chain owner are legal
     *
     * @param rsSet
     * @param dataIdentityList
     * @return
     */
    public boolean validate(Set<String> rsSet, List<DataIdentity> dataIdentityList) {
        log.info("Start to validate data attribution");
        // validate params
        if (CollectionUtils.isEmpty(rsSet) || CollectionUtils.isEmpty(dataIdentityList)) {
            log.error("RsList or dataIdentityPOList can not null. RsList is {} , dataIdentityPOList is ", rsSet, dataIdentityList);
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

        // validate dataidentity rs can not be more than rsList
        if (rsSet.size() < dataIdentityRsSet.size()) {
            log.error("Chain owner  is illegal, the data owners are more than rsList .The data owners  are: {} , rsList is :{}", dataIdentityRsSet, rsSet);
            return false;
        }

        // validate weather dataidentity rs in rsList，if it is not in break.

        for (String dataIdentityRs : dataIdentityRsSet) {
            if (!rsSet.contains(dataIdentityRs)) {
                log.error("Data owner  is illegal, the data owner  {} is not in  policy rs list :{}.", dataIdentityRs, rsSet);
                return false;
            }
        }
        return true;
    }
}

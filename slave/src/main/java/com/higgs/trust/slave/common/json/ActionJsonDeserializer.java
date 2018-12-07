package com.higgs.trust.slave.common.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.higgs.trust.common.lambda.Mapper;
import com.higgs.trust.consensus.p2pvalid.core.ResponseCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.model.bo.account.*;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.DataIdentityAction;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.ca.CaAction;
import com.higgs.trust.slave.model.bo.contract.*;
import com.higgs.trust.slave.model.bo.manage.CancelRS;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import com.higgs.trust.slave.model.bo.node.NodeAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author duhongming
 * @date 2018/8/2
 */
@Slf4j public class ActionJsonDeserializer implements ObjectDeserializer {

    static Map<String, Mapper<JSONObject, Action>> convertMap = new HashMap<>(20);
    static Map<ActionTypeEnum, Type> actionMap = new HashMap<>(20);

    static {
        actionMap.put(ActionTypeEnum.OPEN_ACCOUNT, OpenAccount.class);
        actionMap.put(ActionTypeEnum.FREEZE, AccountFreeze.class);
        actionMap.put(ActionTypeEnum.UNFREEZE, AccountUnFreeze.class);
        actionMap.put(ActionTypeEnum.UTXO, UTXOAction.class);
        actionMap.put(ActionTypeEnum.ACCOUNTING, AccountOperation.class);
        actionMap.put(ActionTypeEnum.REGISTER_RS, RegisterRS.class);
        actionMap.put(ActionTypeEnum.RS_CANCEL, CancelRS.class);
        actionMap.put(ActionTypeEnum.REGISTER_POLICY, RegisterPolicy.class);
        actionMap.put(ActionTypeEnum.ISSUE_CURRENCY, IssueCurrency.class);
        actionMap.put(ActionTypeEnum.CREATE_DATA_IDENTITY, DataIdentityAction.class);
        actionMap.put(ActionTypeEnum.BIND_CONTRACT, AccountContractBindingAction.class);
        actionMap.put(ActionTypeEnum.TRIGGER_CONTRACT, ContractInvokeAction.class);
        actionMap.put(ActionTypeEnum.REGISTER_CONTRACT, ContractCreationAction.class);
        actionMap.put(ActionTypeEnum.CONTRACT_STATE_MIGRATION, ContractStateMigrationAction.class);
        actionMap.put(ActionTypeEnum.CA_AUTH, CaAction.class);
        actionMap.put(ActionTypeEnum.CA_CANCEL, CaAction.class);
        actionMap.put(ActionTypeEnum.CA_INIT, CaAction.class);
        actionMap.put(ActionTypeEnum.CA_UPDATE, CaAction.class);
        actionMap.put(ActionTypeEnum.NODE_JOIN, NodeAction.class);
        actionMap.put(ActionTypeEnum.NODE_LEAVE, NodeAction.class);
        actionMap.put(ActionTypeEnum.CONTRACT_CREATION, ContractCreationV2Action.class);
        actionMap.put(ActionTypeEnum.CONTRACT_INVOKED, ContractInvokeV2Action.class);

        ParserConfig.getGlobalInstance().putDeserializer(ValidCommand.class, new ValidCommandJsonDeserializer());
        ParserConfig.getGlobalInstance().putDeserializer(ResponseCommand.class, new ResponseCommandJsonDeserializer());
        //convertMap.put(ActionTypeEnum.REGISTER_CONTRACT.name(), obj -> CreateContractAction.fromMap(obj));
    }

    @Override public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        Object obj = parser.parse();
        if (obj == null) {
            return null;
        }
        if (obj instanceof Action) {
            return (T)obj;
        }

        // parser.parseObject()
        JSONObject jsonObject = (JSONObject)obj;
        String actionTypeName = jsonObject.getString("type");
        if (StringUtils.isEmpty(actionTypeName)) {
            log.error("action type is empty, {}", ((JSONObject)obj).toJSONString());
            return null;
        }
        ActionTypeEnum actionType = ActionTypeEnum.valueOf(actionTypeName);
        if (actionType == null) {
            log.error("action type is invalid: {}", actionTypeName);
            return null;
        }
        Mapper mapper = convertMap.get(actionType);
        if (mapper != null) {
            return (T)convertMap.get(actionType).mapping(jsonObject);
        }
        jsonObject.remove("@type");
        Type realType = actionMap.get(actionType);
        return (T)JSON.parseObject(jsonObject.toJSONString(), realType);
    }

    @Override public int getFastMatchToken() {
        return 0;
    }
}


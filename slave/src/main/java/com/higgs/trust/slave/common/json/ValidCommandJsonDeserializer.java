package com.higgs.trust.slave.common.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import com.higgs.trust.slave.core.service.consensus.view.ClusterViewCmd;
import com.higgs.trust.slave.model.bo.consensus.BlockHeaderCmd;
import com.higgs.trust.slave.model.bo.consensus.ClusterHeightCmd;
import com.higgs.trust.slave.model.bo.consensus.PersistCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

//import com.higgs.trust.config.master.command.ChangeMasterVerifyCmd;

/**
 * @author duhongming
 * @date 2018/8/2
 */
@Slf4j
public class ValidCommandJsonDeserializer implements ObjectDeserializer {

    static Map<String, Type> typeMap = new HashMap<>();

    static {
        typeMap.put(BlockHeaderCmd.class.getSimpleName(), BlockHeaderCmd.class);
//        typeMap.put(ChangeMasterVerifyCmd.class.getSimpleName(), ChangeMasterVerifyCmd.class);
        typeMap.put(ClusterHeightCmd.class.getSimpleName(), ClusterHeightCmd.class);
        typeMap.put(ClusterViewCmd.class.getSimpleName(), ClusterViewCmd.class);
        typeMap.put(PersistCommand.class.getSimpleName(), PersistCommand.class);
    }

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        Object obj = parser.parse();
        if (obj == null) {
            return null;
        }
        if(obj instanceof ValidCommand) {
            return (T) obj;
        }

        JSONObject jsonObject = (JSONObject) obj;
        String cmdName = jsonObject.getString("cmdName");
        if (StringUtils.isEmpty(cmdName)) {
            log.error("cmdName is empty, {}", ((JSONObject) obj).toJSONString());
            return null;
        }

        jsonObject.remove("@type");
        Type realType = typeMap.get(cmdName);
        if (realType == null) {
            log.error("cmdName is invalid: {}", cmdName);
        }
        return (T) JSON.parseObject(jsonObject.toJSONString(), realType);
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}

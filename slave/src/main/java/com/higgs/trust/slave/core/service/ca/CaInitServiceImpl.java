package com.higgs.trust.slave.core.service.ca;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.managment.ClusterInitHandler;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.ca.CaAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author WangQuanzhou
 * @desc ca init service
 * @date 2018/6/5 15:48
 */
@Service @Slf4j public class CaInitServiceImpl implements CaInitService {

    @Autowired private NodeState nodeState;
    @Autowired private ClusterInitHandler clusterInitHandler;

    /**
     * @param
     * @return
     * @desc
     */
    @Override public void initKeyPair() throws FileNotFoundException {

        List<Action> caActionList = acquirePubKeys();

        // construct genius block and insert into db
        try {
            log.info("[CaInitServiceImpl.initKeyPair] start to generate genius block");

            // sort caActionList by user
            Collections.sort(caActionList, new Comparator<Action>() {
                @Override public int compare(Action caAction1, Action caAction2) {
                    return ((CaAction)caAction1).getUser().compareTo(((CaAction)caAction2).getUser());
                }
            });

            if (log.isDebugEnabled()) {
                log.debug("[CaInitServiceImpl.initKeyPair] user ={}, caActionList={}", nodeState.getNodeName(),
                    caActionList.toString());
            }

            clusterInitHandler.process(caActionList);
            log.info("[CaInitServiceImpl.initKeyPair] end generate genius block");

        } catch (Throwable e) {
            log.error("[CaInitServiceImpl.initKeyPair] cluster init CA error", e);
            throw new SlaveException(SlaveErrorEnum.SLAVE_CA_INIT_ERROR,
                "[CaInitServiceImpl.initKeyPair] cluster init CA error", e);
        }

    }

    private List<Action> acquirePubKeys() throws FileNotFoundException {
        JsonParser parser = new JsonParser();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("geniusBlock.json");
        JsonObject object = (JsonObject)parser.parse(new InputStreamReader(inputStream));

        JsonArray array = object.get("transactions").getAsJsonArray();

        JsonArray actions = (array.get(0).getAsJsonObject()).get("actions").getAsJsonArray();

        List<Action> caActionList = new LinkedList<>();

        for (int k = 0; k < actions.size(); k++) {
            JsonObject node = actions.get(k).getAsJsonObject();
            String nodeName = node.get("nodeName").getAsString();
            JsonArray pubKeys = node.get("keys").getAsJsonArray();
            for (int i = 0; i < pubKeys.size(); i++) {
                CaAction caAction = new CaAction();
                String pubKey = pubKeys.get(i).getAsJsonObject().get("publicKey").getAsString();
                String type = pubKeys.get(i).getAsJsonObject().get("type").getAsString();

                caAction.setType(ActionTypeEnum.CA_INIT);
                caAction.setUser(nodeName);
                caAction.setPubKey(pubKey);
                caAction.setUsage(type);
                caActionList.add(caAction);
            }
        }
        log.info("end acquirePubKeys, caActionList.size={}, caActionList={}", caActionList.size(), caActionList);
        return caActionList;
    }
}

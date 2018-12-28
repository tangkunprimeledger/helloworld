package com.higgs.trust.slave.core.service.ca;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.managment.ClusterInitHandler;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.ca.CaAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    @Value("${higgs.trust.geniusPath:#{null}}") String geniusPath;

    /**
     * @param
     * @return
     * @desc
     */
    @Override public void initKeyPair() throws IOException {

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

    private List<Action> acquirePubKeys() throws IOException {
        JSONObject geniusBlock;
        if (StringUtils.isBlank(geniusPath)) {
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("geniusBlock.json");
            String jsonText = IOUtils.toString(inputStream, "UTF-8");
            geniusBlock = JSON.parseObject(jsonText);
        } else {
            String jsonText = IOUtils.toString(new FileInputStream(geniusPath), "UTF-8");
            geniusBlock = JSON.parseObject(jsonText);
        }

        JSONArray transactions = geniusBlock.getJSONArray("transactions");
        JSONArray actions = transactions.getJSONObject(0).getJSONArray("actions");

        List<Action> caActionList = new LinkedList<>();

        for (int k = 0; k < actions.size(); k++) {
            JSONObject node = actions.getJSONObject(k);
            String nodeName = node.getString("nodeName");
            JSONArray pubKeys = node.getJSONArray("keys");
            for (int i = 0; i < pubKeys.size(); i++) {
                CaAction caAction = new CaAction();
                String pubKey = pubKeys.getJSONObject(i).getString("publicKey");
                String type = pubKeys.getJSONObject(i).getString("type");

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

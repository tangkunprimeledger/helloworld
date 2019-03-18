package com.higgs.trust.config.properties.master;

import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.consensus.config.listener.StateChangeListener;
import com.higgs.trust.consensus.config.listener.StateListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author lingchao
 * @create 2018年07月12日17:19
 */
@Service
@StateListener
public class DefaultMasterService {

    @Autowired
    private MasterConfig config;

    @Autowired
    private NodeState nodeState;

    @StateChangeListener(value = NodeStateEnum.Running, before = true)
    public void setMaster() {
        String masterName = config.getMasterName();
        if (StringUtils.isBlank(masterName)) {
            masterName = nodeState.getNodeName();
        }
        nodeState.changeMaster(masterName);
    }
}

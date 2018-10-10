package com.higgs.trust.management.failover.service;

import com.higgs.trust.consensus.config.NodeProperties;
import com.higgs.trust.consensus.config.NodeStatefulService;
import com.higgs.trust.network.NetworkManage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * standby service
 *
 * @author lingchao
 * @create 2018年10月08日19:53
 */
@Service
public class StandbyService extends NodeStatefulService {
    @Autowired
    private NodeProperties nodeProperties;


    @Override
    public String getStatefulServiceName() {
        return "standby";
    }

    @Override
    protected void doStart() {
        nodeProperties.setStandby(true);

    }

    @Override
    protected void doPause() {
        nodeProperties.setStandby(false);
    }

    @Override
    protected void doResume() {
        nodeProperties.setStandby(true);
    }


    /**
     * restart net work
     */
    public void restartNetwork(){
        NetworkManage.getInstance().shutdown();
        NetworkManage.getInstance().config().setBackupNode(nodeProperties.isStandby());
        NetworkManage.getInstance().start();
    }

}

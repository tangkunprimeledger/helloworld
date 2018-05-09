package com.higgs.trust.consensus.p2pvalid.core.storage;
import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import com.higgs.trust.consensus.p2pvalid.core.spi.ClusterInfo;
import com.higgs.trust.consensus.p2pvalid.dao.*;
import com.higgs.trust.consensus.p2pvalid.dao.po.QueuedApplyPO;
import com.higgs.trust.consensus.p2pvalid.dao.po.QueuedReceiveGcPO;
import com.higgs.trust.consensus.p2pvalid.dao.po.ReceiveCommandPO;
import com.higgs.trust.consensus.p2pvalid.dao.po.ReceiveNodePO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@Slf4j
public class ReceiveService {

    private static final Integer COMMAND_NORMAL = 0;
    private static final Integer COMMAND_QUEUED_APPLY = 1;
    private static final Integer COMMAND_QUEUED_GC = 2;

    private static final Integer COMMAND_NOT_CLOSED = 0;
    private static final Integer COMMAND_CLOSED = 1;

    @Autowired
    private ValidConsensus validConsensus;

    @Autowired
    private ReceiveCommandDao receiveCommandDao;

    @Autowired
    private ReceiveNodeDao receiveNodeDao;

    @Autowired
    private QueuedReceiveGcDao queuedReceiveGcDao;

    @Autowired
    private QueuedApplyDao queuedApplyDao;

    @Autowired
    private QueuedApplyDelayDao queuedApplyDelayDao;

    @Autowired
    private TransactionTemplate txRequired;

    @Autowired
    private ClusterInfo clusterInfo;



    public synchronized void receive(ValidCommandWrap validCommandWrap){
        try{
            String messageDigest = validCommandWrap.getValidCommand().getMessageDigestHash();

            //check duplicate
            ReceiveNodePO receiveNode = receiveNodeDao.queryByMessageDigestAndFromNode(validCommandWrap.getValidCommand().getMessageDigestHash(),validCommandWrap.getFromNode());

            if(null != receiveNode){
                log.warn("duplicate command from node {} , validCommandWrap : {}", validCommandWrap.getFromNode(), validCommandWrap);
                return;
            }

            String pubKey = clusterInfo.pubKey(clusterInfo.myNodeName());
            if (!SignUtils.verify(messageDigest, validCommandWrap.getSign(), pubKey)) {
                throw new Exception(String.format("check sign failed for node %s, validCommandWrap %s, pubKey %s", validCommandWrap.getFromNode(), validCommandWrap, pubKey));
            }

            txRequired.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    ReceiveCommandPO receiveCommand = receiveCommandDao.queryByMessageDigest(messageDigest);
                    if(null == receiveCommand){
                        //set apply threshold
                        receiveCommand.setApplyThreshold(Math.min(clusterInfo.faultNodeNum() * 2 + 1, clusterInfo.clusterNodeNames().size()));
                        receiveCommand.setCommandClass(validCommandWrap.getCommandClass());
                        receiveCommand.setGcThreshold(clusterInfo.clusterNodeNames().size());
                        receiveCommand.setMessageDigest(messageDigest);
                        receiveCommand.setNodeName(clusterInfo.myNodeName());
                        receiveCommand.setReceiveNodeNum(1);
                        receiveCommand.setValidCommand(JSON.toJSONString(validCommandWrap.getValidCommand()));
                        receiveCommand.setStatus(COMMAND_NORMAL);
                        receiveCommand.setClosed(COMMAND_NOT_CLOSED);
                        receiveCommandDao.add(receiveCommand);
                    }else{
                        Integer receiveNodeNum = receiveCommand.getReceiveNodeNum() + 1;
                        receiveCommand.setReceiveNodeNum(receiveNodeNum);
                        receiveCommandDao.updateReceiveNodeNum(receiveCommand.getMessageDigest(), receiveNodeNum);
                    }

                    // add receive node
                    ReceiveNodePO receiveNode = new ReceiveNodePO();
                    receiveNode.setCommandSign(validCommandWrap.getSign());
                    receiveNode.setFromNodeName(validCommandWrap.getFromNode());
                    receiveNode.setMessageDigest(validCommandWrap.getValidCommand().getMessageDigestHash());
                    receiveNodeDao.add(receiveNode);

                    //check status
                    if(receiveCommand.getStatus().equals(COMMAND_QUEUED_GC)){
                        log.warn("command has add to gc : {}", receiveCommand);

                    }else if(receiveCommand.getStatus().equals(COMMAND_QUEUED_APPLY)){
                        log.info("command has queued to apply : {}", receiveCommand);
                        if(receiveCommand.getClosed().equals(COMMAND_CLOSED) && receiveCommand.getReceiveNodeNum() >= receiveCommand.getGcThreshold()){
                            log.info("command has closed by biz and receive node num :{} >=  gc threshold :{} ,add command to gc queue : {}", receiveCommand.getReceiveNodeNum(), receiveCommand.getGcThreshold(), receiveCommand);
                            //add gc
                            QueuedReceiveGcPO queuedReceiveGc = new QueuedReceiveGcPO();
                            queuedReceiveGc.setGcTime(System.currentTimeMillis() + 10000L);
                            queuedReceiveGc.setMessageDigest(receiveCommand.getMessageDigest());
                            queuedReceiveGcDao.add(queuedReceiveGc);
                            //trans status
                            receiveCommandDao.transStatus(receiveCommand.getMessageDigest(), COMMAND_QUEUED_GC);
                        }
                    }else if(receiveCommand.getReceiveNodeNum() >= receiveCommand.getApplyThreshold()){
                        log.info("comman receive node num : {} >= command apply threshold : {}, add command to apply queue : {}", receiveCommand.getReceiveNodeNum(), receiveCommand.getApplyThreshold(), receiveCommand);
                        //add apply
                        QueuedApplyPO queuedApply = new QueuedApplyPO();
                        queuedApply.setMessageDigest(receiveCommand.getMessageDigest());
                        queuedApplyDao.add(queuedApply);
                        //trans status
                        receiveCommandDao.transStatus(receiveCommand.getMessageDigest(), COMMAND_QUEUED_APPLY);
                    }
                }
            });
        }catch (Throwable throwable){
            throw new RuntimeException(throwable);
        }
    }

}

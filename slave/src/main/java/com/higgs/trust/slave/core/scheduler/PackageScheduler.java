package com.higgs.trust.slave.core.scheduler;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.higgs.trust.common.utils.TraceUtils;
import com.higgs.trust.common.enums.MonitorTargetEnum;
import com.higgs.trust.common.utils.MonitorLogUtils;
import com.higgs.trust.config.crypto.CryptoUtil;
import com.higgs.trust.config.view.ClusterOptTx;
import com.higgs.trust.config.view.IClusterViewManager;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.core.managment.master.MasterPackageCache;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.PackageRepository;
import com.higgs.trust.slave.core.service.pack.PackageProcess;
import com.higgs.trust.slave.core.service.pack.PackageService;
import com.higgs.trust.slave.core.service.pending.PendingState;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.consensus.PackageCommand;
import com.higgs.trust.slave.model.bo.node.NodeAction;
import com.higgs.trust.slave.model.convert.PackageConvert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.Span;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.xml.soap.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author tangfashuang
 * @date 2018/04/09 15:30
 */
@ConditionalOnProperty(name = "higgs.trust.joinConsensus", havingValue = "true", matchIfMissing = true)
@Service @Slf4j public class PackageScheduler {

    @Autowired private PendingState pendingState;

    @Autowired private PackageRepository packageRepository;

    @Autowired private PackageService packageService;

    @Autowired private PackageProcess packageProcess;

    @Autowired private BlockRepository blockRepository;

    @Autowired private NodeState nodeState;

    @Autowired private MasterPackageCache packageCache;

    @Autowired private IClusterViewManager viewManager;

    @Value("${trust.batch.tx.limit:200}") private int TX_PENDING_COUNT;

    /**
     * master node create package
     */
    @Scheduled(fixedRateString = "${trust.schedule.package.create}") public void createPackage() {

        if (nodeState.isState(NodeStateEnum.Running) && nodeState.isMaster()
            && packageCache.getPendingPackSize() < Constant.MAX_BLOCKING_QUEUE_SIZE) {

            Object[] objs = pendingState.getPendingTransactions(TX_PENDING_COUNT);
            if(objs == null){
                return;
            }
            List<SignedTransaction> signedTransactions = (List<SignedTransaction>)objs[0];
            if (CollectionUtils.isEmpty(signedTransactions)) {
                return;
            }
            // remove dup transactions
            Set<SignedTransaction> txSet = Sets.newHashSet();
            CollectionUtils.addAll(txSet, signedTransactions);
            signedTransactions = Lists.newArrayList(txSet);
            log.debug("[PackageScheduler.createPackage] start create package, currentPackHeight={}",
                packageCache.getPackHeight());
            Package pack = packageService.create(signedTransactions, packageCache.getPackHeight());
            if (null == pack) {
                //add pending signedTransactions to pendingTxQueue
                pendingState.addPendingTxsToQueueFirst(signedTransactions);
                return;
            }
            try {
                 //node operation transaction
                SignedTransaction nodeOptTx = null;
                if(objs[1] != null){
                    nodeOptTx = (SignedTransaction)objs[1];
                }
                //make package command
                PackageCommand command = createPackCommand(pack,nodeOptTx);
                //put to queue
                packageCache.putPendingPack(command);
            } catch (Throwable e) {
                log.warn("pending pack offer InterruptedException. ", e);
                pendingState.addPendingTxsToQueueFirst(signedTransactions);
            }
            log.debug("[PackageScheduler.createPackage] create package finished, packHeight={}", pack.getHeight());
        }
    }

    /**
     * master node submit package
     */
    @Scheduled(fixedRateString = "${trust.schedule.package.submit}") public void submitPackage() {
        while (nodeState.isState(NodeStateEnum.Running) && nodeState.isMaster()
            && packageCache.getPendingPackSize() > 0) {
            Span span = TraceUtils.createSpan();
            try {
                packageService.submitConsensus(packageCache.getPackage());
            } finally {
                TraceUtils.closeSpan(span);
            }
        }
    }

    /**
     * process package
     */
    @Scheduled(fixedRateString = "${trust.schedule.package.process}") public void processPackage() {
        if (!nodeState.isState(NodeStateEnum.Running)) {
            return;
        }
        //get max block height
        Long currentHeight = blockRepository.getMaxHeight();

        if (null == currentHeight) {
            log.error("please initial Genesis block.");
            MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_GENESIS_BLOCK_NOT_EXISTS.getMonitorTarget(), 1);
            return;
        }

        while (true) {
            //check if the next package to be process is exited
            List<Long> heights = packageRepository.loadHeightList(currentHeight);

            if (CollectionUtils.isEmpty(heights)) {
                return;
            }

            for (Long height : heights) {
                // process next block as height = maxBlockHeight + 1
                if (height != currentHeight + 1) {
                    log.warn("package height is not continuous!");
                    return;
                }

                Span span = TraceUtils.createSpan();
                try {
                    packageProcess.process(height);
                    currentHeight++;
                } catch (Throwable e) {
                    log.error("package process scheduled execute failed. ", e);
                    return;
                } finally {
                    TraceUtils.closeSpan(span);
                }
            }
        }
    }

    /**
     * create command of package
     *
     * @param pack
     * @param nodeOptTx
     * @return
     */
    private PackageCommand createPackCommand(Package pack,SignedTransaction nodeOptTx){
        PackageCommand command = new PackageCommand(nodeState.getCurrentTerm(),viewManager.getCurrentViewId(),nodeState.getMasterName(),
            PackageConvert.convertPackToPackVO(pack));
        String signValue = command.getSignValue();
        command.setSign(CryptoUtil.getProtocolCrypto().sign(signValue, nodeState.getConsensusPrivateKey()));
        if(nodeOptTx!=null) {
            //convert sign info
            List<ClusterOptTx.SignatureInfo> signs = new ArrayList<>(nodeOptTx.getSignatureList().size());
            for(SignInfo signInfo : nodeOptTx.getSignatureList()){
                ClusterOptTx.SignatureInfo signatureInfo = new ClusterOptTx.SignatureInfo();
                signatureInfo.setSign(signInfo.getSign());
                signatureInfo.setSigner(signInfo.getOwner());
                signs.add(signatureInfo);
            }
            List<Action> actionList = nodeOptTx.getCoreTx().getActionList();
            NodeAction action = (NodeAction)actionList.get(0);
            //make ClusterOptTx
            ClusterOptTx clusterOptTx = new ClusterOptTx();
            //txs signs
            clusterOptTx.setSignatureList(signs);
            //self node name
            clusterOptTx.setNodeName(action.getNodeName());
            //self pubkey
            clusterOptTx.setPubKey(action.getPubKey());
            //self sign
            clusterOptTx.setSelfSign(action.getSelfSign());
            //self sign value
            clusterOptTx.setSelfSignValue(action.getSignValue());
            //txs sign value
            clusterOptTx.setSignatureValue(JSON.toJSONString(nodeOptTx.getCoreTx()));
            //action operation JOIN or LEAVE
            clusterOptTx.setOperation(action.getType() == ActionTypeEnum.NODE_JOIN ? ClusterOptTx.Operation.JOIN : ClusterOptTx.Operation.LEAVE);
            //set
            command.setClusterOptTx(clusterOptTx);
        }
        return command;
    }
}

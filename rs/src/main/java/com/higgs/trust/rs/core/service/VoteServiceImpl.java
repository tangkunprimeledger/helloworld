package com.higgs.trust.rs.core.service;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.common.utils.OkHttpClientManager;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.SignService;
import com.higgs.trust.rs.core.api.TxCallbackRegistor;
import com.higgs.trust.rs.core.api.VoteService;
import com.higgs.trust.rs.core.api.enums.VoteResultEnum;
import com.higgs.trust.rs.core.bo.CoreTxBO;
import com.higgs.trust.rs.core.bo.VoteReceipt;
import com.higgs.trust.rs.core.bo.VoteRequestRecord;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.rs.core.dao.po.VoteRequestRecordPO;
import com.higgs.trust.rs.core.integration.ServiceProviderClient;
import com.higgs.trust.rs.core.repository.CoreTxRepository;
import com.higgs.trust.rs.core.repository.VoteReceiptRepository;
import com.higgs.trust.rs.core.repository.VoteReqRecordRepository;
import com.higgs.trust.rs.core.vo.ReceiptRequest;
import com.higgs.trust.rs.core.vo.VotingRequest;
import com.higgs.trust.slave.api.enums.TxTypeEnum;
import com.higgs.trust.slave.api.enums.manage.DecisionTypeEnum;
import com.higgs.trust.slave.api.enums.manage.VotePatternEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.RsNodeRepository;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.manage.RsNode;
import com.higgs.trust.slave.model.enums.biz.RsNodeStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author liuyu
 * @description
 * @date 2018-06-06
 */
@Slf4j @Service public class VoteServiceImpl implements VoteService {
    @Autowired private TransactionTemplate txRequired;
    @Autowired private RsConfig rsConfig;
    @Autowired private CoreTxRepository coreTxRepository;
    @Autowired private VoteReqRecordRepository voteReqRecordRepository;
    @Autowired private VoteReceiptRepository voteReceiptRepository;
    @Autowired private ServiceProviderClient serviceProviderClient;
    @Autowired private RsNodeRepository rsNodeRepository;
    @Autowired private SignService signService;
    @Autowired private TxCallbackRegistor txCallbackRegistor;

    @Autowired private ThreadPoolTaskExecutor syncVotingExecutorPool;
    @Autowired private ThreadPoolTaskExecutor asyncVotingExecutorPool;

    /**
     * request voting by each voter
     *
     * @param voters
     * @param coreTxBO
     * @param votePattern
     * @return
     */
    @Override public List<VoteReceipt> requestVoting(CoreTxBO coreTxBO, List<String> voters,
        VotePatternEnum votePattern) {
        if (CollectionUtils.isEmpty(voters)) {
            return null;
        }
        //get thread pool by vote pattern
        ThreadPoolTaskExecutor executor =
            (votePattern == VotePatternEnum.SYNC) ? syncVotingExecutorPool : asyncVotingExecutorPool;
        List<Future<VoteReceipt>> futureList = new ArrayList<>(voters.size());
        for (String voter : voters) {
            Future<VoteReceipt> future =
                executor.submit(new VotingExecutor(voter, coreTxRepository.convertTxVO(coreTxBO), votePattern));
            futureList.add(future);
        }
        List<VoteReceipt> receipts = new ArrayList<>();
        for (Future<VoteReceipt> future : futureList) {
            try {
                VoteReceipt receipt = future.get();
                if (receipt != null) {
                    receipts.add(receipt);
                }
            } catch (Throwable e) {
                log.error("[requestVoting]has error", e);
            }
        }
        return receipts;
    }

    @Override public VoteReceipt acceptVoting(VotingRequest votingRequest) {
        log.info("[acceptVoting]param:{}", votingRequest);
        CoreTransaction coreTx = votingRequest.getCoreTransaction();
        VoteRequestRecord voteRequestRecord = voteReqRecordRepository.queryByTxId(coreTx.getTxId());
        if (voteRequestRecord != null) {
            log.info("[acceptVoting]voteRequestRecord is already exist txId:{}", coreTx.getTxId());
            return makeVoteReceipt(coreTx.getTxId(), voteRequestRecord.getSign(), voteRequestRecord.getVoteResult());
        }
        final VoteReceipt[] receipts = {null};
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                try {
                    //save record
                    voteReqRecordRepository.add(makeVoteRequestRecord(votingRequest));
                    //process SYNC
                    if (StringUtils.equals(votingRequest.getVotePattern(), VotePatternEnum.SYNC.getCode())) {
                        VoteResultEnum voteResult = VoteResultEnum.AGREE;
                        try {
                            //check self node status
                            boolean r = checkSelfNodeStatus();
                            if(r){
                                //callback custom rs
                                txCallbackRegistor.onVote(votingRequest);
                            }else {
                                log.info("[acceptVoting]self.rs status is not COMMON");
                                voteResult = VoteResultEnum.DISAGREE;
                            }
                        } catch (Throwable e) {
                            log.error("[acceptVoting]callback custom has error", e);
                            voteResult = VoteResultEnum.DISAGREE;
                        }
                        log.info("[acceptVoting]txId:{},voteResult:{}", coreTx.getTxId(), voteResult);
                        String sign = null;
                        if (voteResult == VoteResultEnum.AGREE) {
                            //get sign info
                            SignInfo signInfo = signService.signTx(coreTx);
                            sign = signInfo.getSign();
                        }
                        //update sign info vote result
                        voteReqRecordRepository.setVoteResult(coreTx.getTxId(), sign, voteResult);
                        receipts[0] = makeVoteReceipt(coreTx.getTxId(), sign, voteResult);
                    } else {
                        //for ASYNC
                        receipts[0] = makeVoteReceipt(coreTx.getTxId(), null, VoteResultEnum.INIT);
                    }
                } catch (SlaveException e) {
                    if (e.getCode() == SlaveErrorEnum.SLAVE_IDEMPOTENT) {
                        log.info("[acceptVoting]voteRequestRecord is already exist txId:{}", coreTx.getTxId());
                        VoteRequestRecord voteRequestRecord = voteReqRecordRepository.queryByTxId(coreTx.getTxId());
                        receipts[0] = makeVoteReceipt(coreTx.getTxId(), voteRequestRecord.getSign(),
                            voteRequestRecord.getVoteResult());
                    } else {
                        log.error("[acceptVoting]has error", e);
                        throw e;
                    }
                }
            }
        });
        return receipts[0];
    }

    @Override public void receiptVote(String txId, boolean agree) {
        VoteRequestRecord voteRequestRecord = voteReqRecordRepository.queryByTxId(txId);
        if (voteRequestRecord == null) {
            log.info("[receiptVote]voteRequestRecord is not exist txId:{}", txId);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_VOTE_REQUEST_RECORD_NOT_EXIST);
        }
        //check status
        if (voteRequestRecord.getVoteResult() != VoteResultEnum.INIT) {
            log.info("[receiptVote]voteRequestRecord is already has result txId:{}", txId);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_VOTE_ALREADY_HAS_RESULT_ERROR);
        }
        CoreTransaction coreTx = JSON.parseObject(voteRequestRecord.getTxData(), CoreTransaction.class);
        //check self node status
        if(!TxTypeEnum.isTargetType(coreTx.getTxType(), TxTypeEnum.NODE)) {
            boolean r = checkSelfNodeStatus();
            if (!r) {
                log.info("[receiptVote]self.rs status is not COMMON txId:{}", txId);
                throw new RsCoreException(RsCoreErrorEnum.RS_CORE_RS_STATUS_NOT_COMMON_ERROR);
            }
        }
        VoteResultEnum voteResult = agree ? VoteResultEnum.AGREE : VoteResultEnum.DISAGREE;
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                String sign = null;
                if (agree) {
                    //get sign info
                    CoreTransaction coreTx = JSON.parseObject(voteRequestRecord.getTxData(), CoreTransaction.class);
                    SignInfo signInfo = signService.signTx(coreTx);
                    sign = signInfo.getSign();
                }
                //update result
                voteReqRecordRepository.setVoteResult(txId, sign, voteResult);
                try {
                    RespData<String> respData = receipting(voteRequestRecord.getSender(), makeVoteReceipt(txId, sign, voteResult));
                    if(!respData.isSuccess()){
                        throw new RsCoreException(RsCoreErrorEnum.getByCode(respData.getRespCode()));
                    }
                } catch (Throwable e) {
                    log.error("[receipting]has error", e);
                    throw new RsCoreException(RsCoreErrorEnum.RS_CORE_VOTE_RECEIPTING_HAS_ERROR);
                }
            }
        });
    }

    @Override public RespData<String> acceptReceipt(ReceiptRequest receiptRequest) {
        log.info("[acceptReceipt]param:{}", receiptRequest);
        RespData<String> respData = new RespData<>();
        CoreTransactionPO coreTransactionPO = coreTxRepository.queryByTxId(receiptRequest.getTxId(), false);
        if (coreTransactionPO == null) {
            respData.setCode(RsCoreErrorEnum.RS_CORE_TX_NOT_EXIST_ERROR.getCode());
            respData.setMsg(RsCoreErrorEnum.RS_CORE_TX_NOT_EXIST_ERROR.getDescription());
            log.error("[acceptReceipt] query core tx not exist by txId:{}", receiptRequest.getTxId());
            return respData;
        }
        //check
        VoteReceipt voteReceipt =
            voteReceiptRepository.queryForVoter(receiptRequest.getTxId(), receiptRequest.getVoter());
        if (voteReceipt != null) {
            log.info("[acceptReceipt] query vote receipt exist by txId:{},voter:{}", receiptRequest.getTxId(),
                receiptRequest.getVoter());
            return respData;
        }
        voteReceipt = BeanConvertor.convertBean(receiptRequest, VoteReceipt.class);
        voteReceipt.setVoteResult(VoteResultEnum.fromCode(receiptRequest.getVoteResult()));
        try {
            voteReceiptRepository.add(voteReceipt);
        } catch (SlaveException e) {
            if (e.getCode() == SlaveErrorEnum.SLAVE_IDEMPOTENT) {
                voteReceipt = voteReceiptRepository.queryForVoter(receiptRequest.getTxId(), receiptRequest.getVoter());
                if (voteReceipt != null) {
                    log.info("[acceptReceipt] query vote receipt exist by txId:{},voter:{}", receiptRequest.getTxId(),
                        receiptRequest.getVoter());
                    return respData;
                }
            } else {
                log.error("[acceptReceipt]has error", e);
                respData.setCode(e.getCode().getCode());
                respData.setMsg(e.getCode().getDescription());
            }
        } catch (Throwable e) {
            log.error("[acceptReceipt]has error", e);
            respData.setCode(RsCoreErrorEnum.RS_CORE_UNKNOWN_EXCEPTION.getCode());
            respData.setMsg(RsCoreErrorEnum.RS_CORE_UNKNOWN_EXCEPTION.getDescription());
        }
        return respData;
    }

    @Override public List<SignInfo> getSignInfos(List<VoteReceipt> receipts,SignInfo.SignTypeEnum signType) {
        List<SignInfo> signInfos = new ArrayList<>(receipts.size());
        for (VoteReceipt receipt : receipts) {
            SignInfo signInfo = new SignInfo();
            signInfo.setOwner(receipt.getVoter());
            signInfo.setSign(receipt.getSign());
            signInfo.setSignType(signType);
            signInfos.add(signInfo);
        }
        return signInfos;
    }

    @Override public boolean getDecision(List<VoteReceipt> receipts, DecisionTypeEnum decisionType) {
        log.info("[getDecision]decisionType:{},receipts:{}", decisionType, receipts);
        if (decisionType == DecisionTypeEnum.FULL_VOTE) {
            //require all RS voting
            for (VoteReceipt receipt : receipts) {
                if (receipt.getVoteResult() == VoteResultEnum.DISAGREE) {
                    return false;
                }
            }
            return true;
        } else if (decisionType == DecisionTypeEnum.ONE_VOTE) {
            //just one agree
            for (VoteReceipt receipt : receipts) {
                if (receipt.getVoteResult() == VoteResultEnum.AGREE) {
                    return true;
                }
            }
            return false;
        }
        //default
        return false;
    }

    @Override public List<VoteRequestRecord> queryAllInitRequest(int row, int count) {
        if(row < 0){
            row = 0;
        }
        if(count <= 0){
            count = 10;
        }
        return voteReqRecordRepository.queryAllInitRequest(row,count);
    }

    @Override public List<String> getVoters(List<SignInfo> signInfos, List<String> rsIds) {
        if (CollectionUtils.isEmpty(rsIds)) {
            return null;
        }
        if(CollectionUtils.isEmpty(signInfos)){
            return rsIds;
        }
        //make sign map,key:rsId,value:sign
        Map<String, SignInfo> signInfoMap = SignInfo.makeSignMap(signInfos);
        List<String> voters = new ArrayList<>(rsIds.size());
        for (String rs : rsIds) {
            //filter already voting
            if (signInfoMap.containsKey(rs)) {
                continue;
            }
            voters.add(rs);
        }
        return voters;
    }

    /**
     * voting executor inner class
     */
    class VotingExecutor implements Callable<VoteReceipt> {
        private String voter;
        private CoreTransaction coreTx;
        private VotePatternEnum votePattern;

        public VotingExecutor(String voter, CoreTransaction coreTx, VotePatternEnum votePattern) {
            this.voter = voter;
            this.coreTx = coreTx;
            this.votePattern = votePattern;
        }

        @Override public VoteReceipt call() throws Exception {
            return requestVoting();
        }

        /**
         * request voting use HTTP or FeignClient channel
         *
         * @return
         */
        private VoteReceipt requestVoting() throws Exception {
            log.info("[requestVoting]voter:{}", voter);
            log.info("[requestVoting]coreTx:{}", coreTx);
            boolean useHttpChannel = rsConfig.isUseHttpChannel();
            log.info("[requestVoting]useHttpChannel:{}", useHttpChannel);
            VotingRequest request = new VotingRequest(rsConfig.getRsName(), coreTx, votePattern.getCode());
            if (useHttpChannel) {
                return byHttp(voter, request);
            }
            return serviceProviderClient.voting(voter, request);
        }

        /**
         * request voting by http channel
         *
         * @param voter
         * @param request
         * @return
         */
        private VoteReceipt byHttp(String voter, VotingRequest request) throws Exception {
            String url = "http://" + voter + ":" + rsConfig.getServerPort() + "/voting";
            log.info("[byHttp]url:" + url);
            String paramJSON = JSON.toJSONString(request);
            log.info("[byHttp]paramJSON:" + paramJSON);
            String resultJSON = OkHttpClientManager.postAsString(url, paramJSON, rsConfig.getSyncRequestTimeout() / 2);
            log.info("[byHttp]resultJSON:" + resultJSON);
            return JSON.parseObject(resultJSON, VoteReceipt.class);
        }
    }

    /**
     * receipt vote result
     *
     * @param sender
     * @param voteReceipt
     */
    private RespData<String> receipting(String sender, VoteReceipt voteReceipt) throws Exception {
        log.info("[receipting]voteReceipt:{}", voteReceipt);
        boolean useHttpChannel = rsConfig.isUseHttpChannel();
        log.info("[receipting]useHttpChannel:{}", useHttpChannel);
        ReceiptRequest request = BeanConvertor.convertBean(voteReceipt, ReceiptRequest.class);
        request.setVoteResult(voteReceipt.getVoteResult().getCode());
        if (!useHttpChannel) {
           return serviceProviderClient.receipting(sender, request);
        }
        String url = "http://" + sender + ":" + rsConfig.getServerPort() + "/receipting";
        log.info("[receipting.byHttp]url:" + url);
        String paramJSON = JSON.toJSONString(request);
        log.info("[receipting.byHttp]paramJSON:" + paramJSON);
        String resultJSON = OkHttpClientManager.postAsString(url, paramJSON, rsConfig.getSyncRequestTimeout() / 2);
        log.info("[receipting.byHttp]resultJSON:" + resultJSON);
        return JSON.parseObject(resultJSON,RespData.class);
    }

    /**
     * check rs status for self
     * @return
     */
    private boolean checkSelfNodeStatus(){
        RsNode rsNode = rsNodeRepository.queryByRsId(rsConfig.getRsName());
        if(rsNode == null){
            return false;
        }
        return rsNode.getStatus() == RsNodeStatusEnum.COMMON;
    }

    /**
     * make new record
     *
     * @param votingRequest
     * @return
     */
    private VoteRequestRecord makeVoteRequestRecord(VotingRequest votingRequest) {
        VoteRequestRecord requestRecord = new VoteRequestRecord();
        requestRecord.setTxId(votingRequest.getCoreTransaction().getTxId());
        requestRecord.setSender(votingRequest.getSender());
        requestRecord.setTxData(JSON.toJSONString(votingRequest.getCoreTransaction()));
        return requestRecord;
    }

    /**
     * make new receipt
     *
     * @param txId
     * @param sign
     * @param voteResultEnum
     * @return
     */
    private VoteReceipt makeVoteReceipt(String txId, String sign, VoteResultEnum voteResultEnum) {
        VoteReceipt receipt = new VoteReceipt();
        receipt.setTxId(txId);
        receipt.setVoter(rsConfig.getRsName());
        receipt.setSign(sign);
        receipt.setVoteResult(voteResultEnum);
        return receipt;
    }
}

package com.higgs.trust.slave.core.service.pack;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.Labels;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.slave.api.vo.PackageVO;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.PackageRepository;
import com.higgs.trust.slave.core.repository.RsPubKeyRepository;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.core.service.consensus.p2p.P2pHandler;
import com.higgs.trust.slave.core.service.pending.PendingState;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.enums.BlockHeaderTypeEnum;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import com.higgs.trust.slave.model.enums.biz.PendingTxStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Description: package service
 * @author: pengdi
 **/
@Service @Slf4j public class PackageServiceImpl implements PackageService {

    @Autowired private PackageRepository packageRepository;

    @Autowired private BlockRepository blockRepository;

    @Autowired private BlockService blockService;

    @Autowired private PendingState pendingState;

    @Autowired private PackagePersistor packagePersistor;

    @Autowired private PackageValidator packageValidator;

    @Autowired private P2pHandler p2pHandler;

    @Autowired private TransactionTemplate txNested;

    @Autowired private NodeState nodeState;

    @Autowired private RsPubKeyRepository rsPubKeyRepository;

    @Value("${trust.batch.tx.limit}")
    private int count;

    private static final Long DEFAULT_HEIGHT = 1L;

    /**
     * create new package from pending transactions
     *
     * @return
     */
    @Override public Package create() {
        List<SignedTransaction> signedTransactions = pendingState.getPendingTransactions(count);

        if (CollectionUtils.isEmpty(signedTransactions)) {
            return null;
        }

        Long height = getHeight();

        if (null == height) {
            return null;
        }

        log.info("[PackageScheduler.createPackage] start create package, txSize: {}, txList: {}, package.height: {}",
            signedTransactions.size(), signedTransactions, height + 1);

        /**
         * initial package
         */
        Package pack = new Package();
        pack.setSignedTxList(signedTransactions);
        pack.setPackageTime(System.currentTimeMillis());

        //get max height, add 1 for next package height
        pack.setHeight(height + 1);
        return pack;
    }

    /**
     * package status change function
     *
     * @param pack
     * @param from
     * @param to
     */
    @Override public void statusChange(Package pack, PackageStatusEnum from, PackageStatusEnum to) {
        packageRepository.updateStatus(pack.getHeight(), from, to);
    }

    private Long getHeight() {
        Long maxPackHeight = packageRepository.getMaxHeight();
        Long maxBlockHeight = blockRepository.getMaxHeight();

        //first create package. return default height
        if (null == maxPackHeight && null == maxBlockHeight) {
            log.info("first create package. return default height");
            return DEFAULT_HEIGHT;
        }

        if (null == maxPackHeight && null != maxBlockHeight) {
            return maxBlockHeight;
        }

        //if max package height equals max block height, return max package height
        if (null == maxBlockHeight || maxBlockHeight.compareTo(maxPackHeight) == 0) {
            return maxPackHeight;
        }

        if (maxBlockHeight.compareTo(maxPackHeight) < 0) {
            /**
             * get minPackHeight and count which status equals 'INIT' or 'RECEIVED' or 'SUBMIT_CONSENSUS_SUCCESS'
             */
            Set<String> statusSet = new HashSet<String>() {{
                add(PackageStatusEnum.INIT.getCode());
                add(PackageStatusEnum.SUBMIT_CONSENSUS_SUCCESS.getCode());
                add(PackageStatusEnum.RECEIVED.getCode());
                add(PackageStatusEnum.VALIDATING.getCode());
                add(PackageStatusEnum.WAIT_VALIDATE_CONSENSUS.getCode());
                add(PackageStatusEnum.VALIDATED.getCode());
                add(PackageStatusEnum.PERSISTING.getCode());
                add(PackageStatusEnum.WAIT_PERSIST_CONSENSUS.getCode());
            }};
            long minPackHeight = packageRepository.getMinHeight(statusSet);
            long count = packageRepository.count(statusSet);

            if (0 != minPackHeight && 0 != count) {
                long result = count + minPackHeight - 1;

                /**
                 * check block height is continuous or not
                 */
                if (result != maxPackHeight || minPackHeight != (maxBlockHeight + 1)) {
                    log.error("block height is not continuous");
                    //TODO 添加告警
                    return null;
                }
            } else {
                log.error("package height is invalid. ");
                //TODO 添加告警
                return null;
            }

        }

        return maxPackHeight;
    }

    /**
     * receive new package from somewhere, almost from consensus
     *
     * @param pack
     */
    @Override public void receive(Package pack) {
        log.info("receive package from consensus, pack: {}", pack);

        if (null == pack || CollectionUtils.isEmpty(pack.getSignedTxList())) {
            log.error("package is null or transaction list is empty.");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        if (!checkTransactions(pack.getSignedTxList())) {
            log.error("transaction list is not order by txId asc.");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        Package packageBO = packageRepository.load(pack.getHeight());
        // check package hash
        if (null != packageBO) {
            boolean checkHash = StringUtils.equals(buildPackHash(pack), buildPackHash(packageBO));
            if (!checkHash) {
                log.error("receive package is not the same as db package");
                //TODO 添加告警
                throw new SlaveException(SlaveErrorEnum.SLAVE_UNKNOWN_EXCEPTION);
            }
        }

        if (nodeState.isMaster()) {
            if (null == packageBO || CollectionUtils.isEmpty(packageBO.getSignedTxList())) {
                log.error("system exception. packageBO is null or transaction list is empty.");
                //TODO 增加告警信息
                throw new SlaveException(SlaveErrorEnum.SLAVE_UNKNOWN_EXCEPTION);
            }

            if (PackageStatusEnum.RECEIVED.equals(packageBO.getStatus())) {
                log.warn("package already exists. {}", pack);
                return;
            }
            packageRepository
                .updateStatus(pack.getHeight(), PackageStatusEnum.SUBMIT_CONSENSUS_SUCCESS, PackageStatusEnum.RECEIVED);
        } else {
            if (null != packageBO) {
                log.warn("package already exists. {}", pack);
                return;
            }

            pack.setStatus(PackageStatusEnum.RECEIVED);
            txNested.execute(new TransactionCallbackWithoutResult() {
                @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    packageRepository.save(pack);
                    pendingState
                        .addPendingTransactions(pack.getSignedTxList(), PendingTxStatusEnum.PACKAGED, pack.getHeight());
                }
            });
        }
    }

    /**
     * check transaction list if sorted by txId
     *
     * @param signedTxList
     * @return
     */
    private boolean checkTransactions(List<SignedTransaction> signedTxList) {
        for (int i = 0; i < signedTxList.size() - 1; i++) {
            String firstTxId = signedTxList.get(i).getCoreTx().getTxId();
            String secondTxId = signedTxList.get(i + 1).getCoreTx().getTxId();
            if (firstTxId.compareTo(secondTxId) > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * create pack context for main process
     *
     * @param pack
     * @return
     */
    @Override public PackContext createPackContext(Package pack) {
        Block block = blockService.buildDummyBlock(pack.getHeight(), pack.getPackageTime());
        return new PackContext(pack, block);
    }

    /**
     * execute package validating, get validate result and submit consensus layer
     *
     * @param packContext
     */
    @Override public void validating(PackContext packContext) {
        log.info("validating package start, package context: {}", packContext);
        packageValidator.validating(packContext);
        log.info("validating package finish");
    }

    /**
     * get validate result and submit consensus layer
     *
     * @param pack
     */
    @Override public void validateConsensus(Package pack) {
        log.info("send validating header to consensus start, {}", pack);

        // get the temp header created by the "validating" function
        BlockHeader header = blockService.getTempHeader(pack.getHeight(), BlockHeaderTypeEnum.TEMP_TYPE);
        if (header == null) {
            log.error("[PackageService.validateConsensus] send header to consensus failed, no validating block header");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_HEADER_IS_NULL_ERROR);
        }
        p2pHandler.sendValidating(header);
        log.info("send validating header to consensus finish");
    }

    /**
     * receive validate final result from consensus layer
     *
     * @param pack
     */
    @Override public void validated(Package pack) {
        packageValidator.validated(pack);
    }

    /**
     * execute package persisting, get persist result and submit consensus layer
     *
     * @param packContext
     */
    @Override public void persisting(PackContext packContext) {
        log.info("persisting package start, package context: {}", packContext);
        packagePersistor.persisting(packContext);
        log.info("persisting package finish");
    }

    /**
     * get persist result and submit consensus layer
     *
     * @param pack
     */
    @Override public void persistConsensus(Package pack) {
        log.info("send persisting header to consensus start, package:{}", pack);
        BlockHeader header = blockService.getHeader(pack.getHeight());
        if (header == null) {
            log.error("[PackageService.persistConsensus] send header to consensus failed, no persisting block header");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_HEADER_IS_NULL_ERROR);
        }
        p2pHandler.sendPersisting(header);
        log.info("send persisting header to consensus finish");
    }

    /**
     * receive persist final result from consensus layer
     *
     * @param pack
     */
    @Override public void persisted(Package pack) {
        packagePersistor.persisted(pack);
    }

    /**
     * remove the package if it is done
     *
     * @param pack
     */
    @Override public void remove(Package pack) {
        /** TODO
         * 1.判断package是否执行完成
         * 2.开启事务
         * 3.通过packageRepository删除包
         * 4.通过pendingState删除交易
         * 5.提交事务
         */
    }

    @Override public String getSign(PackageVO packageVO) {
        try {
            String dataString = JSON.toJSONString(packageVO, Labels.excludes("sign"));
            return SignUtils.sign(dataString, nodeState.getPrivateKey());
        } catch (Exception e) {
            log.error("package sign exception. ", e);
            return null;
        }
    }

    /**
     * build hash for package
     *
     * @param pack
     * @return
     */
    private String buildPackHash(Package pack) {
        HashFunction function = Hashing.sha256();
        StringBuilder builder = new StringBuilder();
        builder.append(function.hashLong(pack.getHeight()));
        builder.append(function.hashLong(pack.getPackageTime()));
        builder.append(function.hashString(getSafety(JSON.toJSONString(pack.getSignedTxList())), Charsets.UTF_8));
        String hash = function.hashString(builder.toString(), Charsets.UTF_8).toString();
        return hash;
    }

    private String getSafety(String data) {
        if (data == null) {
            return "";
        }
        return data;
    }
}

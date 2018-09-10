package com.higgs.trust.rs.core.repository;

import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.common.dao.RocksUtils;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.dao.po.CoreTransactionProcessPO;
import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class CoreTxProcessRepositoryTest extends IntegrateBaseTest{
    @Autowired
    private CoreTxProcessRepository coreTxProcessRepository;
    @Test public void testAdd() throws Exception {
        ThreadLocalUtils.putWriteBatch(new WriteBatch());
        for (int i = 0; i < 100; i++) {
            coreTxProcessRepository.add("test-tx-id-" + i, CoreTxStatusEnum.INIT);
        }
        RocksUtils.batchCommit(new WriteOptions(), ThreadLocalUtils.getWriteBatch());
        ThreadLocalUtils.clearWriteBatch();
    }

    @Test public void testQueryByTxId() throws Exception {
        CoreTransactionProcessPO po =
            coreTxProcessRepository.queryByTxId("test-tx-id-1", CoreTxStatusEnum.INIT);
        Assert.assertEquals(po.getStatus(), CoreTxStatusEnum.INIT.getCode());
    }

    @Test public void testQueryByStatus() throws Exception {
        List<CoreTransactionProcessPO> list = coreTxProcessRepository.queryByStatus(CoreTxStatusEnum.END, 0, 20);
        Assert.assertEquals(list.size(), 10);
    }

    @Test public void testUpdateStatus() throws Exception {

//        for (int i = 99; i >= 80; i -= 3) {
            ThreadLocalUtils.putWriteBatch(new WriteBatch());
            coreTxProcessRepository.updateStatus("test-tx-id-23", CoreTxStatusEnum.INIT, CoreTxStatusEnum.END);
            RocksUtils.batchCommit(new WriteOptions(), ThreadLocalUtils.getWriteBatch());
            ThreadLocalUtils.clearWriteBatch();
//        }
    }

    @Test public void testBatchInsert() throws Exception {
        List<RsCoreTxVO> rsCoreTxVOList = new ArrayList<>();
        for (int i = 100; i < 200; i++){
            RsCoreTxVO rsCoreTxVO = new RsCoreTxVO();
            rsCoreTxVO.setTxId("test-tx-id-" + i);
            rsCoreTxVOList.add(rsCoreTxVO);
        }

        ThreadLocalUtils.putWriteBatch(new WriteBatch());
        coreTxProcessRepository.batchInsert(rsCoreTxVOList, CoreTxStatusEnum.PERSISTED);
        RocksUtils.batchCommit(new WriteOptions(), ThreadLocalUtils.getWriteBatch());
        ThreadLocalUtils.clearWriteBatch();
    }

    @Test public void testBatchUpdateStatus() throws Exception {
        List<RsCoreTxVO> rsCoreTxVOList = new ArrayList<>();
        for (int i = 50; i < 70; i += 2){
            RsCoreTxVO rsCoreTxVO = new RsCoreTxVO();
            rsCoreTxVO.setTxId("test-tx-id-" + i);
            rsCoreTxVOList.add(rsCoreTxVO);
        }
        ThreadLocalUtils.putWriteBatch(new WriteBatch());
        coreTxProcessRepository.batchUpdateStatus(rsCoreTxVOList, CoreTxStatusEnum.INIT, CoreTxStatusEnum.END);
        RocksUtils.batchCommit(new WriteOptions(), ThreadLocalUtils.getWriteBatch());
        ThreadLocalUtils.clearWriteBatch();
    }

    @Test public void testDeleteEnd() throws Exception {
        coreTxProcessRepository.deleteEnd();

        List<CoreTransactionProcessPO> list = coreTxProcessRepository.queryByStatus(CoreTxStatusEnum.END, 0, 1000);
        Assert.assertEquals(list.size(), 0);
    }
}
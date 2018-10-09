package com.higgs.trust.slave.dao.rocks.pack;

import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.Transaction;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;

/**
 * @author tangfashuang
 * @desc key: status_height, value: height
 */
@Service
@Slf4j
public class PackStatusRocksDao extends RocksBaseDao<Long> {
    private static final int LOAD_LIMIT = 30;

    @Override protected String getColumnFamilyName() {
        return "packageStatus";
    }

    public Long getMaxHeightByStatus(String status) {

        List<String> indexList = PackageStatusEnum.getIndexs(status);
        for (String index : indexList) {
            if (!StringUtils.isEmpty(queryFirstKey(index))) {
                return queryForPrev(index);
            }
        }
        return queryLastValueWithPrefix(status);
    }

    public Long getMinHeightByStatus(String status) {
        return queryFirstValueByPrefix(status);
    }

    public void save(Long height, String status) {

        DecimalFormat df = new DecimalFormat(Constant.PACK_STATUS_HEIGHT_FORMAT);
        String key = status + Constant.SPLIT_SLASH + df.format(height);
        if (keyMayExist(key) && null != get(key)) {
            log.error("[PackStatusRocksDao.save] height and status is exist, key={}", key);
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_ALREADY_EXIST);
        }

        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[PackStatusRocksDao.save] transaction is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_TRANSACTION_IS_NULL);
        }

        txPut(tx, key, height);
    }

    public void batchDelete(Long height, String status) {
        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[PackStatusRocksDao.batchDelete] transaction is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_TRANSACTION_IS_NULL);
        }

        DecimalFormat df = new DecimalFormat(Constant.PACK_STATUS_HEIGHT_FORMAT);
        String key = status + Constant.SPLIT_SLASH + df.format(height);

        txDelete(tx, key);
    }

    public void update(Long height, String from , String to) {
        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[PackStatusRocksDao.update] transaction is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_TRANSACTION_IS_NULL);
        }

        DecimalFormat df = new DecimalFormat(Constant.PACK_STATUS_HEIGHT_FORMAT);
        String key = from + Constant.SPLIT_SLASH + df.format(height);
        if (!keyMayExist(key) && null != get(key)) {
            log.error("[PackStatusRocksDao.update] height and status is not exist, key={}", key);
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_IS_NOT_EXIST);
        }

        //delete
        txDelete(tx, key);

        //put
        String newKey = to + Constant.SPLIT_SLASH + df.format(height);
        txPut(tx, newKey, height);
    }

    public String getStatusByHeight(Long height) {
        List<String> indexList = PackageStatusEnum.getIndexs(null);
        DecimalFormat df = new DecimalFormat(Constant.PACK_STATUS_HEIGHT_FORMAT);

        for (String index : indexList) {
            String key = index + Constant.SPLIT_SLASH + df.format(height);
            if (keyMayExist(key) && null != get(key)) {
                return PackageStatusEnum.getByIndex(index).getCode();
            }
        }
        return null;
    }

    public List<Long> queryHeightListByHeight(Long height) {
        DecimalFormat df = new DecimalFormat(Constant.PACK_STATUS_HEIGHT_FORMAT);
        String position = PackageStatusEnum.RECEIVED.getIndex() + Constant.SPLIT_SLASH + df.format(height);
        return queryByPrefix(PackageStatusEnum.RECEIVED.getIndex(), LOAD_LIMIT, position);
    }

    public List<Long> queryByStatusAndLessThanHeight(String index, Long height) {
        DecimalFormat df = new DecimalFormat(Constant.PACK_STATUS_HEIGHT_FORMAT);
        String position = index + Constant.SPLIT_SLASH + df.format(height);
        return queryLessThanByPrefixAndPosition(index, position);
    }
}

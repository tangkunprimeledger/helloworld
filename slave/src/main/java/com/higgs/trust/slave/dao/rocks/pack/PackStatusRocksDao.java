package com.higgs.trust.slave.dao.rocks.pack;

import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.WriteBatch;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author tangfashuang
 * @desc key: status_height, value: height
 */
@Service
@Slf4j
public class PackStatusRocksDao extends RocksBaseDao<Long>{
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
        return queryLastValue();
    }


    public Long getMinHeightByStatus(String status) {
        return queryFirstValueByPrefix(status);
    }

    public void save(Long height, String status) {
        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[PackStatusRocksDao.save] write batch is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        DecimalFormat df = new DecimalFormat(Constant.PACK_STATUS_HEIGHT_FORMAT);
        String key = status + Constant.SPLIT_SLASH + df.format(height);
        if (keyMayExist(key) && null != get(key)) {
            log.error("[PackStatusRocksDao.save] height and status is exist, key={}", key);
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_ALREADY_EXIST);
        }

        batchPut(batch, key, height);
    }

    public void batchDelete(Long height, String status) {
        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[PackStatusRocksDao.batchDelete] write batch is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        DecimalFormat df = new DecimalFormat(Constant.PACK_STATUS_HEIGHT_FORMAT);
        String key = status + Constant.SPLIT_SLASH + df.format(height);

        batchDelete(batch, key);
    }

    public void update(Long height, String from , String to) {
        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[PackStatusRocksDao.update] write batch is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        DecimalFormat df = new DecimalFormat(Constant.PACK_STATUS_HEIGHT_FORMAT);
        String key = from + Constant.SPLIT_SLASH + df.format(height);
        if (!keyMayExist(key) && null != get(key)) {
            log.error("[PackStatusRocksDao.update] height and status is not exist, key={}", key);
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_IS_NOT_EXIST);
        }

        //delete
        batchDelete(batch, key);

        //put
        String newKey = to + Constant.SPLIT_SLASH + df.format(height);
        batchPut(batch, newKey, height);
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
}

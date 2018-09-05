package com.higgs.trust.slave.dao.rocks.pack;

import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.pack.PackagePO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.WriteBatch;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;

/**
 * @author tangfashuang
 */
@Service
@Slf4j
public class PackRocksDao extends RocksBaseDao<PackagePO> {
    @Override protected String getColumnFamilyName() {
        return "package";
    }

    public void save(PackagePO po) {

        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[PackRocksDao.save] write batch is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        String height = String.valueOf(po.getHeight());
        if (keyMayExist(height) && null != get(height)) {
            log.error("[PackRocksDao.save] package is already exist. height={}", height);
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_ALREADY_EXIST);
        }
        po.setCreateTime(new Date());
        batchPut(batch, height, po);
    }

    public void updateStatus(Long height, String from, String to) {
        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[PackRocksDao.updateStatus] write batch is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        String heightStr = String.valueOf(height);
        PackagePO po = get(heightStr);
        if (null == po) {
            log.error("[PackageRocksDao.updateStatus] package is not exist, height={}", height);
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_IS_NOT_EXIST);
        }

        if (!StringUtils.equals(po.getStatus(), from)) {
            log.error("[PackRocksDao.updateStatus] status is not equals, po.status = {}, from = {}", po.getStatus(), from);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_UPDATE_STATUS_ERROR);
        }

        po.setUpdateTime(new Date());
        po.setStatus(to);

        batchPut(batch, heightStr, po);
    }

    public List<Long> queryHeightListByHeight(List<String> packHeights) {
        List<Long> heights = new ArrayList<>();
        Map<String, PackagePO> resultMap = multiGet(packHeights);
        if (!MapUtils.isEmpty(resultMap)) {
            for (String key : resultMap.keySet()) {
                if (!StringUtils.isEmpty(key)) {
                    heights.add(Long.parseLong(key));
                }
            }
        }

        //sort height asc
        Collections.sort(heights);
        return heights;
    }

    public void batchDelete(Long h) {
        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[PackStatusRocksDao.batchDelete] write batch is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        batchDelete(batch, String.valueOf(h));
    }
}

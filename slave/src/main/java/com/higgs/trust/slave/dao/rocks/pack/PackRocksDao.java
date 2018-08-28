package com.higgs.trust.slave.dao.rocks.pack;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.pack.PackagePO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteBatch;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author tangfashuang
 */
@Service
@Slf4j
public class PackRocksDao extends RocksBaseDao<Long, PackagePO> {
    @Override protected String getColumnFamilyName() {
        return "package";
    }

    public void save(PackagePO po) {
        if (null != get(po.getHeight())) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_ALREADY_EXIST);
        }
        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[PackRocksDao.save] write batch is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_WRITE_BATCH_IS_NULL);
        }
        po.setCreateTime(new Date());
        batchPut(batch, po.getHeight(), po);
    }

    public void updateStatus(Long height, String from, String to) {
        PackagePO po = get(height);
        if (null == height) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_IS_NOT_EXIST);
        }

        if (!StringUtils.equals(po.getStatus(), from)) {
            log.error("[PackRocksDao.updateStatus] status is not equals, po.status = {}, from = {}", po.getStatus(), from);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_UPDATE_STATUS_ERROR);
        }

        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[PackRocksDao.updateStatus] write batch is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_WRITE_BATCH_IS_NULL);
        }
        po.setUpdateTime(new Date());
        po.setStatus(to);

        batchPut(batch, height, po);
    }

    public List<Long> queryHeightListByHeight(List<Long> packHeights) {
        List<Long> heights = new ArrayList<>();
        Map<Long, PackagePO> resultMap = multiGet(packHeights);
        if (!MapUtils.isEmpty(resultMap)) {
            for (Long key : resultMap.keySet()) {
                heights.add(key);
            }
        }

        return heights;
    }
}

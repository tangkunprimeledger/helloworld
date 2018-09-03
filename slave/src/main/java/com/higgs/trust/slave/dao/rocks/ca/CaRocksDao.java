package com.higgs.trust.slave.dao.rocks.ca;

import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.ca.CaPO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
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
public class CaRocksDao extends RocksBaseDao<CaPO>{
    @Override protected String getColumnFamilyName() {
        return "ca";
    }

    public void save(CaPO caPO) {
        String key = caPO.getUser() + Constant.SPLIT_SLASH + caPO.getUsage();
        if (null != get(key)) {
            log.error("[CaRocksDao.save] ca is exist. key={}", key);
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_ALREADY_EXIST);
        }
        caPO.setCreateTime(new Date());
        put(key, caPO);
    }

    public void update(CaPO caPO) {
        String key = caPO.getUser() + Constant.SPLIT_SLASH + caPO.getUsage();
        if (null == get(key)) {
            log.error("[CaRocksDao.update] ca is not exits, key={}", key);
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_IS_NOT_EXIST);
        }
        caPO.setUpdateTime(new Date());
        put(key, caPO);
    }

    public int batchInsert(List<CaPO> caPOList) {
        if (CollectionUtils.isEmpty(caPOList)) {
            return 0;
        }

        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[CaRocksDao.batchInsert] write batch is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        for (CaPO po : caPOList) {
            String key = po.getUser() + Constant.SPLIT_SLASH + po.getUsage();
            if (null == po.getCreateTime()) {
                po.setCreateTime(new Date());
            } else {
                po.setUpdateTime(new Date());
            }
            batchPut(batch, key, po);
        }

        return caPOList.size();
    }

    public List<CaPO> getCaListByUsers(List<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return null;
        }

        Map<String, CaPO> resultMap = multiGet(keys);
        if (MapUtils.isEmpty(resultMap)) {
            return null;
        }

        List<CaPO> caPOS = new ArrayList<>(resultMap.size());
        for (String key : resultMap.keySet()) {
            caPOS.add(resultMap.get(key));
        }

        return caPOS;
    }
}

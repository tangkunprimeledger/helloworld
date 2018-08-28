package com.higgs.trust.slave.dao.rocks.config;

import com.google.common.collect.Lists;
import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.config.ConfigPO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.WriteBatch;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author tangfashuang
 */
@Service
@Slf4j
public class ConfigRocksDao extends RocksBaseDao<String, ConfigPO>{
    @Override protected String getColumnFamilyName() {
        return "config";
    }

    public void save(ConfigPO configPO) {
        String key = configPO.getNodeName() + Constant.SPLIT_SLASH + configPO.getUsage();
        if (null != get(key)) {
            log.error("[ConfigRocksDao.save] config is exist. key={}", key);
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_ALREADY_EXIST);
        }
        configPO.setCreateTime(new Date());
        put(key, configPO);
    }

    public void update(ConfigPO configPO) {
        String key = configPO.getNodeName() + Constant.SPLIT_SLASH + configPO.getUsage();
        if (null == get(key)) {
            log.error("[ConfigRocksDao.save] config is not exist. key={}", key);
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_IS_NOT_EXIST);
        }
        configPO.setUpdateTime(new Date());
        put(key, configPO);
    }

    public int batchInsert(List<ConfigPO> configPOList) {
        if (CollectionUtils.isEmpty(configPOList)) {
            return 0;
        }

        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[ConfigRocksDao.batchInsert] write batch is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_WRITE_BATCH_IS_NULL);
        }
        for (ConfigPO po : configPOList) {
            String key = po.getNodeName() + Constant.SPLIT_SLASH + po.getUsage();
            if (null == po.getCreateTime()) {
                po.setCreateTime(new Date());
            } else {
                po.setUpdateTime(new Date());
            }
            batchPut(batch, key, po);
        }
        return configPOList.size();
    }

    public List<ConfigPO> getConfig(String nodeName, String usage) {
        if (StringUtils.isEmpty(usage)) {
            return queryByPrev(nodeName);
        }

        ConfigPO po = get(nodeName + Constant.SPLIT_SLASH + usage);
        if (null == po) {
            return null;
        }
        return Lists.newArrayList(po);
    }
}

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
import org.rocksdb.Transaction;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author tangfashuang
 * @desc key: nodeName_usage, value: ConfigPO
 */
@Service
@Slf4j
public class ConfigRocksDao extends RocksBaseDao<ConfigPO>{
    @Override protected String getColumnFamilyName() {
        return "config";
    }

    public void save(ConfigPO configPO) {
        String key = configPO.getNodeName() + Constant.SPLIT_SLASH + configPO.getUsage();
        if (keyMayExist(key) && null != get(key)) {
            log.error("[ConfigRocksDao.save] config is exist. key={}", key);
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_ALREADY_EXIST);
        }
        configPO.setCreateTime(new Date());
        put(key, configPO);
    }

    public void update(ConfigPO configPO) {
        String key = configPO.getNodeName() + Constant.SPLIT_SLASH + configPO.getUsage();
        if (null == get(key)) {
            log.error("[ConfigRocksDao.update] config is not exist. key={}", key);
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_IS_NOT_EXIST);
        }
        configPO.setUpdateTime(new Date());

        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            put(key, configPO);
        } else {
            txPut(tx, key, configPO);
        }
    }

    public int batchInsert(List<ConfigPO> configPOList) {
        if (CollectionUtils.isEmpty(configPOList)) {
            return 0;
        }

       Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[ConfigRocksDao.batchInsert] transaction is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_TRANSACTION_IS_NULL);
        }
        for (ConfigPO po : configPOList) {
            String key = po.getNodeName() + Constant.SPLIT_SLASH + po.getUsage();
            if (null == po.getCreateTime()) {
                po.setCreateTime(new Date());
            } else {
                po.setUpdateTime(new Date());
            }
            txPut(tx, key, po);
        }
        return configPOList.size();
    }

    public List<ConfigPO> getConfig(String nodeName, String usage) {
        if (StringUtils.isEmpty(usage)) {
            return queryByPrefix(nodeName);
        }

        ConfigPO po = get(nodeName + Constant.SPLIT_SLASH + usage);
        if (null == po) {
            return null;
        }
        return Lists.newArrayList(po);
    }

    public void batchCancel(List<String> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }

        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[ConfigRocksDao.batchCancel] transaction is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_TRANSACTION_IS_NULL);
        }

        String usage = "biz";
        for (String node : nodes) {
            String key = node + Constant.SPLIT_SLASH + usage;
            ConfigPO po = get(key);
            if (null == po) {
                log.error("[ConfigRocksDao.batchCancel] config is not exist, key={}", key);
                throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_IS_NOT_EXIST);
            }
            po.setUpdateTime(new Date());
            po.setValid(false);
            txPut(tx, key, po);
        }
    }

    public void batchEnable(List<String> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }

        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[ConfigRocksDao.batchEnable] transaction is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_TRANSACTION_IS_NULL);
        }

        String usage = "biz";
        for (String node : nodes) {
            String key = node + Constant.SPLIT_SLASH + usage;
            ConfigPO po = get(key);
            if (null == po) {
                log.error("[ConfigRocksDao.batchEnable] config is not exist, key={}", key);
                throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_KEY_IS_NOT_EXIST);
            }
            po.setUpdateTime(new Date());
            po.setPriKey(po.getTmpPriKey());
            po.setPubKey(po.getTmpPubKey());
            po.setValid(true);
            txPut(tx, key, po);
        }
    }
}

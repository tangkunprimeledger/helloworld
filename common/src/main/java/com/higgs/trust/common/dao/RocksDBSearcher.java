package com.higgs.trust.common.dao;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-09-13
 */
@Component public class RocksDBSearcher extends RocksBaseDao<Object> {
    /**
     * the definition of table name
     */
    private String tableName;

    @Override protected String getColumnFamilyName() {
        return tableName;
    }

    /**
     * query
     *
     * @param tableName
     * @param key
     * @return
     */
    public Object queryByKey(String tableName, String key) {
        setTableName(tableName);
        return get(key);
    }

    /**
     * query by prefix
     *
     * @param tableName
     * @param prefix
     * @param limit
     * @return
     */
    public List<Object> queryByPrefix(String tableName, String prefix, int limit) {
        setTableName(tableName);
        return queryByPrefix(prefix, limit == 0 ? -1 : limit);
    }

    /**
     * query by limit
     *
     * @param tableName
     * @param count
     * @param order
     * @return
     */
    public List<Object> queryByLimit(String tableName, int count, int order){
        setTableName(tableName);
        if(count > 1000){
            count = 1000;
        }
        return queryByLimit(count,order);
    }
    /**
     * set table name
     *
     * @param tableName
     */
    private void setTableName(String tableName) {
        if (StringUtils.isEmpty(tableName)) {
            throw new RuntimeException("table name is null");
        }
        this.tableName = tableName.trim();
    }
}

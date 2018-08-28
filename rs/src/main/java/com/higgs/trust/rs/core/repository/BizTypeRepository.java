package com.higgs.trust.rs.core.repository;

import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.core.dao.BizTypeDao;
import com.higgs.trust.rs.core.dao.po.BizTypePO;
import com.higgs.trust.rs.core.dao.rocks.BizTypeRocksDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuyu
 * @description
 * @date 2018-06-13
 */
@Slf4j @Repository public class BizTypeRepository implements InitializingBean {
    @Autowired private RsConfig rsConfig;
    @Autowired BizTypeDao bizTypeDao;
    @Autowired BizTypeRocksDao bizTypeRocksDao;

    private Map<String, String> bizTypeMap = new HashMap<>();

    @Override public void afterPropertiesSet() throws Exception {
        List<BizTypePO> bizTypePOList;
        if (rsConfig.isUseMySQL()) {
            bizTypePOList = bizTypeDao.queryAll();
        } else {
            bizTypePOList = bizTypeRocksDao.queryAll();
        }

        if (!CollectionUtils.isEmpty(bizTypePOList)) {
            for (BizTypePO bizTypePO : bizTypePOList) {
                bizTypeMap.put(bizTypePO.getPolicyId(), bizTypePO.getBizType());
            }
        }
    }

    /***
     * get bizType by policyId
     *
     * @param policyId
     * @return
     */
    public String getByPolicyId(String policyId) {
        if (bizTypeMap.containsKey(policyId)) {
            return bizTypeMap.get(policyId);
        }
        BizTypePO bizTypePO;
        if (!rsConfig.isUseMySQL()) {
            bizTypePO = bizTypeRocksDao.get(policyId);
        } else {
            bizTypePO = bizTypeDao.queryByPolicyId(policyId);
        }
        if (bizTypePO == null) {
            log.info("[getByPolicyId]bizTypePO is empty policyId:{}", policyId);
            return null;
        }
        bizTypeMap.put(policyId, bizTypePO.getBizType());
        return bizTypePO.getBizType();
    }

}

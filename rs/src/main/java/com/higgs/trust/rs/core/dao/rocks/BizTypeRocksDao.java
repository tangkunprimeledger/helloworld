package com.higgs.trust.rs.core.dao.rocks;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.dao.po.BizTypePO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author tangfashuang
 * @desc key: policyId, value: bizType
 */
@Slf4j
@Service
public class BizTypeRocksDao extends RocksBaseDao<BizTypePO>{
    @Override protected String getColumnFamilyName() {
        return "bizType";
    }

    public void add(BizTypePO bizTypePO) {
        String key = bizTypePO.getPolicyId();
        if (keyMayExist(key) && null != get(key)) {
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_ALREADY_EXIST);
        }

        bizTypePO.setCreateTime(new Date());
        put(key, bizTypePO);
    }

    public void update(String policyId, String bizType) {
        BizTypePO bizTypePO = get(policyId);
        if (null == bizTypePO) {
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_IS_NOT_EXIST);
        }

        bizTypePO.setBizType(bizType);
        put(policyId, bizTypePO);
    }
}

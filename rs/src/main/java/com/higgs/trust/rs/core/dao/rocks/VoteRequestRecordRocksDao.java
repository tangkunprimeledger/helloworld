package com.higgs.trust.rs.core.dao.rocks;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.dao.po.VoteRequestRecordPO;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author tangfashuang
 */
@Service
@Slf4j
public class VoteRequestRecordRocksDao extends RocksBaseDao<String, VoteRequestRecordPO> {
    @Override protected String getColumnFamilyName() {
        return "voteRequestRecord";
    }

    public void save(VoteRequestRecordPO voteRequestRecordPO) {
        String key = voteRequestRecordPO.getTxId();
        if (null != get(key)) {
            log.error("[VoteRequestRecordRocksDao.save] vote request record is exist, txId={}", key);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_ALREADY_EXIST);
        }

        voteRequestRecordPO.setCreateTime(new Date());
        put(key, voteRequestRecordPO);
    }

    public void setVoteResult(String txId, String sign, String code) {
        VoteRequestRecordPO po = get(txId);
        if (null == po) {
            log.error("[VoteRequestRecordRocksDao.setVoteResult] vote request record is null, txId={}", txId);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_IS_NOT_EXIST);
        }

        po.setUpdateTime(new Date());
        po.setVoteResult(code);
        po.setSign(sign);

        put(txId, po);
    }
}

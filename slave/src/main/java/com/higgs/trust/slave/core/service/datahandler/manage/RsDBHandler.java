package com.higgs.trust.slave.core.service.datahandler.manage;

import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.core.repository.RsPubKeyRepository;
import com.higgs.trust.slave.core.service.merkle.MerkleService;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import com.higgs.trust.slave.model.bo.manage.RsPubKey;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * @author tangfashuang
 * @date 2018/04/17 19:26
 * @desc rs db handler
 */
@Service
public class RsDBHandler implements RsHandler {
    @Autowired
    private RsPubKeyRepository rsPubKeyRepository;

    @Autowired
    private MerkleService merkleService;

    @Override public RsPubKey getRsPubKey(String rsId) {
        return rsPubKeyRepository.queryByRsId(rsId);
    }

    @Override public void registerRsPubKey(RegisterRS registerRS) {
        RsPubKey rsPubKey = rsPubKeyRepository.convertActionToRsPubKey(registerRS);

        rsPubKeyRepository.save(rsPubKey);

        MerkleTree merkleTree = merkleService.queryMerkleTree(MerkleTypeEnum.RS);
        if (null == merkleTree) {
            merkleTree = merkleService.build(MerkleTypeEnum.RS, Arrays.asList(new Object[] {rsPubKey}));
        } else {
            merkleService.add(merkleTree, rsPubKey);
        }
        merkleService.flush(merkleTree);
    }
}

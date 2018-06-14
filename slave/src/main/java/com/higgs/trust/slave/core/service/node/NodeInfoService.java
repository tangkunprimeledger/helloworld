/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.service.node;

import com.higgs.trust.config.master.INodeInfoService;
import com.higgs.trust.config.node.NodeState;
import com.higgs.trust.config.node.NodeStateEnum;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.PackageRepository;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @author suimi
 * @date 2018/6/12
 */
@Service public class NodeInfoService implements INodeInfoService {
    @Autowired private NodeState nodeState;

    @Autowired private BlockRepository blockRepository;

    @Autowired private PackageRepository packageRepository;

    @Override public Long packageHeight() {
        return packageRepository.getMaxHeight();
    }

    @Override public Long blockHeight() {
        return blockRepository.getMaxHeight();
    }

    @Override public boolean hasMasterQualify() {
        if (!nodeState.isState(NodeStateEnum.Running)) {
            return false;
        }
        Long blockHeight = blockRepository.getMaxHeight();
        Long packageHeight = packageRepository.getMaxHeight();
        packageHeight = packageHeight == null ? 0 : packageHeight;
        if (blockHeight >= packageHeight) {
            return true;
        }
        List<PackageStatusEnum> packageStatusEnums = Arrays.asList(PackageStatusEnum.values());
        HashSet statusSet = new HashSet<>(packageStatusEnums);
        long count = packageRepository.count(statusSet, blockHeight);
        if (count >= packageHeight - blockHeight) {
            return true;
        }
        return false;
    }
}

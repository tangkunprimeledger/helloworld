/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.management.failover.filter;

import com.higgs.trust.consensus.core.ConsensusCommit;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import com.higgs.trust.consensus.core.filter.CommandFilter;
import com.higgs.trust.consensus.core.filter.CommandFilterChain;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.model.bo.consensus.PackageCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author suimi
 * @date 2018/6/21
 */
@Order(3) @Slf4j @Component public class FailoverPackageFilter implements CommandFilter {

    @Autowired private BlockRepository blockRepository;

    private AtomicLong blockHeight = new AtomicLong(0);

    @Override
    public void doFilter(ConsensusCommit<? extends AbstractConsensusCommand> commit, CommandFilterChain chain) {
        if (commit.operation() instanceof PackageCommand) {
            PackageCommand command = (PackageCommand)commit.operation();
            Long packageHeight = command.getPackageHeight();
            if (packageHeight <= blockHeight.get()) {
                log.warn("package command rejected,current block height:{}", blockHeight.get());
                commit.close();
                return;
            } else {
                blockHeight.set(blockRepository.getMaxHeight());
                if (packageHeight <= blockHeight.get()) {
                    log.warn("package command rejected,current block height:{}", blockHeight.get());
                    commit.close();
                    return;
                }
            }
        }
        chain.doFilter(commit);
    }
}

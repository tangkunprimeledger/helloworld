/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core.primitive;

import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import io.atomix.primitive.Synchronous;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author suimi
 * @date 2018/7/6
 */
@Slf4j
public class CommandPrimitive extends Synchronous<IAsyncCommandPrimitive> implements ICommandPrimitive {

    private IAsyncCommandPrimitive asyncCommandPrimitive;

    private final long operationTimeoutMillis;

    public CommandPrimitive(IAsyncCommandPrimitive primitive, long operationTimeoutMillis) {
        super(primitive);
        this.asyncCommandPrimitive = primitive;
        this.operationTimeoutMillis = operationTimeoutMillis;
    }

    @Override public void submit(AbstractConsensusCommand command) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("sync submit");
            }
            asyncCommandPrimitive.submit(command).get(operationTimeoutMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException("submit command failed", e);
        }
    }

    @Override public IAsyncCommandPrimitive async() {
        if (log.isDebugEnabled()) {
            log.debug("sync async");
        }
        return asyncCommandPrimitive;
    }
}

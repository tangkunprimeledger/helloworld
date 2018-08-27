/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.term;

import com.higgs.trust.consensus.core.IConsensusSnapshot;
import io.atomix.utils.serializer.Namespace;
import io.atomix.utils.serializer.Namespaces;
import io.atomix.utils.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author suimi
 * @date 2018/6/4
 */
@Slf4j @Component public class ConsensusSnapshot implements IConsensusSnapshot {

    @Autowired private TermManager termManager;

    private Serializer serializer;

    public ConsensusSnapshot() {
        //@formatter:off
        Namespace namespace = Namespace.builder()
            .setRegistrationRequired(false)
            .setCompatible(true)
            .register(Namespaces.BASIC)
            .register(SnapshotInfo.class)
            .register(TermInfo.class)
            .build();
        //@formatter:on
        serializer = Serializer.using(namespace);
    }

    @Override public byte[] getSnapshot() {
        SnapshotInfo snapshotInfo = new SnapshotInfo();
        snapshotInfo.setTerms(termManager.getTerms());
        log.info("get snapshot:{}", snapshotInfo);
        return serializer.encode(snapshotInfo);
    }

    @Override public void installSnapshot(byte[] snapshot) {
        log.debug("install snapshot:{}", snapshot);
        SnapshotInfo snapshotInfo = serializer.decode(snapshot);
        log.info("install snapshot:{}", snapshot);
        termManager.resetTerms(snapshotInfo.getTerms());
    }

}

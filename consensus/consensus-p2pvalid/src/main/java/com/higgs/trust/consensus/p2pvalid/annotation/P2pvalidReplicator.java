package com.higgs.trust.consensus.p2pvalid.annotation;

import java.lang.annotation.*;

@Documented @Target({ElementType.TYPE, ElementType.METHOD}) @Retention(RetentionPolicy.RUNTIME)
public @interface P2pvalidReplicator {
}

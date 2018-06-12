package com.higgs.trust.consensus.annotation;

import java.lang.annotation.*;

@Documented @Target({ElementType.TYPE, ElementType.METHOD}) @Retention(RetentionPolicy.RUNTIME)
public @interface Replicator {
}

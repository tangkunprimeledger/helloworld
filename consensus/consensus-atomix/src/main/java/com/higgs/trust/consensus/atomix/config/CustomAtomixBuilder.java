/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.config;

import io.atomix.core.AtomixBuilder;
import io.atomix.core.AtomixConfig;
import io.atomix.core.AtomixRegistry;

/**
 * @author suimi
 * @date 2018/8/8
 */
public class CustomAtomixBuilder extends AtomixBuilder {
    protected CustomAtomixBuilder(AtomixConfig config, AtomixRegistry registry) {
        super(config, registry);
    }
}

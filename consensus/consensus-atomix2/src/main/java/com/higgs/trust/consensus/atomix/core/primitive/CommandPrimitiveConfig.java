/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core.primitive;

import io.atomix.primitive.PrimitiveConfig;

/**
 * @author suimi
 * @date 2018/8/6
 */
public class CommandPrimitiveConfig extends PrimitiveConfig<CommandPrimitiveConfig> {
    public CommandPrimitiveConfig(CommandPrimitiveType primitiveType) {
        super(primitiveType);
    }
}

/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core.primitive;

import io.atomix.primitive.config.PrimitiveConfig;

/**
 * @author suimi
 * @date 2018/8/6
 */
public class CommandPrimitiveConfig extends PrimitiveConfig<CommandPrimitiveConfig> {

    private CommandPrimitiveType primitiveType;

    public CommandPrimitiveConfig(CommandPrimitiveType primitiveType) {
        this.primitiveType = primitiveType;
    }

    @Override public CommandPrimitiveType getType() {
        return primitiveType;
    }
}

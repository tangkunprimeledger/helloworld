/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.service.consensus.log;

import com.higgs.trust.slave.model.bo.Package;

/**
 * @author suimi
 * @date 2018/6/13
 */
public interface PackageListener {

    void received(Package pack);
}

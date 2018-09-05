/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.term;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author suimi
 * @date 2018/8/27
 */
@ToString @Getter @Setter public class SnapshotInfo implements Serializable {

    private List<TermInfo> terms = new ArrayList<>();

}

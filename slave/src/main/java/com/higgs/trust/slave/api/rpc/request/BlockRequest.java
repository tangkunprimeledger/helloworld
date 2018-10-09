package com.higgs.trust.slave.api.rpc.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author duhongming
 * @date 2018/9/18
 */
@Getter
@Setter
@AllArgsConstructor
public class BlockRequest implements Serializable {
    private long startHeight;
    private int size;
}

package com.higgs.trust.rs.core.bo;

import com.higgs.trust.rs.common.BaseBO;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * lock object
 *
 * @author lingchao
 * @create 2018年08月23日14:27
 */
@Getter
@Setter
public class WaitAsyncFinishLockObject extends BaseBO {
    /**
     * lock
     */
    private Lock lock;
    /**
     * condition
     */
    private Condition condition;
    /**
     * finish
     */
    private boolean finish;
    /**
     * result
     */
    private Object result;
}

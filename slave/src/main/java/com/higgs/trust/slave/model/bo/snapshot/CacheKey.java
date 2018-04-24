package com.higgs.trust.slave.model.bo.snapshot;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

/**
 * CacheKey  the father of all sons
 *
 * @author lingchao
 * @create 2018年04月17日11:48
 */
@Getter
@Setter
public class CacheKey extends BaseBO{
    /**
     * the cache class
     */
    private Class clazz = this.getClass();
}

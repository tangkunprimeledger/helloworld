package com.higgs.trust.common.lambda;

/**
 * @author duhongming
 * @date 2018/8/2
 */
public interface Mapper<FROM, TO> {

    /**
     * map
     * @param src
     * @return
     */
    TO mapping(FROM src);
}

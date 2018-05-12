package com.higgs.trust.common.mybatis;

import java.util.List;

/**
 * @Description:
 * @author: pengdi
 **/
public interface BaseDao<T> {

    public int add(T t);

    public int delete(Object id);

    public int queryByCount(T t);

    public List<T> queryByList(T t);

    public T queryById(Object id);
}

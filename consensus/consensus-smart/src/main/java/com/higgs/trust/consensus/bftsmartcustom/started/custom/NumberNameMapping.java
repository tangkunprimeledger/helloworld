package com.higgs.trust.consensus.bftsmartcustom.started.custom;

import java.util.Map;

/**
 * @author: zhouyafeng
 * @create: 2018/07/15 09:58
 * @description:
 */
public interface NumberNameMapping {
    //获取映射对象
    Map<String, String> getMapping();
    //添加映射对
    boolean addMapping(Map<String, String> map);
}
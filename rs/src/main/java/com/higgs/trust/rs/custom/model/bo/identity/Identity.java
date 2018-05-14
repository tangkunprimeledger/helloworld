package com.higgs.trust.rs.custom.model.bo.identity;

import com.higgs.trust.rs.common.BaseBO;
import lombok.Getter;
import lombok.Setter;

/*
 * @desc 存证实体类
 * @author WangQuanzhou
 * @date 2018/3/2 18:03
 */
@Getter @Setter public class Identity extends BaseBO {

    private static final long serialVersionUID = 1436723757568942247L;
    /**
     * 存证的key
     */
    private String key;

    /**
     * 存证value
     */
    private String value;

    /**
     * 请求唯一id，幂等判断的依据
     */
    private String reqNo;

}

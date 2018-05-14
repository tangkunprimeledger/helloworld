package com.higgs.trust.rs.custom.model.bo.identity;

import com.higgs.trust.rs.common.BaseBO;
import lombok.Getter;
import lombok.Setter;

/*
 * @desc 暂存 存证请求数据的相关数据对象
 * @author WangQuanzhou
 * @date 2018/3/5 12:02
 */
@Getter @Setter public class IdentityRequest extends BaseBO {

    private static final long serialVersionUID = -6405043635467806518L;

    /**
     * 请求唯一标识
     */
    private String reqNo;

    /**
     * 存证的key
     */
    private String key;

    /**
     * 存证value
     */
    private String value;

    /**
     * 存证更新的标志
     * 000表示覆盖更新
     * 999表示不做更新
     */
    private String flag;

}

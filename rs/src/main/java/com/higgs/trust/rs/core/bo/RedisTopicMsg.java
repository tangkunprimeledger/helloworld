package com.higgs.trust.rs.core.bo;

import com.higgs.trust.rs.common.BaseBO;
import com.higgs.trust.rs.core.api.enums.RedisMegGroupEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * redis topic message
 *
 * @author lingchao
 * @create 2018年08月23日14:27
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RedisTopicMsg extends BaseBO{
    /**
     * msg key
     */
    private String key;
    /**
     * meg
     */
    private Object result;
    /**
     * topic
     */
    private RedisMegGroupEnum redisMegGroupEnum;
}

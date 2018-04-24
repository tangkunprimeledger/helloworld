package com.higgs.trust.slave.model.bo.action;

import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @Description: 操作类型 抽象类对象
 * @author: pengdi
 **/
@Getter @Setter public abstract class Action extends BaseBO {
    private static final long serialVersionUID = -9206591383343379207L;
    /**
     * 操作类型
     */
    @NotNull private ActionTypeEnum type;

    /**
     * 操作索引
     */
    @NotNull private Integer index;

    /**
     * data identity action
     *
     * @author lingchao
     * @create 2018年03月30日18:12
     */
}

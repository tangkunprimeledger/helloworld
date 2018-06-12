package com.higgs.trust.slave.model.bo.snapshot;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * object with status
 *
 * @author lingchao
 * @create 2018年06月07日10:38
 */
@Getter
@Setter
@AllArgsConstructor
public class Value {
    /**
     * value data
     */
    private Object object;
    /**
     * value status as 1.INSERT for insert  2.UPDATE for update
     */
    private String status;
}

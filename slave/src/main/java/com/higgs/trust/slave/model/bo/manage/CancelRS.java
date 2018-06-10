package com.higgs.trust.slave.model.bo.manage;

import com.higgs.trust.slave.model.bo.action.Action;
import lombok.Getter;
import lombok.Setter;

/**
 * cancel rs bean
 * @author tangfashuang
 */
@Getter
@Setter
public class CancelRS extends Action{
    private String rsId;
}

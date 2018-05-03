package com.higgs.trust.slave.model.bo.manage;

import com.higgs.trust.slave.model.bo.BaseBO;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * @author tangfashuang
 * @desc policy bo
 * @date 2018-04-02
 */
@Setter @Getter public class Policy extends BaseBO {

    /**
     * policy id
     */
    private String policyId;

    /**
     * policy name
     */
    private String policyName;

    /**
     * rs ids of related to policy
     */
    private List<String> rsIds;
}

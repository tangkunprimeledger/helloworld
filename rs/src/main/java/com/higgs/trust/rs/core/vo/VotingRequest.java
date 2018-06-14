package com.higgs.trust.rs.core.vo;

import com.higgs.trust.rs.common.BaseBO;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author liuyu
 * @description
 * @date 2018-06-07
 */
@Setter @Getter @AllArgsConstructor public class VotingRequest extends BaseBO {
    /**
     * voting sender
     */
    @NotNull private String sender;
    /**
     * transaction info
     */
    @NotNull private CoreTransaction coreTransaction;
    /**
     * voting pattern SYNC/ASYNC
     */
    @NotNull private String votePattern;
}

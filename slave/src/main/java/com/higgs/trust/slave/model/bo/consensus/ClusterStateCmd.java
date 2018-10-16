package com.higgs.trust.slave.model.bo.consensus;

import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author liuyu
 * @date 2018/10/12
 */
@NoArgsConstructor @Getter @Setter public class ClusterStateCmd extends ValidCommand<Integer> {

    private static final long serialVersionUID = -2067709119627092336L;

    private String requestId;

    public ClusterStateCmd(String requestId, Integer value, long view) {
        super(value, view);
        this.requestId = requestId;
    }

    @Override public String messageDigest() {
        return requestId;
    }
}

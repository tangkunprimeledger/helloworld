package com.higgs.trust.slave.model.bo.consensus;

import com.alibaba.fastjson.annotation.JSONField;
import com.higgs.trust.consensus.bft.core.template.AbstractConsensusCommand;
import com.higgs.trust.slave.api.vo.PackageVO;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @Description:
 * @author: pengdi
 **/
@Getter
@Setter
public class PackageCommand extends AbstractConsensusCommand<PackageVO> {

    private long term;

    private String masterName;

    /**
     * signature
     */
    @NotEmpty
    @JSONField(label = "sign")
    private String sign;

    public PackageCommand(PackageVO value) {
        super(value);
    }
}

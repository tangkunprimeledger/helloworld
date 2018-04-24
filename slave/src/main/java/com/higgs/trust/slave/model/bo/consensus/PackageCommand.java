package com.higgs.trust.slave.model.bo.consensus;

import com.higgs.trust.consensus.bft.core.template.AbstractConsensusCommand;
import com.higgs.trust.slave.api.vo.PackageVO;

/**
 * @Description:
 * @author: pengdi
 **/
public class PackageCommand extends AbstractConsensusCommand<PackageVO> {
    public PackageCommand(PackageVO value) {
        super(value);
    }
}

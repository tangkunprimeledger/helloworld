package com.higgs.trust.slave.model.bo.consensus;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.Labels;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import com.higgs.trust.consensus.core.command.SignatureCommand;
import com.higgs.trust.slave.api.vo.PackageVO;

/**
 * @Description:
 * @author: pengdi
 **/
public class PackageCommand extends SignatureCommand<PackageVO> {
    public PackageCommand(PackageVO value) {
        super(value);
    }

    @Override public String getNodeName() {
        return get().getMasterName();
    }

    @Override public String getSignValue() {
        return JSON.toJSONString(get(), Labels.excludes("sign"));
    }

    @Override public String getSignature() {
        return get().getSign();
    }
}

package com.higgs.trust.slave.model.bo.consensus;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.higgs.trust.consensus.core.command.SignatureCommand;
import com.higgs.trust.slave.api.vo.PackageVO;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @Description:
 * @author: pengdi
 **/
@Getter @Setter public class PackageCommand extends SignatureCommand<PackageVO> {

    /**
     * term
     */
    private Long term;

    /**
     * master name
     */
    private String masterName;

    /**
     * signature
     */
    @NotEmpty @JSONField(label = "sign") private String sign;

    public PackageCommand(Long term, String masterName, PackageVO value) {
        super(value);
    }

    @Override public String getNodeName() {
        return masterName;
    }

    @Override public String getSignValue() {
        return JSON.toJSONString(get());
    }

    @Override public String getSignature() {
        return sign;
    }
}

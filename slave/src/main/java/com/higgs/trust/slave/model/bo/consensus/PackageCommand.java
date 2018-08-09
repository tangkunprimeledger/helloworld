package com.higgs.trust.slave.model.bo.consensus;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.higgs.trust.config.node.command.TermCommand;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import com.higgs.trust.consensus.core.command.SignatureCommand;
import com.higgs.trust.slave.api.vo.PackageVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @Description:
 * @author: pengdi
 **/
@ToString(callSuper = true, exclude = {"sign"}) @Getter @Setter public class PackageCommand
    extends AbstractConsensusCommand<PackageVO> implements SignatureCommand, TermCommand {

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
        this.term = term;
        this.masterName = masterName;
    }

    @Override public Long[] getPackageHeight() {
        return new Long[]{ get().getHeight() };
    }

    @Override public String getNodeName() {
        return masterName;
    }

    @Override public String getSignValue() {
        String join = String.join(",", JSON.toJSONString(get()), "" + term, masterName);
        return Hashing.sha256().hashString(join, Charsets.UTF_8).toString();
    }

    @Override public String getSignature() {
        return sign;
    }
}

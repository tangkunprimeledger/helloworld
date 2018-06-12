package com.higgs.trust.slave.model.bo.consensus;

import com.higgs.trust.consensus.bft.core.template.AbstractConsensusCommand;
import com.higgs.trust.slave.api.vo.PackageVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.StringJoiner;

/**
 * @Description:
 * @author: pengdi
 **/
@ToString(callSuper = true, exclude = {"sign"}) @Getter @Setter public class PackageCommand
    extends AbstractConsensusCommand<PackageVO> implements SignatureCommand {

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

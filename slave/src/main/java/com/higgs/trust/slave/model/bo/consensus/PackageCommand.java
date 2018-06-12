package com.higgs.trust.slave.model.bo.consensus;

<<<<<<< HEAD
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import com.higgs.trust.consensus.core.command.SignatureCommand;
import com.higgs.trust.slave.api.vo.PackageVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.StringJoiner;
=======
import com.alibaba.fastjson.annotation.JSONField;
import com.higgs.trust.consensus.bft.core.template.AbstractConsensusCommand;
import com.higgs.trust.slave.api.vo.PackageVO;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;
>>>>>>> dev_0610_ca

/**
 * @Description:
 * @author: pengdi
 **/
<<<<<<< HEAD
@ToString(callSuper = true, exclude = {"sign"}) @Getter @Setter public class PackageCommand
    extends AbstractConsensusCommand<PackageVO> implements SignatureCommand {

    /**
     * term
     */
    private Long term;

    /**
     * master name
     */
=======
@Getter
@Setter
public class PackageCommand extends AbstractConsensusCommand<PackageVO> {

    private long term;

>>>>>>> dev_0610_ca
    private String masterName;

    /**
     * signature
     */
<<<<<<< HEAD
    @NotEmpty @JSONField(label = "sign") private String sign;

    public PackageCommand(Long term, String masterName, PackageVO value) {
=======
    @NotEmpty
    @JSONField(label = "sign")
    private String sign;

    public PackageCommand(PackageVO value) {
>>>>>>> dev_0610_ca
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

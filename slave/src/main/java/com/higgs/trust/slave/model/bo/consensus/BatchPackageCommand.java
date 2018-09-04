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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author tangfashuang
 * @date 2018/8/3 10:44
 * @desc batch package command
 */
@ToString(callSuper = true, exclude = {"sign"}) @Getter @Setter public class BatchPackageCommand
    extends AbstractConsensusCommand<List<PackageVO>> implements SignatureCommand, TermCommand {

    /**
     * term
     */
    private Long term;

    /**
     * view
     */
    private long view;

    /**
     * master name
     */
    private String masterName;

    /**
     * signature
     */
    @NotEmpty @JSONField(label = "sign") private String sign;

    public BatchPackageCommand(Long term, long view,String masterName, List<PackageVO> value) {
        super(value);
        this.term = term;
        this.view = view;
        this.masterName = masterName;
    }

    @Override public Long[] getPackageHeight() {
        List<PackageVO> voList = get();
        int size = voList.size();

        // sort by height asc
        Collections.sort(voList, new Comparator<PackageVO>() {
            @Override public int compare(PackageVO vo1, PackageVO vo2) {
                return vo1.getHeight().compareTo(vo2.getHeight());
            }
        });

        Long[] heightArr = new Long[size];
        for (int i = 0; i < size; i ++) {
            heightArr[i] = voList.get(i).getHeight();
        }
        return heightArr;
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

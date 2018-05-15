package com.higgs.trust.rs.custom.api.vo.blockchain;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class UtxoVO extends BaseBO{

    private String txId;

    private Integer index;

    private Integer actionIndex;

    private String identity;

    private Map<String, String> state;

    private String contractAddress;

    private String status;

    private Date createTime;

    private List<UtxoVO> preUTXOVO;
}

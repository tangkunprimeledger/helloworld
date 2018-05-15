package com.higgs.trust.slave.api.vo;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class UTXOVO extends BaseBO{

    private String txId;

    private Integer index;

    private Integer actionIndex;

    private String identity;

    private Map<String, String> state;

    private String contractAddress;

    private String status;

    private Date createTime;

    private List<UTXOVO> preUTXOVO;
}

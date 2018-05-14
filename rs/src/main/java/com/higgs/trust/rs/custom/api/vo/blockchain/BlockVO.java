package com.higgs.trust.rs.custom.api.vo.blockchain;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class BlockVO extends BaseBO{
   private Long height;

   private String blockHash;

   private Integer txNum;

   private Date BlockTime;

   private String version;
}

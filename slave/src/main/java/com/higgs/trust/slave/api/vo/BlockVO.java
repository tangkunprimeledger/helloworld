package com.higgs.trust.slave.api.vo;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Setter
@Getter
public class BlockVO extends BaseBO{
   private Long height;

   private String blockHash;
   /**
    * previous block hash
    */
   private String previousHash;

   private Integer txNum;
   /**
    * total block size,unit:kb
    */
   private BigDecimal totalBlockSize;
   /**
    * the number of transactions recorded by the current block
    */
   private Long totalTxNum;

   private Date BlockTime;

   private String version;
}

package com.higgs.trust.slave.model.bo.ca;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @desc TODO  
 * @author WangQuanzhou
 * @date 2018/6/5 16:15    
 */
@Getter @Setter public class Ca extends BaseBO {
    private String version;

    private Date period;

    private boolean valid;

    private String pubKey;

    private String user;

    private String usage;
}

package com.higgs.trust.rs.tx;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tangkun
 * @date 2018-12-20
 */
@Setter
@Getter
public class AutoTestContext {

    //部署合约个数
    public int deployNum = 1000;

    public Froze froze;
}

@Setter
@Getter
class Froze {

    private String from;
    private String to;
    private List<Currency> currencyList = new ArrayList<>(10000);

    public Froze(String from, String to) {
        this.from = from;
        this.to = to;
    }
}

@Setter
@Getter
class Currency {
    private String from;
    private String to;
    private STO sto;
    private int totalSupply;

    public Currency(String from, String to, int totalSupply) {
        this.from = from;
        this.to = to;
        this.totalSupply = totalSupply;
    }
}

@Setter
@Getter
class STO {
    private String from;
    private String to;
    private int totalSupply;

    public STO(String from, String to, int totalSupply) {
        this.from = from;
        this.to = to;
        this.totalSupply = totalSupply;
    }
}

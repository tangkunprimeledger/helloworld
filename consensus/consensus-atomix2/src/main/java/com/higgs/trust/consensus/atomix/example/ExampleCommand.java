package com.higgs.trust.consensus.atomix.example;

import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import lombok.Getter;

/**
 * @author Zhu_Yuanxiang
 * @create 2018-08-01
 */
@Getter
public class ExampleCommand extends AbstractConsensusCommand<String> {
    private static final long serialVersionUID = 1L;//??

    private String msg;

    public ExampleCommand(String value) {
        super(value);
        msg = value;
    }

    @Override public String toString() {
        return "ExampleCommand{" + "msg='" + msg + '\'' + '}';
    }
}

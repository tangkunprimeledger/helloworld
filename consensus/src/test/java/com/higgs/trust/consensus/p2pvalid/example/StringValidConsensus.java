package com.higgs.trust.consensus.p2pvalid.example;

import com.higgs.trust.consensus.p2pvalid.core.ValidCommit;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import com.higgs.trust.consensus.p2pvalid.example.slave.ValidateCommand;
import org.springframework.stereotype.Component;

/**
 * @author cwy
 */
@Component
public class StringValidConsensus extends ValidConsensus {

    public void testStringValid(ValidCommit<StringValidCommand> commit) {
        System.out.println("command2 is " + commit.operation().get());
        commit.close();
    }
    public void testValidateCommand(ValidCommit<ValidateCommand> commit) {
        System.out.println("ValidateCommand is " + commit.operation().get());
        commit.close();
    }
}

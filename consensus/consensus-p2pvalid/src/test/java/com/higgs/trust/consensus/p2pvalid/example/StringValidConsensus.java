package com.higgs.trust.consensus.p2pvalid.example;

import com.higgs.trust.consensus.p2pvalid.core.P2PValidCommit;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import com.higgs.trust.consensus.p2pvalid.example.slave.ValidateCommand;
import org.springframework.stereotype.Component;

/**
 * @author cwy
 */
@Component
public class StringValidConsensus extends ValidConsensus {

    public void testStringValid(P2PValidCommit<StringValidCommand> commit) {
        System.out.println("command2 is " + commit.operation().get());
        commit.close();
    }
    public void testValidateCommand(P2PValidCommit<ValidateCommand> commit) {
        System.out.println("ValidateCommand is " + commit.operation().get());
        commit.close();
    }
}

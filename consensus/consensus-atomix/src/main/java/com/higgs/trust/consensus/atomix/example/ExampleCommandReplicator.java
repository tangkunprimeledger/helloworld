package com.higgs.trust.consensus.atomix.example;

import com.higgs.trust.consensus.annotation.Replicator;
import com.higgs.trust.consensus.core.ConsensusCommit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Zhu_Yuanxiang
 * @create 2018-08-01
 */

@Slf4j
@Component
@Replicator
public class ExampleCommandReplicator {

    public void artificialChangeMaster(ConsensusCommit<ExampleCommand> commit) {
        ExampleCommand operation = commit.operation();
        log.debug("ExampleCommandReplicator received ExampleCommand:{}",operation.getMsg());
        commit.close();
    }
}

//package com.higgs.trust.consensus.bftsmart.started.example;
//
//import com.higgs.trust.consensus.bft.core.ConsensusCommit;
//import com.higgs.trust.consensus.bftsmartcustom.started.SmartAbstractConsensusStateMachine;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
///**
// *
// * @author: Zhouyafeng
// * @create: 2018/4/25 16:04
// * @description:
// *
// */
//@Component
//public class StringStateMachine extends SmartAbstractConsensusStateMachine {
//    private static final Logger log = LoggerFactory.getLogger(StringStateMachine.class);
//
//    /**
//     * apply method for your command
//     *
//     * @param commit
//     * @return
//     */
//    public void stringApply(ConsensusCommit<StringCommand> commit) {
//        try {
//            log.warn("command value is {}", commit.operation().get());
//            System.out.println("value: " + commit.operation().get());
//        } finally {
//            System.out.println("close the commit");
//            commit.close();
//        }
//    }
//}

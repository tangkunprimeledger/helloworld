/*
 * Copyright (c) 2013-2017, suimi
 */

import bftsmart.tom.server.defaultservices.CommandsInfo;
import bftsmart.tom.server.defaultservices.FileRecoverer;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * @author suimi
 * @date 2018/7/19
 */
public class FileRecovererTest {

    @Test
    public void test() {
        FileRecoverer recoverer = new FileRecoverer(3, "e:/tmp");
//        CommandsInfo[] logState = recoverer.getLogState(3, "e:/tmp/0.1531970509932.log");
        CommandsInfo[] logState = recoverer.getLogState(3, "e:/tmp/3.1531988584372.log");
        System.out.print(Arrays.toString(logState));
    }

    @Test
    public void test2() {
        FileRecoverer recoverer = new FileRecoverer(0, "e:/tmp");
        //        CommandsInfo[] logState = recoverer.getLogState(3, "e:/tmp/0.1531970509932.log");
        byte[] ckpState = recoverer.getCkpState("e:/tmp/0.1532062738584.ckp");
        System.out.println(new String(ckpState));
        int ckpLastConsensusId = recoverer.getCkpLastConsensusId();
        System.out.println(ckpLastConsensusId);

        CommandsInfo[] logState = recoverer.getLogState(3, "e:/tmp/0.1532062738598.log");
        System.out.println(Arrays.toString(logState));
        System.out.println(recoverer.getLogLastConsensusId());
        System.out.println(logState[0].msgCtx[0].getConsensusId());
        System.out.println(logState[logState.length-1].msgCtx[0].getConsensusId());
    }
}

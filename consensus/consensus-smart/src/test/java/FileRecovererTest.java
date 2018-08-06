/*
 * Copyright (c) 2013-2017, suimi
 */

import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.server.defaultservices.CommandsInfo;
import bftsmart.tom.server.defaultservices.FileRecoverer;
import bftsmart.tom.util.BatchReader;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * @author suimi
 * @date 2018/7/19
 */
public class FileRecovererTest {

    @Test public void test() {
        FileRecoverer recoverer = new FileRecoverer(2, "e:/tmp");
        //        CommandsInfo[] logState = recoverer.getLogState(3, "e:/tmp/0.1531970509932.log");
        CommandsInfo[] logState = recoverer.getLogState(4342, "e:/tmp/2.1532956240676.log");

        int startId = logState[0].msgCtx[0].getConsensusId();
        for (int i = 0; i < logState.length; i++) {
            int consensusId = logState[i].msgCtx[0].getConsensusId();
            if (consensusId != startId) {
                System.out.println(startId);
                startId = consensusId;
            }
            startId++;
        }
        System.out.print(Arrays.toString(logState));
    }

    @Test public void test2() {
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
        System.out.println(logState[logState.length - 1].msgCtx[0].getConsensusId());
    }

    @Test
    public void test3() {
        byte[] bytes = {9, 106, -48, 32, -15, 1, 82, 28, 56, 74, -47, -81, -48, 6, -28, 107};
        BatchReader batchReader = new BatchReader(bytes, false);
        TOMMessage[] tomMessages = batchReader.deserialiseRequests(null);
        System.out.println(tomMessages);
    }
}

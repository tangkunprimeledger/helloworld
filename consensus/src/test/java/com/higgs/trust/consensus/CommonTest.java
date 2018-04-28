package com.higgs.trust.consensus;

import com.higgs.trust.consensus.p2pvalid.core.storage.entry.impl.ReceiveCommandStatistics;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@Slf4j
public class CommonTest {
    private DB db;
    private static final String RECEIVE_STATISTICS_MAP = "receive_statistics_map";

    @BeforeTest
    public void before(){
        String dbFile = "D:/temp/p2p.tar/p2p/receiveDB";
        db = DBMaker
                .fileDB(dbFile)
                .fileMmapEnable()
                .closeOnJvmShutdown()
                .cleanerHackEnable()
                .transactionEnable()
                .make();
    }

    @Test
    public void scanTheReceiveMap(){
        HTreeMap<String, ReceiveCommandStatistics> treeMap = db.hashMap(RECEIVE_STATISTICS_MAP)
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();
        treeMap.forEachValue((receiveCommandStatistics)->{
            if(!receiveCommandStatistics.isApply()){
                log.info("{}", receiveCommandStatistics);
            }
            return null;
        });

    }
}

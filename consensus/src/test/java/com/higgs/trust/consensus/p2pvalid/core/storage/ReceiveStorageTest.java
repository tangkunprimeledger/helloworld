package com.higgs.trust.consensus.p2pvalid.core.storage;
import com.higgs.trust.consensus.p2pvalid.core.exchange.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.storage.entry.impl.ReceiveCommandStatistics;
import com.higgs.trust.consensus.p2pvalid.example.StringValidCommand;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

@Slf4j
public class ReceiveStorageTest {
    private ValidCommandWrap validCommandWrap;
    private ReceiveStorage receiveStorage;
    private Integer applyThreshold;

    @BeforeTest
    public void before(){
        applyThreshold = 1;
        StringValidCommand stringValidCommand = new StringValidCommand("test String command");
        validCommandWrap = ValidCommandWrap.of(stringValidCommand).fromNodeName("self");
        receiveStorage = ReceiveStorage.createMemoryStorage();
    }

    @Test
    public void testAdd(){
        String key = validCommandWrap.getCommandClass().getName().concat("_").concat(validCommandWrap.getMessageDigest());
        ReceiveCommandStatistics receiveCommandStatistics = receiveStorage.add(key, validCommandWrap);
        assertNotNull(receiveCommandStatistics);
    }

    @Test(expectedExceptions={RuntimeException.class})
    public void testAddException(){
        receiveStorage.add(null, null);
    }

    @Test
    public void testGetReceiveCommandStatistics(){
        String key = validCommandWrap.getCommandClass().getName().concat("_").concat(validCommandWrap.getMessageDigest());
        receiveStorage.add(key, validCommandWrap);
        assertNotNull(receiveStorage.getReceiveCommandStatistics(key));
    }

    @Test
    public void testUpdateReceiveCommandStatistics(){
        String key = validCommandWrap.getCommandClass().getName().concat("_").concat(validCommandWrap.getMessageDigest());
        assertNotNull(key);
        receiveStorage.add(key, validCommandWrap);
        ReceiveCommandStatistics receiveCommandStatistics = receiveStorage.getReceiveCommandStatistics(key);
        receiveStorage.updateReceiveCommandStatistics(key, receiveCommandStatistics);
    }

    @Test(expectedExceptions={RuntimeException.class})
    public void testUpdateReceiveCommandStatisticsException(){
        receiveStorage.updateReceiveCommandStatistics(null, null);
    }

    @Test
    public void testFromApplyQueue(){
        String key = validCommandWrap.getCommandClass().getName().concat("_").concat(validCommandWrap.getMessageDigest());
        receiveStorage.add(key, validCommandWrap);
        receiveStorage.addApplyQueue(key);
        key = receiveStorage.takeFromApplyQueue();
        assertNotNull(key);
    }

    @Test
    public void testSign() throws Exception {
        String pri = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIiVJuf1C5CXeDl/g7mcZ4daNsMkdO33OU4LU5CWtXcIMXONRM0/jcHw171TM9PaV+c31TRfZKkhurm5cbzN7+wOT+745AX0BxM2oGXKKNIuyt9gtO91d1O87K39q3Vjf65vzjjIUjT9tLzTDotCGSAs3MHolZqZuyTMNbvRQ2RFAgMBAAECgYBpCMKtuP2WyVU0aoKDV+sp5M+eTEf7xJZ2FTHDTJLqogtdgHM7oDdEwRE30GdX8V6OOpqkNwPzKTQupZT5DCoHaLlKSNRCpQMfANmb84//0B6sLj8nTwcL+Ap51l/XOkhlJ/Pe/uElDaBYP05/SV7yD25xYJTMa7KUxn0JGsW3IQJBAOni5qrZ2/Xfl7VMbq3eGqDYkB+fJPpAXcfMg94+mKYoyZEptr/b78a3yvGXo4Fu8He3KXVmEjPMGL8JTXgURN0CQQCVfxCDPUA6s+D5BcH7x0ivMwaJedk4k0uaGW9Tfxl9SMFxmLQaRRdg/Pc/FJBV7cWDrjLUfK4CVJ0Zr2whLxKJAkAH8MUmDcly+olbBWJIkt7SkSereyq1OFQ2wVHEzNk/4uic/g6PSvdEBt9j/mL1tP+DAPvuWW3KxmYcrkMsDXwdAkADcTKLIwzojR2//h7yUitdRVqALdsPj2ytNqq2jHWkydkSaYa0GCUqqYhz3mBYaiojFw66j8hd617WqZBezzupAkEAjIkafm75nhYx9ObTkZU3WDyeSACr7NH/bkaKtWibDRhfuELgdW9LUrXXnqImLgFu8VvbqF/zBhkaFWAcfy9mSA==";
        String pub = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCIlSbn9QuQl3g5f4O5nGeHWjbDJHTt9zlOC1OQlrV3CDFzjUTNP43B8Ne9UzPT2lfnN9U0X2SpIbq5uXG8ze/sDk/u+OQF9AcTNqBlyijSLsrfYLTvdXdTvOyt/at1Y3+ub844yFI0/bS80w6LQhkgLNzB6JWambskzDW70UNkRQIDAQAB";
    }

    @Test
    public void testAddDelayAndTrans(){
        String key = validCommandWrap.getCommandClass().getName().concat("_").concat(validCommandWrap.getMessageDigest());
        receiveStorage.add(key, validCommandWrap);
        receiveStorage.addDelayQueue(key);
        key = receiveStorage.takeFromApplyQueue();
        assertNotNull(key);
    }

    @Test
    public void testAddApply(){
        for(int i = 0; i < 10; i++){
            receiveStorage.addApplyQueue("test");
        }
    }

    @Test()
    public void testAddApplyException(){
        receiveStorage.addApplyQueue(null);
    }

    @Test()
    public void testAddDelayAndTransException(){
        receiveStorage.addDelayQueue(null);
    }

    @Test
    public void testGC() throws InterruptedException {
        String key = validCommandWrap.getCommandClass().getName().concat("_").concat(validCommandWrap.getMessageDigest());
        receiveStorage.add(key, validCommandWrap);
        receiveStorage.addGCSet(key);
        Thread.sleep(5000);
    }

    @Test(expectedExceptions={RuntimeException.class})
    public void testGCException(){
        receiveStorage.addGCSet(null);
    }

    @AfterTest
    public void after() throws InterruptedException {
        Thread.sleep(200);
    }
}
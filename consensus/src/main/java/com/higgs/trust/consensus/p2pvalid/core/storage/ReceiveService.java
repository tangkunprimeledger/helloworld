package com.higgs.trust.consensus.p2pvalid.core.storage;
import com.higgs.trust.consensus.p2pvalid.dao.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReceiveService {

    @Autowired
    private ReceiveCommandDao receiveCommandDao;

    @Autowired
    private ReceiveNodeDao receiveNodeDao;

    @Autowired
    private QueuedSendDao queuedSendDao;

    @Autowired
    private QueuedSendDelayDao queuedSendDelayDao;

    @Autowired
    private QueuedSendGcDao queuedSendGcDao;

}

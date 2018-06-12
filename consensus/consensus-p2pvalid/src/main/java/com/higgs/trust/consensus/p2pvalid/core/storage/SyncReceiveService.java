package com.higgs.trust.consensus.p2pvalid.core.storage;

import com.higgs.trust.consensus.p2pvalid.core.ResponseCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidResponseWrap;
import com.higgs.trust.consensus.p2pvalid.core.ValidSyncCommit;
import org.springframework.stereotype.Component;

@Component public class SyncReceiveService extends BaseReceiveService {

    @Override public ValidResponseWrap<? extends ResponseCommand> receive(ValidCommand validCommand) {
        Object object = validConsensus.getValidExecutor().execute(new ValidSyncCommit(validCommand));
        return ValidResponseWrap.successResponse(object);
    }
}

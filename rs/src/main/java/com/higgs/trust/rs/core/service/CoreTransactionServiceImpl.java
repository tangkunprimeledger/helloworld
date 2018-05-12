package com.higgs.trust.rs.core.service;

import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.dao.CoreTransactionDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service public class CoreTransactionServiceImpl implements CoreTransactionService {
    private final static Logger LOG = Logger.getLogger(CoreTransactionServiceImpl.class);
    @Autowired private CoreTransactionDao coreTransactionDao;
}

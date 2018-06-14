package com.higgs.trust.rs.core.service;

import com.higgs.trust.rs.core.api.BizTypeService;
import com.higgs.trust.rs.core.repository.BizTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service @Slf4j public class BizTypeServiceImpl implements BizTypeService {
    @Autowired BizTypeRepository bizTypeRepository;

    @Override public String getByPolicyId(String policyId) {
        return bizTypeRepository.getByPolicyId(policyId);
    }
}

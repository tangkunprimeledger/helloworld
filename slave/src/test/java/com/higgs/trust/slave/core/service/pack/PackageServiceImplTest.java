package com.higgs.trust.slave.core.service.pack;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.core.repository.PackageRepository;
import com.higgs.trust.slave.model.bo.Package;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/*
 *
 * @desc
 * @author tangfashuang
 * @date 2018/4/14
 *
 */
public class PackageServiceImplTest extends BaseTest{

    @Autowired
    private PackageService packageService;

    @Autowired
    private PackageRepository packageRepository;

//    private Package pack;

    @Test public void create() {
    }

    @Test public void receive() {
        Package pack = packageRepository.load(2L);
        packageService.receive(pack);
    }

    @Test public void validating() {
    }

    @Test public void validateConsensus() {
    }

    @Test public void validated() {
    }

    @Test public void persisting() {
    }

    @Test public void persistConsensus() {
    }

    @Test public void persisted() {
    }

    @Test public void remove() {
    }

    @Test public void getSign() {
    }
}
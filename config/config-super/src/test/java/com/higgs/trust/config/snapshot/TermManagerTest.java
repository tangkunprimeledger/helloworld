package com.higgs.trust.config.snapshot;

import com.higgs.trust.consensus.config.NodeState;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;

/**
 * @author Zhu_Yuanxiang
 * @create 2018-09-20
 */
public class TermManagerTest extends PowerMockTestCase {

    @InjectMocks
    @Autowired
    TermManager termManager;

    @Mock
    NodeState nodeState;
    @Mock
    TermProperties properties;

    @BeforeClass
    public void before() {}

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(properties.getMaxTermsSize()).thenReturn(20);
    }

    @Test
    public void testStartNewTerm() {
        Mockito.when(nodeState.getNodeName()).thenReturn("master");
        Mockito.when(nodeState.getMasterName()).thenReturn("master");

        long initTerm = 1;
        TermInfo terminfo = new TermInfo(initTerm, "master", 200, 400);
        ArrayList<TermInfo> terms = new ArrayList<>();
        terms.add(terminfo);
        termManager.resetTerms(terms);
        for (int a = 0; a < 10; a++) {
            Mockito.when(nodeState.getCurrentTerm()).thenReturn(initTerm + a);
            termManager.startNewTerm(initTerm + 1 + a, "new" + a);
        }
        System.out.println(termManager.getTerms());
        Assert.assertEquals(11, termManager.getTerms().size());
        Assert.assertEquals(terminfo, termManager.getTerms().get(0));
    }

    @Test
    public void testStartNewTermWithStartHeight() {
        Mockito.when(nodeState.getNodeName()).thenReturn("master");
        Mockito.when(nodeState.getMasterName()).thenReturn("master");

        long initTerm = 1;
        long initEndHeight = 400;
        TermInfo terminfo = new TermInfo(initTerm, "master", 200, initEndHeight);
        ArrayList<TermInfo> terms = new ArrayList<>();
        terms.add(terminfo);
        termManager.resetTerms(terms);
        for (int a = 0; a < 10; a++) {
            Mockito.when(nodeState.getCurrentTerm()).thenReturn(initTerm + a);
            termManager.startNewTerm(initTerm + 1 + a, "new" + a, initEndHeight);
            initEndHeight += 100;
        }
        System.out.println(termManager.getTerms());
        Assert.assertEquals(11, termManager.getTerms().size());
        Assert.assertEquals(terminfo, termManager.getTerms().get(0));
    }
}

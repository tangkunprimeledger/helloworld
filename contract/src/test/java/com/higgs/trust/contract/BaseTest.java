package com.higgs.trust.contract;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;

public abstract class BaseTest {

    @BeforeClass public static void beforeClass() {

    }

    @Before public void runBefore() {
        return;
    }

    @After public void runAfter() {
        runLast();
    }

    protected void runLast() {
    }

    protected String loadCodeFromResourceFile(String fileName) {
        try {
            return IOUtils.toString(this.getClass().getResource(fileName), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
package com.higgs.trust.rs.custom.api;

import com.higgs.trust.rs.custom.config.RsPropertiesConfig;
import com.higgs.trust.rs.custom.exceptions.BankChainException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;

/**
 * @author lingchao
 */
public abstract class StatefulService {

    public static final String STATUS_FILE_PREFIX = ".bankchain" + File.separator + "enable_";
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private volatile boolean isStop = true;

    @Autowired
    private RsPropertiesConfig propertiesConfig;

    public abstract String getStatefulServiceName();


    protected abstract void doStart();

    protected abstract void doPause();

    protected abstract void doResume();

    protected void doAfterInit(boolean isStart) {
    }

    public final synchronized void init() {
        LOGGER.info("{} [INIT开始]", getStatefulServiceName());
        boolean isStart;
        if (isExistStatusFile()) {
            start();
            isStart = true;
        } else {
            LOGGER.info("状态文件{}不存在，不启动{}", getStatusFile().getAbsolutePath(), getStatefulServiceName());
            isStart = false;
        }
        doAfterInit(isStart);
        LOGGER.info("{} [INIT完成]", getStatefulServiceName());
    }

    public final synchronized void start() {
        LOGGER.info("{} [Starting]", getStatefulServiceName());

        try {
            doStart();
        } catch (Exception e) {
            LOGGER.error(getStatefulServiceName() + " [FAIL]", e);
            throw e;
        }
        isStop = false;
        createStatusFile();
        LOGGER.info("{} [OK]", getStatefulServiceName());
    }

    public final synchronized void startOrResume() {
        LOGGER.info("{} startOrResume - 开始", getStatefulServiceName());
        if (isStop) {
            start();
        } else {
            resume();
        }
        LOGGER.info("{} startOrResumeIf - 结束, 执行成功", getStatefulServiceName());
    }

    public final synchronized void pause() {
        LOGGER.info("{} [Pausing]", getStatefulServiceName());
        doPause();
        deleteStatusFile();
        LOGGER.info("{} [Paused]", getStatefulServiceName());

    }

    public final synchronized void resume() {
        LOGGER.info("{} [Resuming]", getStatefulServiceName());
        doResume();
        createStatusFile();
        LOGGER.info("{} [OK]", getStatefulServiceName());
    }

    private void createStatusFile() {
        try {
            File statusFile = getStatusFile();
            FileUtils.forceMkdirParent(statusFile);
            boolean ret = statusFile.createNewFile();
            LOGGER.info("创建statusFile [{}], ret={}", statusFile.getAbsolutePath(), ret);
        } catch (IOException e) {
            throw new BankChainException(e);
        }
    }

    private void deleteStatusFile() {
        File statusFile = getStatusFile();
        boolean ret = statusFile.delete();
        LOGGER.info("删除statusFile [{}], ret={}", statusFile.getAbsolutePath(), ret);
    }

    private boolean isExistStatusFile() {
        return getStatusFile().exists();
    }

    private File getStatusFile() {
        return new File(propertiesConfig.getCoreStatusRootPath() + File.separator + STATUS_FILE_PREFIX + getStatefulServiceName());
    }

}

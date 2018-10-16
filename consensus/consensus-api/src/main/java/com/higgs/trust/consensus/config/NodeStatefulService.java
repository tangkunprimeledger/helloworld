package com.higgs.trust.consensus.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;

/**
 *node state  status file service
 * @author lingchao
 * @create 2018年09月21日14:27
 */
@Slf4j
public abstract class NodeStatefulService {
    
    public static final String STATUS_FILE_PREFIX = ".trust" + File.separator + "enable_";

    private volatile boolean isStop = true;

    @Autowired
    private NodeProperties nodeProperties;

    public abstract String getStatefulServiceName();


    protected abstract void doStart();

    protected abstract void doPause();

    protected abstract void doResume();

    protected void doAfterInit(boolean isStart) {
    }

    public final synchronized void init() {
        log.info("{} [INIT开始]", getStatefulServiceName());
        boolean isStart;
        if (isExistStatusFile()) {
            start();
            isStart = true;
        }
        else {
            log.info("状态文件{}不存在，不启动{}", getStatusFile().getAbsolutePath(), getStatefulServiceName());
            isStart = false;
        }
        doAfterInit(isStart);
        log.info("{} [INIT完成]", getStatefulServiceName());
    }

    public final synchronized void start() {
        log.info("{} [Starting]", getStatefulServiceName());

        try {
            doStart();
        } catch (Exception e) {
            log.error(getStatefulServiceName() + " [FAIL]", e);
            throw e;
        }
        isStop = false;
        createStatusFile();
        log.info("{} [OK]", getStatefulServiceName());
    }

    public final synchronized void startOrResume() {
        log.info("{} startOrResume - 开始", getStatefulServiceName());
        if (isStop) {
            start();
        }
        else {
            resume();
        }
        log.info("{} startOrResumeIf - 结束, 执行成功", getStatefulServiceName());
    }

    public final synchronized void pause() {
        log.info("{} [Pausing]", getStatefulServiceName());
        doPause();
        deleteStatusFile();
        log.info("{} [Paused]", getStatefulServiceName());

    }

    public final synchronized void resume() {
        log.info("{} [Resuming]", getStatefulServiceName());
        doResume();
        createStatusFile();
        log.info("{} [OK]", getStatefulServiceName());
    }

    private void createStatusFile() {
        try {
            File statusFile = getStatusFile();
            FileUtils.forceMkdirParent(statusFile);
            boolean ret = statusFile.createNewFile();
            log.info("创建statusFile [{}], ret={}", statusFile.getAbsolutePath(), ret);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteStatusFile() {
        File statusFile = getStatusFile();
        boolean ret = statusFile.delete();
        log.info("删除statusFile [{}], ret={}", statusFile.getAbsolutePath(), ret);
    }

    private boolean isExistStatusFile() {
        return getStatusFile().exists();
    }

    private File getStatusFile() {
        return new File(nodeProperties.getPath() + File.separator + STATUS_FILE_PREFIX + getStatefulServiceName());
    }

}

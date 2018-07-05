package com.higgs.trust.consensus.bftsmartcustom.started.custom.client;

import bftsmart.tom.ServiceProxy;
import com.higgs.trust.consensus.core.ConsensusClient;
import com.higgs.trust.consensus.core.command.ConsensusCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class Client implements ConsensusClient {

    private static final Logger log = LoggerFactory.getLogger(Client.class);

    private ServiceProxy serviceProxy;

    @Value("${bftSmart.systemConfigs.myClientId}")
    private String myClientId;

    public void init() {
        log.info("-----ServiceProxy init-----");
        serviceProxy = new ServiceProxy(Integer.valueOf(myClientId));
    }

    public void processingPackage(ConsensusCommand command) {
        byte[] bytes = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(command);
            objectOutputStream.flush();
            bytes = byteArrayOutputStream.toByteArray();
            log.debug("service invoke ordered");
            byte[] reply = serviceProxy.invokeOrdered(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    log.error("source close error", e);
                }
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    log.error("source close error", e);
                }
            }
        }
    }

    @Override
    public <T> CompletableFuture<T> submit(ConsensusCommand<T> command) {
        CompletableFuture completableFuture = CompletableFuture.runAsync(() -> {
            processingPackage(command);
        });
        return completableFuture;
    }
}

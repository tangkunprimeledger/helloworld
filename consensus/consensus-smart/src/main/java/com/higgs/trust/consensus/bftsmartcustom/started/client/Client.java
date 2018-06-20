package com.higgs.trust.consensus.bftsmartcustom.started.client;

import bftsmart.tom.ServiceProxy;
import com.higgs.trust.consensus.bftsmartcustom.started.config.SmartServerConfig;
import com.higgs.trust.consensus.core.ConsensusClient;
import com.higgs.trust.consensus.core.command.ConsensusCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.CompletableFuture;

public class Client implements ConsensusClient {

    private static final Logger log = LoggerFactory.getLogger(Client.class);

    private ServiceProxy serviceProxy;

    private int clientId;

    public Client(int clientId) {
        this.clientId = clientId;
    }
    public void init() {
        log.info("-----ServiceProxy init-----");
        serviceProxy = new ServiceProxy(clientId);
    }

    public void processingPackage(ConsensusCommand command) {
        System.out.println("process start");
        byte[] bytes = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(command);
            objectOutputStream.flush();
            bytes = byteArrayOutputStream.toByteArray();
            byte[] reply = serviceProxy.invokeOrdered(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public <T> CompletableFuture<T> submit(ConsensusCommand<T> command) {
        processingPackage(command);
        return null;
    }
}

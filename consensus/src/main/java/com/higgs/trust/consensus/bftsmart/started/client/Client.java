package com.higgs.trust.consensus.bftsmart.started.client;

import com.higgs.trust.consensus.bftsmart.tom.ServiceProxy;
import com.higgs.trust.consensus.core.ConsensusClient;
import com.higgs.trust.consensus.core.command.ConsensusCommand;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.CompletableFuture;

public class Client implements ConsensusClient {
    private ServiceProxy serviceProxy;

    public Client(int clientId) {
        serviceProxy = new ServiceProxy(clientId);
    }

    public void processingPackage(ConsensusCommand command) {
        System.out.println("process start");
        System.out.println(command);
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

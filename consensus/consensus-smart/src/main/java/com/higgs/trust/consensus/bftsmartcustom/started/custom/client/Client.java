package com.higgs.trust.consensus.bftsmartcustom.started.custom.client;

import bftsmart.tom.ServiceProxy;
import com.higgs.trust.consensus.core.ConsensusClient;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import io.atomix.utils.serializer.Namespace;
import io.atomix.utils.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component public class Client implements ConsensusClient {

    private static final Logger log = LoggerFactory.getLogger(Client.class);

    private ServiceProxy serviceProxy;

    @Value("${bftSmart.systemConfigs.myClientId}") private String myClientId;

    private Serializer serializer;

    public void init() {
        log.info("-----ServiceProxy init-----");
        Namespace namespace = Namespace.builder()
            .setRegistrationRequired(false)
            .setCompatible(true)
            .register(AbstractConsensusCommand.class).build();
        serializer = Serializer.using(namespace);
        serviceProxy = new ServiceProxy(Integer.valueOf(myClientId));
    }

    private void submitCommand(AbstractConsensusCommand command) {
        try {
            byte[] bytes = serializer.encode(command);
            log.debug("service invoke ordered");
            byte[] reply = serviceProxy.invokeOrdered(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override public <T> CompletableFuture<?> submit(AbstractConsensusCommand<T> command) {
        CompletableFuture completableFuture = CompletableFuture.runAsync(() -> submitCommand(command));
        return completableFuture;
    }
}

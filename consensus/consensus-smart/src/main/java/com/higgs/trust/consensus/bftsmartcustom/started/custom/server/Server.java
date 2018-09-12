package com.higgs.trust.consensus.bftsmartcustom.started.custom.server;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultRecoverable;
import com.higgs.trust.consensus.bftsmartcustom.started.custom.SmartCommitReplicateComposite;
import com.higgs.trust.consensus.core.ConsensusCommit;
import com.higgs.trust.consensus.core.IConsensusSnapshot;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import io.atomix.utils.serializer.Namespace;
import io.atomix.utils.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author: Zhouyafeng
 * @create: 2018/6/15 14:57
 * @description:
 */
public class Server extends DefaultRecoverable {

    private static Logger log = LoggerFactory.getLogger(Server.class);
    SmartCommitReplicateComposite machine;
    Map<Class<?>, Function<ConsensusCommit<?>, ?>> functionMap;

    private ServiceReplica serviceReplica;

    private IConsensusSnapshot snapshot;

    private Serializer serializer;

    public Server(int serverId, IConsensusSnapshot snapshot, SmartCommitReplicateComposite replicateComposite) {
        this.snapshot = snapshot;
        this.machine = replicateComposite;
        functionMap = machine.registerCommit();
        Namespace namespace = Namespace.builder().setRegistrationRequired(false).setCompatible(true)
            .register(AbstractConsensusCommand.class).build();
        serializer = Serializer.using(namespace);
        serviceReplica = new ServiceReplica(serverId, this, this);
        //先从spring容器中获取对应的bean，如果不存在则反射实例化一个
        //        try {
        //            try {
        //                machine = SpringUtil.getBean(SmartCommitReplicateComposite.class);
        //                functionMap = machine.registerCommit();
        //            } catch (Exception e) {
        //                log.info("Getting the bean from the container fails.");
        //            }
        //        } catch (Exception e) {
        //            log.info("IllegalAccessException:" + e.getLocalizedMessage());
        //            return;
        //        }
    }

    @Override public void installSnapshot(byte[] state) {
        this.snapshot.installSnapshot(state);
    }

    @Override public byte[] getSnapshot() {
        return this.snapshot.getSnapshot();
    }

    @Override public byte[][] appExecuteBatch(byte[][] commands, MessageContext[] msgCtxs, boolean fromConsensus) {
        byte[][] replies = new byte[commands.length][];
        for (int i = 0; i < commands.length; i++) {
            replies[i] = executeSingle(commands[i], msgCtxs[i]);
        }
        return replies;
    }

    private byte[] executeSingle(byte[] command, MessageContext msgCtx) {
        try {
            AbstractConsensusCommand abstractConsensusCommand = serializer.decode(command);
            //共识结束，回调客户端
            if (Objects.nonNull(abstractConsensusCommand)) {
                if (functionMap.containsKey(abstractConsensusCommand.getClass())) {
                    Function function = functionMap.get(abstractConsensusCommand.getClass());
                    if (function != null) {
                        while (true) {
                            try {
                                function.apply(abstractConsensusCommand);
                                break;
                            } catch (Throwable e) {
                                log.error("apply error {}", e.getMessage());
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e1) {
                                    log.error(e1.getMessage());
                                }
                            }
                        }
                    } else {
                        log.error("The corresponding method is not registered.-- {}",
                            abstractConsensusCommand.getClass().getSimpleName());
                    }
                } else {
                    log.error("The corresponding method is not registered.-- {}",
                        abstractConsensusCommand.getClass().getSimpleName());
                }
            }
            return null;
        } catch (Exception e) {
            log.error("execute command error: " + e.getMessage(), e);
            return null;
        }
    }

    @Override public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        return executeSingle(command, msgCtx);
    }

    public ServiceReplica getServiceReplica() {
        return serviceReplica;
    }
}

package com.higgs.trust.consensus.bftsmartcustom.started.custom.server;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultRecoverable;
import com.google.common.base.Charsets;
import com.higgs.trust.consensus.bftsmartcustom.started.custom.SmartCommitReplicateComposite;
import com.higgs.trust.consensus.core.ConsensusCommit;
import com.higgs.trust.consensus.core.ConsensusSnapshot;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
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

    private ConsensusSnapshot snapshot;

    public Server(int serverId, ConsensusSnapshot snapshot, SmartCommitReplicateComposite machine) {
        this.snapshot = snapshot;
        this.machine = machine;
        functionMap = machine.registerCommit();
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
        this.snapshot.installSnapshot(new String(state, Charsets.UTF_8));
    }

    @Override public byte[] getSnapshot() {
        String snapshot = this.snapshot.getSnapshot();
        return snapshot.getBytes(Charsets.UTF_8);
    }

    @Override public byte[][] appExecuteBatch(byte[][] commands, MessageContext[] msgCtxs, boolean fromConsensus) {
        byte[][] replies = new byte[commands.length][];
        for (int i = 0; i < commands.length; i++) {
            replies[i] = executeSingle(commands[i], msgCtxs[i]);
        }
        return replies;
    }

    private byte[] executeSingle(byte[] command, MessageContext msgCtx) {
        ByteArrayInputStream in = new ByteArrayInputStream(command);
        ObjectInput objectInput = null;
        try {
            objectInput = new ObjectInputStream(in);
            AbstractConsensusCommand abstractConsensusCommand = (AbstractConsensusCommand)objectInput.readObject();
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
        } catch (IOException e) {
            log.error("Exception reading data in the replica: " + e.getMessage(), e);
            return null;
        } catch (ClassNotFoundException e) {
            log.error("Coudn't find List: " + e.getMessage(), e);
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

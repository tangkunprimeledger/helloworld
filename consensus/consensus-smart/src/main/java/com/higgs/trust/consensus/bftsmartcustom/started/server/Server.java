package com.higgs.trust.consensus.bftsmartcustom.started.server;

import com.higgs.trust.consensus.bftsmartcustom.started.SpringUtil;
import com.higgs.trust.consensus.bftsmartcustom.started.SmartCommitReplicateComposite;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultRecoverable;
import com.higgs.trust.consensus.core.ConsensusCommit;
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
 *
 * @author: Zhouyafeng
 * @create: 2018/6/15 14:57
 * @description:
 *
 */
public class Server extends DefaultRecoverable {

    private static Logger log = LoggerFactory.getLogger(Server.class);
    SmartCommitReplicateComposite machine;
    Map<Class<?>, Function<ConsensusCommit<?>, ?>> functionMap;
    public Server(int serverId) {
        new ServiceReplica(serverId, this, this);
        //先从spring容器中获取对应的bean，如果不存在则反射实例化一个
        try {
            try {
                machine = SpringUtil.getBean(SmartCommitReplicateComposite.class);
                functionMap = machine.registerCommit();
            } catch (Exception e) {
                log.info("Getting the bean from the container fails.");
            }
        } catch (Exception e) {
            log.info("IllegalAccessException:" + e.getLocalizedMessage());
            return;
        }
    }

    @Override
    public void installSnapshot(byte[] state) {
        //暂定不使用快照
//        ByteArrayInputStream bis = new ByteArrayInputStream(state);
//        try {
//            ObjectInput in = new ObjectInputStream(bis);
//            packageList = (List<MyPackage>) in.readObject();
//            in.close();
//            bis.close();
//        } catch (ClassNotFoundException e) {
//            System.out.print("Coudn't find List: " + e.getMessage());
//            e.printStackTrace();
//        } catch (IOException e) {
//            System.out.print("Exception installing the application state: " + e.getMessage());
//            e.printStackTrace();
//        }
    }

    @Override
    public byte[] getSnapshot() {
        return new byte[0];
    }

    @Override
    public byte[][] appExecuteBatch(byte[][] commands, MessageContext[] msgCtxs, boolean fromConsensus) {
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
            AbstractConsensusCommand abstractConsensusCommand = (AbstractConsensusCommand) objectInput.readObject();
            //共识结束，回调客户端
            if (Objects.nonNull(abstractConsensusCommand)) {
                if (functionMap.containsKey(abstractConsensusCommand.getClass())) {
                    Function function = functionMap.get(abstractConsensusCommand.getClass());
                    if (function != null) {
                        function.apply(abstractConsensusCommand);
                    } else {
                        log.info("The corresponding method is not registered.-- {}",abstractConsensusCommand.getClass().getSimpleName());
                    }
                } else {
                    log.info("The corresponding method is not registered.-- {}",abstractConsensusCommand.getClass().getSimpleName());
                }
            }
            return null;
        } catch (IOException e) {
            System.out.println("Exception reading data in the replica: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            System.out.print("Coudn't find List: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        return executeSingle(command, msgCtx);
    }
}

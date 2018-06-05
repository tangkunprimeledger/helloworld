package com.higgs.trust.consensus.bftsmart.started.server;

import com.higgs.trust.consensus.bft.core.template.AbstractConsensusCommand;
import com.higgs.trust.consensus.bftsmart.reconfiguration.util.SpringUtil;
import com.higgs.trust.consensus.bftsmart.started.SmartAbstractConsensusStateMachine;
import com.higgs.trust.consensus.bftsmart.started.config.SmartConfig;
import com.higgs.trust.consensus.bftsmart.tom.MessageContext;
import com.higgs.trust.consensus.bftsmart.tom.ServiceReplica;
import com.higgs.trust.consensus.bftsmart.tom.server.defaultservices.DefaultRecoverable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class Server extends DefaultRecoverable {

    private static Logger log = LoggerFactory.getLogger(Server.class);
    SmartConfig smartConfig;
    SmartAbstractConsensusStateMachine machine;
    public Server(int serverId) {
        new ServiceReplica(serverId, this, this);
        smartConfig = SpringUtil.getBean(SmartConfig.class);
        if (smartConfig == null) {
            log.info("the config is not found");
            return;
        }
        String stateMachineClass = smartConfig.getStateMachineClass();
        if (StringUtils.isEmpty(stateMachineClass)) {
            log.info("stateMachineClass config is not found");
            return;
        }
        //先从spring容器中获取对应的bean，如果不存在则反射实例化一个
        try {
            Class<?> clazz = Class.forName(stateMachineClass);
            try {
                machine = SpringUtil.getBean(org.apache.commons.lang3.StringUtils.uncapitalize(clazz.getSimpleName()), SmartAbstractConsensusStateMachine.class);
            } catch (Exception e) {
                log.info("Getting the bean from the container fails.");
            }
            if (Objects.isNull(machine)) {
                machine = (SmartAbstractConsensusStateMachine)clazz.newInstance();
                if (Objects.isNull(machine)) {
                    log.info("The reflection gets the instance failure.");
                } else {
                    log.info("The reflection gets the instance success.");
                }
            }
        } catch (ClassNotFoundException e) {
            log.info("the class " + stateMachineClass + "is not found");
            return;
        } catch (IllegalAccessException e) {
            log.info("IllegalAccessException:" + e.getLocalizedMessage());
            return;
        } catch (InstantiationException e) {
            log.info("InstantiationException:" + e.getLocalizedMessage());
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
            AbstractConsensusCommand abstractConsensusCommand = (com.higgs.trust.consensus.bft.core.template.AbstractConsensusCommand)objectInput.readObject();
            //共识结束，回调客户端
            if (Objects.nonNull(abstractConsensusCommand)) {
                if (machine.valueOperationMap.containsKey(abstractConsensusCommand.getClass())) {
                    Function function = machine.valueOperationMap.get(abstractConsensusCommand.getClass());
                    if (function != null) {
                        function.apply(abstractConsensusCommand);
                    } else {
                        System.out.println("未注册相应的处理方法1");
                        log.info("The corresponding method is not registered.-- {}",abstractConsensusCommand.getClass().getSimpleName());
                    }
                } else if (machine.voidOperationMap.containsKey(abstractConsensusCommand.getClass())) {
                    Consumer consumer = machine.voidOperationMap.get(abstractConsensusCommand.getClass());
                    if (consumer != null) {
                        consumer.accept(abstractConsensusCommand);
                    } else {
                        System.out.println("未注册相应的处理方法2");
                        log.info("The corresponding method is not registered.-- {}",abstractConsensusCommand.getClass().getSimpleName());
                    }
                } else {
                    System.out.println("未注册相应的处理方法3");
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

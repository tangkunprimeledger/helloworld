package com.higgs.trust.consensus.p2pvalid.core;

import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.exception.ReceiveException;
import com.higgs.trust.consensus.p2pvalid.core.exchange.ConsensusContext;
import com.higgs.trust.consensus.p2pvalid.core.exchange.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.spi.ClusterInfo;
import com.higgs.trust.consensus.p2pvalid.core.storage.entry.impl.ReceiveCommandStatistics;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.reflect.*;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author cwy
 */
@Slf4j
public abstract class ValidConsensus {

    private final static String BACKSLASH = "/";
    private final static String SLASH = "\\";
    private ValidExecutor executor;
    private ClusterInfo clusterInfo;
    private ConsensusContext consensusContext;

    public ValidConsensus(ClusterInfo clusterInfo, P2pConsensusClient p2pConsensusClient, String baseDir) {
        this.clusterInfo = clusterInfo;
        initContest(baseDir, p2pConsensusClient);
        executor = new ValidExecutor();
        config();
    }

    private void initContest(String baseDir, P2pConsensusClient p2pConsensusClient) {
        if (!baseDir.endsWith(BACKSLASH) && !baseDir.endsWith(SLASH)) {
            baseDir = baseDir.concat(BACKSLASH);
        }
        File file = new File(baseDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        consensusContext = new ConsensusContext(this, p2pConsensusClient, baseDir, clusterInfo.faultNodeNum());
    }

    public final void submit(ValidCommand<?> command) {
        try{
            String fromNode = clusterInfo.myNodeName();
            String messageDigest = command.messageDigest();
            String sign = fromNode + "_" + messageDigest;
            ValidCommandWrap validCommandWrap = ValidCommandWrap.of(command)
                    .fromNodeName(fromNode)
                    .messageDigest(messageDigest)
                    .addToNodeNames(clusterInfo.clusterNodeNames())
                    .sign(SignUtils.sign(messageDigest, clusterInfo.privateKey()));
            consensusContext.submit(validCommandWrap);
        }catch (Exception e){
            throw  new RuntimeException(e);
        }
    }

    public final void submit(ValidCommand<?> command, String toNodeName) {
        try{

            String fromNode = clusterInfo.myNodeName();
            String messageDigest = command.messageDigest();
            String sign = fromNode + "_" + messageDigest;
            ValidCommandWrap validCommandWrap = ValidCommandWrap.of(command)
                    .fromNodeName(fromNode)
                    .messageDigest(messageDigest)
                    .addToNodeName(toNodeName)
                    .sign(SignUtils.sign(messageDigest, clusterInfo.privateKey()));
            consensusContext.submit(validCommandWrap);
        }catch (Exception e){
            throw  new RuntimeException(e);
        }
    }

    public final void submit(ValidCommand<?> command, Collection<String> toNodeNames) {
        try{
            String fromNode = clusterInfo.myNodeName();
            String messageDigest = command.messageDigest();
            String sign = fromNode + "_" + messageDigest;
            ValidCommandWrap validCommandWrap = ValidCommandWrap.of(command)
                    .fromNodeName(fromNode)
                    .messageDigest(messageDigest)
                    .addToNodeNames(toNodeNames)
                    .sign(SignUtils.sign(messageDigest, clusterInfo.privateKey()));
            consensusContext.submit(validCommandWrap);
        }catch (Exception e){
            throw  new RuntimeException(e);
        }
    }

    public void receive(ValidCommandWrap validCommandWrap) throws ReceiveException {

        if(StringUtils.isEmpty(validCommandWrap.getFromNodeName())){
            throw new ReceiveException("from node name can not be null");
        }
        String pubKey = clusterInfo.pubKey(validCommandWrap.getFromNodeName());
        if(StringUtils.isEmpty(pubKey)){
            throw new ReceiveException(String.format("unknown pubKey for node %s", validCommandWrap.getFromNodeName()));
        }
        try {
            if(!SignUtils.verify(validCommandWrap.getMessageDigest(), validCommandWrap.getSign(), pubKey)){
                throw new Exception(String.format("check sign failed for node %s", validCommandWrap.getFromNodeName()));
            }
        }catch (Exception e){
            throw new ReceiveException(String.format("invalid command from node %s", validCommandWrap.getFromNodeName()));
        }
        consensusContext.receive(validCommandWrap);
    }

    public void apply(ReceiveCommandStatistics receiveCommandStatistics) {
        ValidCommit validCommit = ValidCommit.of(receiveCommandStatistics);
        executor.excute(validCommit);
    }

    public ValidExecutor getValidExecutor(){
        return executor;
    }

    public void config() {
        registerOperations();
    }

    /**
     * Registers operations for the class.
     */
    private void registerOperations() {
        Class<?> type = getClass();
        for (Method method : type.getMethods()) {
            if (isOperationMethod(method)) {
                registerMethod(method);
            }
        }
    }

    /**
     * Returns a boolean value indicating whether the given method is an operation method.
     */
    private boolean isOperationMethod(Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        return paramTypes.length == 1 && paramTypes[0] == ValidCommit.class;
    }

    /**
     * Registers an operation for the given method.
     */
    private void registerMethod(Method method) {
        Type genericType = method.getGenericParameterTypes()[0];
        Class<?> argumentType = resolveArgument(genericType);
        if (argumentType != null && ValidCommand.class.isAssignableFrom(argumentType)) {
            registerMethod(argumentType, method);
        }
    }

    /**
     * Resolves the generic argument for the given type.
     */
    private Class<?> resolveArgument(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            return resolveClass(paramType.getActualTypeArguments()[0]);
        } else if (type instanceof TypeVariable) {
            return resolveClass(type);
        } else if (type instanceof Class) {
            TypeVariable<?>[] typeParams = ((Class<?>) type).getTypeParameters();
            return resolveClass(typeParams[0]);
        }
        return null;
    }

    /**
     * Resolves the generic class for the given type.
     */
    private Class<?> resolveClass(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return resolveClass(((ParameterizedType) type).getRawType());
        } else if (type instanceof WildcardType) {
            Type[] bounds = ((WildcardType) type).getUpperBounds();
            if (bounds.length > 0) {
                return (Class<?>) bounds[0];
            }
        }
        return null;
    }

    /**
     * Registers the given method for the given operation type.
     */
    private void registerMethod(Class<?> type, Method method) {
        Class<?> returnType = method.getReturnType();
        if (returnType == void.class || returnType == Void.class) {
            registerVoidMethod(type, method);
        } else {
            registerValueMethod(type, method);
        }
    }

    /**
     * Registers an operation with a void return value.
     */
    @SuppressWarnings("unchecked")
    private void registerVoidMethod(Class type, Method method) {
        executor.register(type, wrapVoidMethod(method));
    }

    /**
     * Wraps a void method.
     */
    private Consumer wrapVoidMethod(Method method) {
        return c -> {
            try {
                method.invoke(this, c);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Registers an operation with a non-void return value.
     */
    @SuppressWarnings("unchecked")
    private void registerValueMethod(Class type, Method method) {
        executor.register(type, wrapValueMethod(method));
    }

    /**
     * Wraps a value method.
     */
    private Function wrapValueMethod(Method method) {
        return c -> {
            try {
                return method.invoke(this, c);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}

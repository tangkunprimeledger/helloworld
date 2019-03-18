package com.higgs.trust.consensus.core;

import com.higgs.trust.common.utils.TraceUtils;
import com.higgs.trust.consensus.annotation.Replicator;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import com.higgs.trust.consensus.core.filter.CompositeCommandFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j public abstract class AbstractCommitReplicateComposite implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private CompositeCommandFilter filter;

    private Map<Class<?>, Function<ConsensusCommit<?>, ?>> classFunctionMap = new HashMap<>();

    public AbstractCommitReplicateComposite(CompositeCommandFilter filter) {
        this.filter = filter;
    }

    /**
     * Registers operations for the class.
     */
    public Map<Class<?>, Function<ConsensusCommit<?>, ?>> registerCommit() {
        if(classFunctionMap.isEmpty()) {
            Map<String, Object> withAnnotation = applicationContext.getBeansWithAnnotation(Replicator.class);
            withAnnotation.values().stream().forEach(object -> {
                Class<?> type = object.getClass();
                for (Method method : type.getMethods()) {
                    if (isOperationMethod(method)) {
                        registerMethod(object, method);
                    }
                }
            });
        }
        return classFunctionMap;
    }

    /**
     * Returns a boolean value indicating whether the given method is an operation method.
     */
    private boolean isOperationMethod(Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        return paramTypes.length == 1 && paramTypes[0] == ConsensusCommit.class;
    }

    /**
     * Registers an operation for the given method.
     */
    private void registerMethod(Object obj, Method method) {
        Type genericType = method.getGenericParameterTypes()[0];
        Class<?> argumentType = resolveArgument(genericType);
        if (argumentType != null && AbstractConsensusCommand.class.isAssignableFrom(argumentType)) {
            registerMethod(obj, method, argumentType);
        }
    }

    /**
     * Resolves the generic argument for the given type.
     */
    private Class<?> resolveArgument(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType)type;
            return resolveClass(paramType.getActualTypeArguments()[0]);
        } else if (type instanceof TypeVariable) {
            return resolveClass(type);
        } else if (type instanceof Class) {
            TypeVariable<?>[] typeParams = ((Class<?>)type).getTypeParameters();
            return resolveClass(typeParams[0]);
        }
        return null;
    }

    /**
     * Resolves the generic class for the given type.
     */
    private Class<?> resolveClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>)type;
        } else if (type instanceof ParameterizedType) {
            return resolveClass(((ParameterizedType)type).getRawType());
        } else if (type instanceof WildcardType) {
            Type[] bounds = ((WildcardType)type).getUpperBounds();
            if (bounds.length > 0) {
                return (Class<?>)bounds[0];
            }
        }
        return null;
    }

    /**
     * Registers the given method for the given operation type.
     */
    private void registerMethod(Object obj, Method method, Class<?> type) {
        Class<?> returnType = method.getReturnType();
        if (returnType == void.class || returnType == Void.class)
            classFunctionMap.put(type, wrapVoidMethod(obj, method));
        else {
            classFunctionMap.put(type, wrapValueMethod(obj, method));
        }
    }

    /**
     * Wraps a void method.
     */
    private Function wrapVoidMethod(Object obj, Method method) {
        return c -> {
            Consumer consumer = t -> {
                ConsensusCommit<? extends AbstractConsensusCommand> consensusCommit = commitAdapter(t);
                Span span = null;
                if (consensusCommit.operation() != null) {
                    span = TraceUtils.createSpan(consensusCommit.operation().getTraceId());
                }
                try {
                    filter.doFilter(consensusCommit);
                } catch (Exception e) {
                    log.error("filter commmit error", e);
                    return;
                }
                if (consensusCommit.isClosed()) {
                    return;
                }
                boolean logException = true;
                while (true) {
                    try {
                        method.invoke(obj, consensusCommit);
                        TraceUtils.closeSpan(span);
                        return;
                    } catch (Exception e) {
                        if(logException) {
                            log.error("apply error ", e);
                            logException = false;
                        }else{
                            log.error("apply error {}", e.getMessage());
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e1) {
                            log.error(e1.getMessage());
                        }
                    }
                }
            };
            consumer.accept(c);
            return null;
        };
    }

    /**
     * Wraps a value method.
     */
    private Function wrapValueMethod(Object obj, Method method) {
        return c -> {
            ConsensusCommit<? extends AbstractConsensusCommand> consensusCommit = commitAdapter(c);
            Span span = null;
            if (consensusCommit.operation() != null) {
                span = TraceUtils.createSpan(consensusCommit.operation().getTraceId());
            }
            try {
                filter.doFilter(consensusCommit);
            } catch (Exception e) {
                log.error("filter commmit error", e);
                return null;
            }
            if (consensusCommit.isClosed()) {
                return null;
            }
            while (true) {
                try {
                    Object result = method.invoke(obj, consensusCommit);
                    TraceUtils.closeSpan(span);
                    return result;
                } catch (Exception e) {
                    log.error("apply error {}", e);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e1) {
                        log.error(e1.getMessage());
                    }
                }
            }
        };
    }

    public abstract ConsensusCommit<? extends AbstractConsensusCommand> commitAdapter(Object request);

    @Override public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

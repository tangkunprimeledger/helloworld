package com.higgs.trust.consensus.p2pvalid.core;

import com.higgs.trust.consensus.p2pvalid.annotation.P2pvalidReplicator;
import com.higgs.trust.consensus.p2pvalid.core.storage.SendService;
import com.higgs.trust.consensus.p2pvalid.core.storage.SyncSendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.*;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author cwy
 */
@Component @Slf4j public class ValidConsensus implements ApplicationContextAware {

    private ValidExecutor executor;

    private ApplicationContext applicationContext;

    @Autowired private SendService sendService;

    @Autowired private SyncSendService syncSendService;

    public ValidConsensus() {
        executor = new ValidExecutor();
    }

    public final void submit(ValidCommand<?> command) {
        sendService.submit(command);
    }

    public final ResponseCommand<?> submitSync(ValidCommand<?> command) {
        return syncSendService.send(command);
    }

    public ValidExecutor getValidExecutor() {
        return executor;
    }

    /**
     * Registers operations for the class.
     */
    @PostConstruct public void registerOperations() {
        Map<String, Object> withAnnotation = applicationContext.getBeansWithAnnotation(P2pvalidReplicator.class);
        withAnnotation.values().stream().forEach(object -> {
            Class<?> type = object.getClass();
            for (Method method : type.getMethods()) {
                if (isOperationMethod(method)) {
                    registerMethod(object, method);
                }
            }
        });
    }

    /**
     * Returns a boolean value indicating whether the given method is an operation method.
     */
    private boolean isOperationMethod(Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        return paramTypes.length == 1 && ValidBaseCommit.class.isAssignableFrom(paramTypes[0]);
    }

    /**
     * Registers an operation for the given method.
     */
    private void registerMethod(Object object, Method method) {
        Type genericType = method.getGenericParameterTypes()[0];
        Class<?> argumentType = resolveArgument(genericType);
        if (argumentType != null && ValidCommand.class.isAssignableFrom(argumentType)) {
            registerMethod(object, method, argumentType);
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
        if (type instanceof Class<?>) {
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
    private void registerMethod(Object object, Method method, Class<?> type) {
        Class<?> returnType = method.getReturnType();
        if (returnType == void.class || returnType == Void.class) {
            registerVoidMethod(object, method, type);
        } else {
            registerValueMethod(object, method, type);
        }
    }

    /**
     * Registers an operation with a void return value.
     */
    @SuppressWarnings("unchecked") private void registerVoidMethod(Object object, Method method, Class type) {
        executor.register(type, wrapVoidMethod(object, method));
    }

    /**
     * Wraps a void method.
     */
    private Consumer wrapVoidMethod(Object object, Method method) {
        return c -> {
            try {
                method.invoke(object, c);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Registers an operation with a non-void return value.
     */
    @SuppressWarnings("unchecked") private void registerValueMethod(Object object, Method method, Class type) {
        executor.register(type, wrapValueMethod(object, method));
    }

    /**
     * Wraps a value method.
     */
    private Function wrapValueMethod(Object object, Method method) {
        return c -> {
            try {
                return method.invoke(object, c);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

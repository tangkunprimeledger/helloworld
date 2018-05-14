package com.higgs.trust.consensus.p2pvalid.core;

import com.higgs.trust.consensus.p2pvalid.core.storage.SendService;
import com.higgs.trust.consensus.p2pvalid.core.storage.SyncSendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author cwy
 */
@Slf4j public abstract class ValidConsensus {

    private ValidExecutor executor;

    @Autowired private SendService sendService;

    @Autowired private SyncSendService syncSendService;

    public ValidConsensus() {
        executor = new ValidExecutor();
        config();
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
        return paramTypes.length == 1 && ValidBaseCommit.class.isAssignableFrom(paramTypes[0]);
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
    @SuppressWarnings("unchecked") private void registerVoidMethod(Class type, Method method) {
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
    @SuppressWarnings("unchecked") private void registerValueMethod(Class type, Method method) {
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

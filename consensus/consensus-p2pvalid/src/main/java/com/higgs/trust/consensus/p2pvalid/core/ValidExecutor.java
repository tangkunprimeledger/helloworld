package com.higgs.trust.consensus.p2pvalid.core;

import org.omg.CORBA.portable.ValueBase;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author cwy
 */
public class ValidExecutor {

    private Map<Class<?>, Function> registry = new HashMap<>();

    public <T extends ValidCommand<U>, U extends Serializable> void register(Class<T> type, Function<T, U> function) {
        registry.put(type, function);
    }

    public Set<Class<?>> getKeySet() {
        return registry.keySet();
    }

    public <T extends ValidCommand<U>, U extends Serializable> void register(Class<T> type, Consumer<ValidBaseCommit> consumer) {
        registry.put(type, (Function<ValidBaseCommit, Void>)commit -> {
            consumer.accept(commit);
            return null;
        });
    }

    public Object execute(ValidBaseCommit commit) {
        Function function = registry.get(commit.type());
        return function.apply(commit);
    }
}

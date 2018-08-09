/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.config;

import io.atomix.core.AtomixRegistry;
import io.atomix.utils.NamedType;
import io.atomix.utils.ServiceException;
import io.atomix.utils.misc.StringUtils;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author suimi
 * @date 2018/8/8
 */
@Slf4j public class SpringBeanAtomixRegistry implements AtomixRegistry, ApplicationContextAware {

    private static final Map<ClassLoader, Map<SpringBeanAtomixRegistry.CacheKey, Map<Class<? extends NamedType>, Map<String, NamedType>>>>
        CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    private final Map<Class<? extends NamedType>, Map<String, NamedType>> registrations = new ConcurrentHashMap<>();
    private ClassLoader classLoader;
    private Class<? extends NamedType>[] types;
    private ApplicationContext applicationContext;

    public SpringBeanAtomixRegistry(ClassLoader classLoader, Class<? extends NamedType>... types) {
        this.classLoader = classLoader;
        this.types = types;
    }

    @PostConstruct public void init() {
        final Map<SpringBeanAtomixRegistry.CacheKey, Map<Class<? extends NamedType>, Map<String, NamedType>>> mappings =
            CACHE.computeIfAbsent(classLoader, cl -> new ConcurrentHashMap<>());
        final Map<Class<? extends NamedType>, Map<String, NamedType>> registrations =
            mappings.computeIfAbsent(new SpringBeanAtomixRegistry.CacheKey(types), cacheKey -> {
                final String[] whitelistPackages =
                    StringUtils.split(System.getProperty("io.atomix.whitelistPackages"), ",");
                final ClassGraph classGraph = whitelistPackages != null ?
                    new ClassGraph().enableClassInfo().whitelistPackages(whitelistPackages)
                        .addClassLoader(classLoader) : new ClassGraph().enableClassInfo().addClassLoader(classLoader);

                final ScanResult scanResult = classGraph.scan();
                final Map<Class<? extends NamedType>, Map<String, NamedType>> result = new ConcurrentHashMap<>();
                for (Class<? extends NamedType> type : cacheKey.types) {
                    final Map<String, NamedType> tmp = new ConcurrentHashMap<>();
                    scanResult.getClassesImplementing(type.getName()).forEach(classInfo -> {
                        if (classInfo.isInterface() || classInfo.isAbstract() || Modifier
                            .isPrivate(classInfo.getModifiers())) {
                            return;
                        }

                        Object bean = null;
                        try {
                            if (log.isDebugEnabled()) {
                                log.debug("get the bean from context:{}", classInfo.loadClass());
                            }
                            bean = applicationContext.getBean(classInfo.loadClass());
                            if (log.isDebugEnabled()) {
                                log.debug("get the bean:{}, classLoader:{}, NamedType classLoader:{}", bean,
                                    bean.getClass().getClassLoader(), NamedType.class.getClassLoader());
                            }
                        } catch (NoSuchBeanDefinitionException e) {

                        }
                        final NamedType instance;
                        if (bean != null) {
                            instance = (NamedType)bean;
                        } else {
                            instance = newInstance(classInfo.loadClass());
                        }
                        final NamedType oldInstance = tmp.put(instance.name(), instance);
                        if (oldInstance != null) {
                            log.warn("Found multiple types with name={}, classes=[{}, {}]", instance.name(),
                                oldInstance.getClass().getName(), instance.getClass().getName());
                        }
                    });
                    result.put(type, Collections.unmodifiableMap(tmp));
                }
                return result;
            });
        this.registrations.putAll(registrations);
    }

    @Override public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private static final class CacheKey {
        // intentionally no reference to ClassLoader to avoid leaks
        private final Class<? extends NamedType>[] types;

        CacheKey(Class<? extends NamedType>[] types) {
            this.types = types;
        }

        @Override public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            SpringBeanAtomixRegistry.CacheKey cacheKey = (SpringBeanAtomixRegistry.CacheKey)o;
            return Arrays.equals(types, cacheKey.types);
        }

        @Override public int hashCode() {
            return Arrays.hashCode(types);
        }
    }

    /**
     * Instantiates the given type using a no-argument constructor.
     *
     * @param type the type to instantiate
     * @param <T>  the generic type
     * @return the instantiated object
     * @throws ServiceException if the type cannot be instantiated
     */
    @SuppressWarnings("unchecked") private static <T> T newInstance(Class<?> type) {
        try {
            return (T)type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ServiceException("Cannot instantiate service class " + type, e);
        }
    }

    @Override @SuppressWarnings("unchecked") public <T extends NamedType> Collection<T> getTypes(Class<T> type) {
        Map<String, NamedType> types = registrations.get(type);
        return types != null ? (Collection<T>)types.values() : Collections.emptyList();
    }

    @Override @SuppressWarnings("unchecked") public <T extends NamedType> T getType(Class<T> type, String name) {
        Map<String, NamedType> types = registrations.get(type);
        return types != null ? (T)types.get(name) : null;
    }
}

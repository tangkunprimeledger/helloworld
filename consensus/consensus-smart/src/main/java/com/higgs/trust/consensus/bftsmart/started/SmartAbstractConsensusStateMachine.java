//package com.higgs.trust.consensus.bftsmart.started;
//
//
//import com.higgs.trust.consensus.bft.core.ConsensusCommit;
//import com.higgs.trust.consensus.bft.core.template.AbstractConsensusCommand;
//import com.higgs.trust.consensus.bftsmart.started.adapter.SmartCommitAdapter;
//
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.lang.reflect.ParameterizedType;
//import java.lang.reflect.Type;
//import java.util.Hashtable;
//import java.util.Map;
//import java.util.function.Consumer;
//import java.util.function.Function;
//
//
///**
// *
// * @author: Zhouyafeng
// * @create: 2018/4/25 15:41
// * @description:
// *
// */
//public abstract class SmartAbstractConsensusStateMachine {
//
//    public Map<Class<?>, Consumer> voidOperationMap = new Hashtable<>(8);
//    public Map<Class<?>, Function> valueOperationMap = new Hashtable<>(8);
//
//    public SmartAbstractConsensusStateMachine() {
//        registOperation();
//    }
//
//    public void registOperation() {
//        Class<?> clazz = getClass();
//        Method[] methods = clazz.getMethods();
//        for (Method method : methods) {
//            Class<?> typeClass = isOperationMethod(method);
//            if (typeClass != null) {
//                registerMethod(typeClass, method);
//            }
//        }
//    }
//
//    private void registerMethod(Class<?> type, Method method) {
//        Class<?> returnType = method.getReturnType();
//        if(returnType ==void.class ||returnType ==Void.class) {
//            voidOperationMap.put(type, getConsumer(method));
//        } else {
//            valueOperationMap.put(type, getFunction(method));
//        }
//    }
//
//
//
//    public Class<?> isOperationMethod(Method method) {
//        Class<?>[] parameters = method.getParameterTypes();
//        if (parameters.length == 1 && parameters[0] == ConsensusCommit.class) {
//            Type t = method.getGenericParameterTypes()[0];
//            if (t instanceof ParameterizedType) {
//                Type t1 = ((ParameterizedType)t).getActualTypeArguments()[0];
//                return (Class<?>)t1;
//            }
//        }
//        return null;
//    }
//
//    private Consumer getConsumer(Method method) {
//        return c -> {
//            try {
//                ConsensusCommit<? extends AbstractConsensusCommand> commit = new SmartCommitAdapter<>((AbstractConsensusCommand)c);
//                method.invoke(this,commit);
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            }
//        };
//    }
//
//    private Function getFunction(Method method) {
//        return c -> {
//            try {
//                ConsensusCommit<? extends AbstractConsensusCommand> commit = new SmartCommitAdapter<>((AbstractConsensusCommand)c);
//                return method.invoke(this,commit);
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            }
//            return null;
//        };
//    }
//
//    public static void main(String[] args) {
//    }
//}
//

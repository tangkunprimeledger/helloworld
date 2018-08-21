package com.higgs.trust.consensus.atomix.core.primitive;

import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import io.atomix.primitive.operation.OperationId;
import io.atomix.primitive.operation.OperationType;
import io.atomix.utils.serializer.KryoNamespace;
import io.atomix.utils.serializer.KryoNamespaces;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author Zhu_Yuanxiang
 * @create 2018-08-15
 */
@Slf4j
public class CommandPrimitiveSubmitOperation implements OperationId {

    private final OperationType type;

    public final KryoNamespace NAMESPACE;

    public CommandPrimitiveSubmitOperation(OperationType type, AbstractCommitReplicateComposite abstractCommitReplicateComposite) {
        this.type = type;
        Set<Class<?>> commandClasses = abstractCommitReplicateComposite.registerCommit().keySet();
        Set<Class<?>> classes = new HashSet<>(commandClasses);
        List<Class<?>> classList = new ArrayList<>();
        classes.stream().sorted(Comparator.comparing(Class::getSimpleName)).forEach(clazz->classList.add(clazz));
        NAMESPACE = KryoNamespace.builder()
                .register(KryoNamespaces.BASIC)
                .nextId(KryoNamespaces.BEGIN_USER_CUSTOM_ID)
                .register(AbstractConsensusCommand.class)
                .register(classList.toArray(new Class[classList.size()]))
                .build(CommandPrimitiveSubmitOperation.class.getSimpleName());
    }

    @Override
    public OperationType type() {
        return type;
    }

    @Override
    public String id() {
        return "command_primitive_submit";
    }
}

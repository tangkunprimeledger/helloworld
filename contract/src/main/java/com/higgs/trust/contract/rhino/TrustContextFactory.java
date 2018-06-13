package com.higgs.trust.contract.rhino;

import com.higgs.trust.contract.ExecuteConfig;
import com.higgs.trust.contract.QuotaExceededException;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;

/**
 * @author duhongming
 * @date 2018/6/7
 */
public class TrustContextFactory extends ContextFactory {

    private ExecuteConfig executeConfig;

    public static void install() {
        ContextFactory.getGlobalSetter().setContextFactoryGlobal(new TrustContextFactory());
        //ContextFactory.initGlobal(new TrustContextFactory());
    }

    public TrustContextFactory() {
//        System.out.println(" contractor TrustContextFactory");
//        System.out.println(this);
    }

    public TrustContextFactory(ExecuteConfig executeConfig) {
        this.executeConfig = executeConfig;
    }

    @Override
    protected Context makeContext() {
        TrustContext cx = new TrustContext(this);
        cx.quota = executeConfig.getInstructionCountQuota();
        cx.setInstructionObserverThreshold(cx.quota / 2);
        return cx;
    }

    @Override
    protected void observeInstructionCount(Context cx, int instructionCount) {
        TrustContext tcx = (TrustContext) cx;
        tcx.quota -= instructionCount;
        if (tcx.quota <= 0) {
            throw new QuotaExceededException("instructionCount exceeded");
        }
    }

    @Override
    protected Object doTopCall(Callable callable, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        assert cx instanceof TrustContext;
        return super.doTopCall(callable, cx, scope, thisObj, args);
    }
}

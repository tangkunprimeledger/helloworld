package com.higgs.trust.evmcontract.config;


import com.higgs.trust.evmcontract.vm.DataWord;
import com.higgs.trust.evmcontract.vm.GasCost;
import com.higgs.trust.evmcontract.vm.OpCode;
import com.higgs.trust.evmcontract.vm.program.Program;
import org.apache.commons.lang3.StringUtils;

/**
 * @author tangkun
 * @date 2018-09-06
 */
public class SystemProperties {

    private static SystemProperties CONFIG;
    private static boolean useOnlySpringConfig = false;
    private static final GasCost GAS_COST = new GasCost();

    /**
     * Returns the static config instance. If the config is passed
     * as a Spring bean by the application this instance shouldn't
     * be used
     * This method is mainly used for testing purposes
     * (Autowired fields are initialized with this static instance
     * but when running within Spring context they replaced with the
     * bean config instance)
     */
    public static SystemProperties getDefault() {
        return useOnlySpringConfig ? null : getSpringDefault();
    }

    static SystemProperties getSpringDefault() {
        if (CONFIG == null) {
            CONFIG = new SystemProperties();
        }
        return CONFIG;
    }


    public String vmTraceDir() {
//        return System.getProperty("vm.structured.dir");
        return "trace";
    }

    public int dumpBlock() {
//        return System.getProperty("dump.block");
        return 0;
    }

    public String dumpStyle() {
//        return config.getString("dump.style");
        return "pretty";
    }

    public boolean vmTrace() {
//        return Boolean.valueOf(System.getProperty("vm.structured.trace"));
        return true;
    }

    public String databaseDir() {
//        return System.getProperty("database.dir");
        return "db";
    }

    public boolean playVM() {
        return true;
    }

    public static GasCost getGasCost() {
        return GAS_COST;
    }

    public DataWord getCallGas(OpCode op, DataWord requestedGas, DataWord availableGas) throws Program.OutOfGasException {
        if (requestedGas.compareTo(availableGas) > 0) {
            throw Program.Exception.notEnoughOpGas(op, requestedGas, availableGas);
        }
        return requestedGas.clone();
    }

    public boolean recordInternalTransactionsData() {
//        if (recordInternalTransactionsData == null) {
//            recordInternalTransactionsData = config.getBoolean("record.internal.transactions.data");
//        }
//        return recordInternalTransactionsData;
        return true;
    }

    public BlockchainConfig getBlockchainConfig() {
        return new ByzantiumConfig();
    }

    public String getCryptoProviderName() {
        return "SC";
    }

    public String getHash256AlgName() {
        return "ETH-KECCAK-256";
    }

    public String getHash512AlgName() {
        return "ETH-KECCAK-512";
    }

    public String customSolcPath() {
        String path = System.getProperty("solcPath");
        if (StringUtils.isNotEmpty(path)) {
            return path;
        }

        path = System.getenv("SolcPath");
        if (StringUtils.isNotEmpty(path)) {
            return path;
        }

//        return "/solc/solc.exe";
        return null;
    }
}


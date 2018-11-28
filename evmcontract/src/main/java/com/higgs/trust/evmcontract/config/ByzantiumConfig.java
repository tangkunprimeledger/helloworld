package com.higgs.trust.evmcontract.config;


import com.higgs.trust.evmcontract.vm.DataWord;
import com.higgs.trust.evmcontract.vm.GasCost;
import com.higgs.trust.evmcontract.vm.OpCode;
import com.higgs.trust.evmcontract.vm.program.Program;
import org.springframework.stereotype.Component;

/**
 * The interface Contract repository.
 *
 * @author zhao xiaogang
 * @date 2018-09-08
 */
@Component
public class ByzantiumConfig implements BlockchainConfig {

    private static final GasCost GAS_COST = new GasCost();

    /**
     * block transactions limit exclude coinBase
     */
    private static final int LIMITED_SIZE = 1024 * 1000 * 1;

    /**
     * contract transactions limit exclude coinBase
     */
    private static final int CONTRACT_LIMITED_SIZE = 1024 * 10 * 1;

    /**
     * limited used gas in a block.
     */
    private static final long BLOCK_GAS_LIMIT = 10_000_000L;

    @Override
    public GasCost getGasCost() {
        return GAS_COST;
    }

    @Override
    public Constants getConstants() {
        return new Constants();
    }

    //Since 160HF
    @Override
    public boolean eip161() {
        return true;
    }

    /**
     * EIP155: https://github.com/ethereum/EIPs/issues/155
     */
    @Override
    public Integer getChainId() {
        return null;
    }

    @Override
    public boolean eip198() {
        return true;
    }

    @Override
    public boolean eip206() {
        return true;
    }

    @Override
    public boolean eip211() {
        return true;
    }

    @Override
    public boolean eip212() {
        return true;
    }

    @Override
    public boolean eip213() {
        return true;
    }

    @Override
    public boolean eip214() {
        return true;
    }

    @Override
    public DataWord getCallGas(OpCode op, DataWord requestedGas, DataWord availableGas) throws Program.OutOfGasException {
        if (requestedGas.compareTo(availableGas) > 0) {
            throw Program.Exception.notEnoughOpGas(op, requestedGas, availableGas);
        }
        return requestedGas.clone();
    }

    /**
     * Calculates available gas to be passed for contract constructor
     * Since EIP150
     *
     * @param availableGas
     */
    @Override
    public DataWord getCreateGas(DataWord availableGas) {
        return null;
    }

    @Override
    public boolean eip658() {
        return true;
    }


}

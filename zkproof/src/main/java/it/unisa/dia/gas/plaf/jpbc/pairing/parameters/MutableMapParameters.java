package it.unisa.dia.gas.plaf.jpbc.pairing.parameters;

/**
 * @author Angelo De Caro (jpbclib@gmail.com)
 * @since 2.0.0
 */
public class MutableMapParameters extends MapParameters implements MutablePairingParameters {

    public MutableMapParameters() {
    }

    public void put(String key,String value){
        throw new IllegalStateException("Not Implemented yet!");
    }

}

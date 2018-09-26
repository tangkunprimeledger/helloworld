package com.higgs.trust.consensus.p2pvalid.example;

import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import lombok.Getter;
import lombok.Setter;

/**
 * @author cwy
 */
@Getter
@Setter
public class StringValidCommand extends ValidCommand<String> {
    private static final long serialVersionUID = -1L;

    public StringValidCommand() {
        super();
    }

    public StringValidCommand(String load) {
        super(load, -1);
    }

    @Override
    public String messageDigest() {
        return get();
    }
}

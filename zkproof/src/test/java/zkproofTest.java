import com.higgs.trust.zkproof.EncryptAmount;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

public class zkproofTest {

    @Test
    public void cipherAddTest() {
        EncryptAmount.initHomomorphicEncryption("BGN",512);
        EncryptAmount amt1 = new EncryptAmount(new BigDecimal("-30.4"),new BigInteger("0"));
        EncryptAmount amt2 = new EncryptAmount(new BigDecimal("30.4"), amt1.getSubRandom());
        EncryptAmount amt3 = amt1.subtract(amt2);
        EncryptAmount amt4 = new EncryptAmount(new BigDecimal("0"), EncryptAmount.FULL_RANDOM.subtract(amt2.getRandom()));

        System.out.println(amt1);
        System.out.println(amt2);
        System.out.println(amt3);
        System.out.println(amt4);

    }

}

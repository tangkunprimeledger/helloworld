import com.higgs.trust.zkproof.EncryptAmount;
import org.junit.Test;

public class zkproofTest {

    @Test
    public void cipherAddTest() {
        EncryptAmount.initHomomorphicEncryption("BGN",512);
        EncryptAmount amt1 = new EncryptAmount("30.4", EncryptAmount.FULL_RANDOM);
        EncryptAmount amt2 = new EncryptAmount("20.3", amt1.getSubRandom());
        EncryptAmount amt3 = amt1.subtract(amt2);
        EncryptAmount amt4 = new EncryptAmount("10.1", EncryptAmount.FULL_RANDOM.subtract(amt2.getRandom()));

        System.out.println(amt1);
        System.out.println(amt2);
        System.out.println(amt3);
        System.out.println(amt4);

    }

}

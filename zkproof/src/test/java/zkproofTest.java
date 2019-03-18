import com.higgs.trust.zkproof.EncryptAmount;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class zkproofTest {

    @Test
    public void cipherAddTest() {
        EncryptAmount.initHomomorphicEncryption("BGN",512);
        String pubKey = "{\"P\":\"26zbXL2Zud9xQhBRCcUwfe2wteD8rfZSAJu4UtLy2KJehKvPr25GU6hkUamTWbMcVufJbfjbMyJJrpNaZyDgNw5T72qGCB3p98PdHryamsU2qxZuoPzTQD9ZgmpdVe9aRwQ6vgdtSf6aKVhyG6fZ5kkz13jewS2HcGjjTbYjfFUyJARpRzUr4v9NvyjamL5h5eQUM6QhsVRULGKVZwrqvFeMcK42dMLNKAgCfnEBx2stKeoybbPUn5NpH3wRfUhEZr6fjXSg97aRsW8VHoMahMbKjdA8txynA8yWcLwEHRbYBauX4cVc4bjxNaLEy4wx5Zkf1Mh3w7y14zBqxzJMJWfwMtJXonyvzvm\",\"Q\":\"22k9HfiJDXX2pvKGSdsp2SAwGwHgDucEQAcgHTQg9tt4bZson3CnWairvhSc3NCU9ZBC5xmED8D1n9ZbjadgGunTaya7W4qFXFAT4icsKvCHGF7oFy71MDUGBSiVXuWt79QpaVv9p4ugFgdqWKgKpyKamgMYBCysVaMDneiXpJzxdf54GspNi7gfvm6ZKjfqdPB6BphRkpBNhub2yfT4hz9bfzZZ3xMg2hvvHXS8rGyZqyRZGe8WLoDRhULmzte314Sqx2NnM87Ywg6C6FqG7zfQ3gQqsMhdqSsjbMM2CWLde4f8GmZVrgKj1N7v25L1nJ5kochN3Wp4tiqKRxW8JAR8mbSHHLQa3wP\",\"key_type\":\"BGN\",\"param\":\"5weGbjV41rQ6NUsMmhdEzKNHPC75SFSpFV2E3oGPx1ufJdxW99zB6MsKJH5XanMK9FsXVHBHEhxVbdj5aCgaZH7RXdrCtJh7abKJdGA95eeSiH6MrFnLHZc9Epr74vTz2tvvgRkHD3bFPHgVHYX6PKNrjdBAaF18FXFCKyRSZQ1rP8ARCHCbbqMds2dJ2QbFebJg2DWC4vhrgV6qHqciS9v9B5TMsvwWZoysckJuAKzKTgVqTf5FNG7T6pvu6y86kKaP4UgbwyU3UrYGMQiaJgbuEMRoQgEEnL3JR1HfyirCcXGtfRjGEKauFJnpppJh3rXkZGT8sPcQM4bNVAvKEXEVRD4nbE1nrChf98ktpLjLVuxfXfgiB9mBYZqHMcN8zMzNBBaiuvynLNTFcEN2JGbHK8nocRkQNPBzNiwA75nWn7Zkzkc1qfAxzPdt5kwnnCRrUeVMtmwWE1n38o9AZZws3C78sjWQAhCyUSYTEjANizwBTVEHWJjS5UN2p7ynWUsaFdwkNqbABB5JThKjE3JqPx4oMYSnaQTqWrzf8N19zHV4oBASc5BcYzmbAuAPs4bLFxf5Nne68mfMTGTsG8S1kgkjdnifmmyonrFBnHM9Auks4BYZmGheFGiyPm3HFwKKU3YmqfbSw9FPGQQSzCrnvkCeGyrMeNKXrJRU8CtD2phCzYtzUFpoahTUgaPE6Tw5VRjWUPPyFJbQ1RN1DjMxDCga3DFCWuSR6VtZ9AyWkX6VjoKFVz5AtGb7geQ3LcSkATqSJ9Nj9FPT2KG2B62CKaaQ4t5tRdxwGnZMKH5yGoG8CQq8xC2HPt3bR347jaDHG9yki4xCoxVVBgAEiZKNuXZxwWmFgVUz3zoJUPEHSH6itid5JX2Z3nqSN3cm7ajrqob9oacRSFCqsCK8j2rHg4XQZiaPFWeVci87Eykwe1g5VgebAiyAjcFtTAkd34oEwoffqnuPMT6aFKV13brhMXCXtsG95LSwg6Qs97PMPhH\",\"n\":\"1NgMy1gmLmRUdLHF1m4Q7uNSRGTPeqsS9ntic8XqA47Lnxafy5fA7GPzCFndnuZ13areXFUqS1T5Q6ij1G8Km4J7s5xE4FcZDkj1JFWx1JrZvjCjUZZUcHXoFdjastM4DSdJPSrNhiQu2fBJtAUK9pJo5iUHo9N83pUm97ckLLYqiGfi\"}";
        //EncryptAmount.setHomomorphicEncryptionKey(pubKey);
        EncryptAmount amt1 = new EncryptAmount(new BigDecimal("100.4"),EncryptAmount.FULL_RANDOM);
        EncryptAmount amt2 = new EncryptAmount(new BigDecimal("30.4"), amt1.getSubRandom());
        EncryptAmount amt3 = amt1.subtract(amt2);
        EncryptAmount amt4 = new EncryptAmount(new BigDecimal("100.4"), EncryptAmount.FULL_RANDOM);

        System.out.println(amt1);
        System.out.println(amt2);
        System.out.println(amt3);
        System.out.println(amt4);
        System.out.println(EncryptAmount.exportPubKey());

    }

    @Test
    public void batchGenPubKey(){

        HashMap<String , String> pubKey = new HashMap<>();
        for (int i = 0; i < 100; i++){

            EncryptAmount.initHomomorphicEncryption("BGN",512);

            pubKey.put(EncryptAmount.exportPubKey(),EncryptAmount.exportPubKey());
        }

        Set<Map.Entry<String,String>> set =  pubKey.entrySet();

        for (Map.Entry<String, String> en: set
             ) {
            System.out.println(en.getKey());
        }

    }

    @Test
    public void SubKey(){
        EncryptAmount.initHomomorphicEncryption("BGN",512);

       String key1 = EncryptAmount.GenSubKey(EncryptAmount.exportFullKey(),1,4);
       String key2 = EncryptAmount.GenSubKey(EncryptAmount.exportFullKey(),2,4);
       String key3 = EncryptAmount.GenSubKey(EncryptAmount.exportFullKey(),3,4);
       String key4 = EncryptAmount.GenSubKey(EncryptAmount.exportFullKey(),4,4);
       String key11 = EncryptAmount.MergeKey(key1,key2);
       String key12 = EncryptAmount.MergeKey(key11,key3);
       key1 = EncryptAmount.MergeKey(key12,key4);
       System.out.println( key1);

       System.out.println("Is key1 contain key2 ? " + EncryptAmount.ContainKey(key11,key12));

       String em1 = EncryptAmount.getHe().Encryption(BigInteger.ONE,BigInteger.ONE);
       BigInteger m1 = EncryptAmount.getHe().Decryption(em1);
       System.out.println(m1);

       EncryptAmount.setHomomorphicEncryptionKey(key1);

       System.out.println(EncryptAmount.exportPubKey());

       EncryptAmount amt1 = new EncryptAmount(new BigDecimal("90000000000.24"),EncryptAmount.FULL_RANDOM);
       EncryptAmount amt2 = new EncryptAmount(new BigDecimal("50.1"), amt1.getSubRandom());
       EncryptAmount amt3 = amt1.subtract(amt2);
       EncryptAmount amt4 = new EncryptAmount(new BigDecimal("90000000000.24").subtract(new BigDecimal("50.1")),EncryptAmount.FULL_RANDOM.subtract(amt2.getRandom()));

       System.out.println(amt1);
       System.out.println(amt2);
       System.out.println(amt3);
       System.out.println(amt4);

       EncryptAmount.initHomomorphicEncryption("Paillier", 512);

       EncryptAmount amt11 = new EncryptAmount(new BigDecimal("124.52"),EncryptAmount.FULL_RANDOM);
       EncryptAmount amt12 = new EncryptAmount(new BigDecimal("50.14"), EncryptAmount.FULL_RANDOM);
       EncryptAmount amt13 = amt11.add(amt12);

       System.out.println(amt11);
       System.out.println(amt12);
       System.out.println(amt13);
       System.out.println(EncryptAmount.Decryption(amt13.toString()));

    //    String em2 = EncryptAmount.getHe().Encryption(BigInteger.ONE,BigInteger.ONE);
    //    BigInteger m2 = EncryptAmount.getHe().Decryption(em2);
    //    System.out.println(m2);



    }

}

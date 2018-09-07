import com.higgs.trust.zkproof.EncryptAmount;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class zkproofTest {

    @Test
    public void cipherAddTest() {
        //EncryptAmount.initHomomorphicEncryption("BGN",512);
        String pubKey = "{\"P\":\"26zbXL2Zud9xQhBRCcUwfe2wteD8rfZSAJu4UtLy2KJehKvPr25GU6hkUamTWbMcVufJbfjbMyJJrpNaZyDgNw5T72qGCB3p98PdHryamsU2qxZuoPzTQD9ZgmpdVe9aRwQ6vgdtSf6aKVhyG6fZ5kkz13jewS2HcGjjTbYjfFUyJARpRzUr4v9NvyjamL5h5eQUM6QhsVRULGKVZwrqvFeMcK42dMLNKAgCfnEBx2stKeoybbPUn5NpH3wRfUhEZr6fjXSg97aRsW8VHoMahMbKjdA8txynA8yWcLwEHRbYBauX4cVc4bjxNaLEy4wx5Zkf1Mh3w7y14zBqxzJMJWfwMtJXonyvzvm\",\"Q\":\"22k9HfiJDXX2pvKGSdsp2SAwGwHgDucEQAcgHTQg9tt4bZson3CnWairvhSc3NCU9ZBC5xmED8D1n9ZbjadgGunTaya7W4qFXFAT4icsKvCHGF7oFy71MDUGBSiVXuWt79QpaVv9p4ugFgdqWKgKpyKamgMYBCysVaMDneiXpJzxdf54GspNi7gfvm6ZKjfqdPB6BphRkpBNhub2yfT4hz9bfzZZ3xMg2hvvHXS8rGyZqyRZGe8WLoDRhULmzte314Sqx2NnM87Ywg6C6FqG7zfQ3gQqsMhdqSsjbMM2CWLde4f8GmZVrgKj1N7v25L1nJ5kochN3Wp4tiqKRxW8JAR8mbSHHLQa3wP\",\"key_type\":\"BGN\",\"param\":\"27wfRdp8w7fJyycReKGTjSTaAZRVCAAUUJDxyNMhzn4mvqhfmtt1vn4F7diood6xmTw3NpAaavEUn1AXweYAsMsPn1C9apxm5tjD9fn7pGnBoxkos8RmhTRGuyc9Ax39mennDeWBu9aqpYDoh9quh2GesQbcg2HkgMpfZDChXXJNPbhUzsjinFYxiWSy6yraUGwUyD59VsxP4JJ33v7t1e8wzY4CcQaWjBjjV5YmWJExFVGXinfZ5j55mqkCr4fAvzxjAmS9wcnr9VMskRJ1FELXYTarq9j4BSBswvNEafPKHsqSZedY8obJ7bKyomx1HmBoxeHNnR4X4KvMF4EXa9fz1PrGNfVj62yM2t7QyrYr2hXbS7ZHgmE1WJh4C12GX4xXwpaVkjwWdhpAVUpsq6vwMjqcxkfm5ZbCkzLMA3GGt4fvzr8byHNZNSCxsMBUhZkJNs3m6BbCNgmRhvtDJUBM4t5fRknx8Vtt1G2Vm9JYqVdcnef6pLhpGhtVf2iBi7djJJi6WSQdBuGWDwTPbtu8gskqCGo6xPXiGV9LiCUaCwUByxZfNcRA3JvtbtMmHF5TFHHrVv8Q2SoteWDpUPkAz85k5r3FX5Ex1o581JeToK8qUNcwu6pFtbV5P7yBWUgwh1UoJuKQvQUGDc1VUDtjMssjbuA49UHfLDxQZvcj6kJ9mBfhHqEeLNX73H4AzENR7hJkUYHeeiswX5SeFL56SS4kUYA7pk1cR323m6McyfDD5zpjKLSRCtrGJPqnPAMVS3FPKtJUrsT7k5iPvB2Mz3H9ztZZaQb56b45kA1jRPCb37Q1QMUoHRiN5YYHPhHeUiAR29drA9o4MErdK9PrEA9prKDu46NxvGjCb5GSsgxDuP758dF5NTmtNata899WaZeoXSrkxhqyPJW8x7eNUw7VmXfETax1ne3c7C34TMw1iYfccC7CTXkGCuF4BYBHybLM4F8VWmvPi2rzWFwP1xLLK5FBfbQfVAJD7dMACP\",\"n\":\"1EyhB6AWZ7DFfdKv6yQoRZNDmehNRx4aUixGA2PZnu43bEBD76Xxorvm2aDAskmBr5zz3V4xccZsc1GWtMRo8UnefAgNUNQtmLFcjXboVmAsWNaMxhFfufivvXRpDPNnLEbNtUC66YPz52rKtcJd9zxdf2G1mYaeKrTcubdYL8VSgEtU\"}";
        EncryptAmount.setHomomorphicEncryptionKey(pubKey);
        EncryptAmount amt1 = new EncryptAmount(new BigDecimal("100.4"),EncryptAmount.FULL_RANDOM);
        EncryptAmount amt2 = new EncryptAmount(new BigDecimal("30.4"), amt1.getSubRandom());
        EncryptAmount amt3 = amt1.subtract(amt2);
        EncryptAmount amt4 = new EncryptAmount(new BigDecimal("0"), EncryptAmount.FULL_RANDOM.subtract(amt2.getRandom()));

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

}

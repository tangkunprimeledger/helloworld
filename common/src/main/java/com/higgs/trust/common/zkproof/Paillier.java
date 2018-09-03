package com.higgs.trust.common.zkproof; /**
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */


import com.alibaba.fastjson.JSONObject;

import java.math.*;
import java.util.*;


/**
 * com.higgs.trust.zkproof.Paillier Cryptosystem <br>
 * <br>
 * References: <br>
 * [1] Pascal com.higgs.trust.zkproof.Paillier,
 * "Public-Key Cryptosystems Based on Composite Degree Residuosity Classes,"
 * EUROCRYPT'99. URL:
 * <a href="http://www.gemplus.com/smart/rd/publications/pdf/Pai99pai.pdf">http:
 * //www.gemplus.com/smart/rd/publications/pdf/Pai99pai.pdf</a><br>
 *
 * [2] com.higgs.trust.zkproof.Paillier cryptosystem from Wikipedia. URL:
 * <a href="http://en.wikipedia.org/wiki/Paillier_cryptosystem">http://en.
 * wikipedia.org/wiki/Paillier_cryptosystem</a>
 *
 * @author Kun Liu (kunliu1@cs.umbc.edu)
 * @version 1.0
 */
public class Paillier  implements HomomorphicEncryption{

    /**
     * p and q are two large primes. lambda = lcm(p-1, q-1) =
     * (p-1)*(q-1)/gcd(p-1, q-1).
     */
    private BigInteger p, q, lambda;
    /**
     * n = p*q, where p and q are two large primes.
     */
    public BigInteger n;
    /**
     * nsquare = n*n
     */
    public BigInteger nsquare;
    /**
     * a random integer in Z*_{n^2} where gcd (L(g^lambda mod n^2), n) = 1.
     */
    private BigInteger g;
    /**
     * number of bits of modulus
     */
    private int bitLength;

    private String keyStat;



    /**
     * Constructs an instance of the com.higgs.trust.zkproof.Paillier cryptosystem.
     *
     * @param bitLengthVal number of bits of modulus
     * @param certainty    The probability that the new BigInteger represents a prime
     *                     number will exceed (1 - 2^(-certainty)). The execution time of
     *                     this constructor is proportional to the value of this
     *                     parameter.
     */
    public Paillier(int bitLengthVal, int certainty) {
         KeyGeneration(bitLengthVal, certainty);
    }

    /**
     * Constructs an instance of the com.higgs.trust.zkproof.Paillier cryptosystem with 512 bits of
     * modulus and at least 1-2^(-64) certainty of primes generation.
     */
    public Paillier(int bits) {
          KeyGeneration(bits, 512);
    }

    public Paillier(String key){
        JSONObject key_value = JSONObject.parseObject(key);

        if (key_value.getString("n") == null || key_value.getString("g") == null || key_value.getString("bitLength") == null){
            keyStat = KEYSTAT.hasNoKey.getCode();
        }
        else {
            n = Base58.decodeToBigInteger(key_value.getString("n"));
            nsquare = n.multiply(n);
            g = Base58.decodeToBigInteger(key_value.getString("g"));
            bitLength = Base58.decodeToBigInteger(key_value.getString("bitLength")).intValue();

            //pubKey check
            if (n.bitLength() != bitLength
                    || g.compareTo(nsquare) >= 0
                    ) {
                keyStat = KEYSTAT.hasNoKey.getCode();
            } else {
                keyStat = KEYSTAT.hasPubKey.getCode();
            }


            // privateKey check
            if (key_value.getString("lambda") != null) {
                lambda = Base58.decodeToBigInteger(key_value.getString("lambda"));
                if (g.modPow(lambda, nsquare).subtract(BigInteger.ONE).divide(n).gcd(n).intValue() == 1) {
                    keyStat = KEYSTAT.hasFullKey.getCode();
                }
            }
        }
    }

    /**
     * Sets up the public key and private key.
     *
     * @param bitLengthVal number of bits of modulus.
     * @param certainty    The probability that the new BigInteger represents a prime
     *                     number will exceed (1 - 2^(-certainty)). The execution time of
     *                     this constructor is proportional to the value of this
     *                     parameter.
     */
    public void KeyGeneration(int bitLengthVal, int certainty) {
        bitLength = bitLengthVal;
        /*
         * Constructs two randomly generated positive BigIntegers that are
         * probably prime, with the specified bitLength and certainty.
         */
        p = new BigInteger(bitLength / 2, certainty, new Random());
        q = new BigInteger(bitLength / 2, certainty, new Random());

        n = p.multiply(q);
        nsquare = n.multiply(n);

        g = new BigInteger(bitLength / 2, certainty, new Random());
        while (g.subtract(p) == BigInteger.ZERO
                ||g.subtract(q) == BigInteger.ZERO)
        {
            g = new BigInteger(bitLength / 2, certainty, new Random());
        }



        lambda = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE))
                .divide(p.subtract(BigInteger.ONE).gcd(q.subtract(BigInteger.ONE)));

        /* check whether g is good. */
        if (g.modPow(lambda, nsquare).subtract(BigInteger.ONE).divide(n).gcd(n).intValue() != 1) {
            //System.out.println("g is not good. Choose g again.");
            keyStat = KEYSTAT.hasNoKey.getCode();
        }

        keyStat = KEYSTAT.hasFullKey.getCode();
        return;
    }

    public String exportFullKey()
    {
         JSONObject key = new JSONObject();
         key.put("key_type", "Pailler");
         key.put("g",Base58.encode(g.toByteArray()));
         key.put("n",Base58.encode(n.toByteArray()));
         key.put("lambda", Base58.encode(lambda.toByteArray()));
         key.put("bitLength", Base58.encode(new BigInteger(String.valueOf(bitLength)).toByteArray()));
        return key.toJSONString();
    }

    public String exportPubKey()
    {
        JSONObject key = JSONObject.parseObject(exportFullKey());
        key.remove("lambda");
        return key.toJSONString();
    }



    /**
     * Encrypts plaintext m. ciphertext c = g^m * r^n mod n^2. This function
     * explicitly requires random input r to help with encryption.
     *
     * @param m plaintext as a BigInteger
     * @param r random plaintext to help with encryption
     * @return ciphertext as a BigInteger
     */
    public String Encryption(BigInteger m, BigInteger r) {
        return Base58.encode(g.modPow(m, nsquare).multiply(r.modPow(n, nsquare)).mod(nsquare).toByteArray());
    }

    /**
     * Encrypts plaintext m. ciphertext c = g^m * r^n mod n^2. This function
     * automatically generates random input r (to help with encryption).
     *
     * @param m plaintext as a BigInteger
     * @return ciphertext as a BigInteger
     */
    public BigInteger Encryption(BigInteger m) {
        BigInteger r = BigInteger.ONE;
        if (hasPubKey() == true){
            return g.modPow(m, nsquare).multiply(r.modPow(n, nsquare)).mod(nsquare);
        }

        return BigInteger.ZERO;
    }

    /**
     * Decrypts ciphertext c. plaintext m = L(c^lambda mod n^2) * u mod n, where
     * u = (L(g^lambda mod n^2))^(-1) mod n.
     *
     * @param c ciphertext as a BigInteger
     * @return plaintext as a BigInteger
     */
    public BigInteger Decryption(BigInteger c) {
        if (hasFullKey() == true) {
            BigInteger u = g.modPow(lambda, nsquare).subtract(BigInteger.ONE).divide(n).modInverse(n);
            //return c.modPow(lambda, nsquare).subtract(BigInteger.ONE).divide(n).multiply(u).mod(n);
            return c.modPow(lambda, nsquare).subtract(BigInteger.ONE).divide(n).multiply(u).mod(n);
        }

        return  BigInteger.ZERO;
    }

    /**
     * sum of (cipher) em1 and em2
     *
     * @param em1
     * @param em2
     * @return
     */
    public BigInteger cipherAdd(BigInteger em1, BigInteger em2) {
        if (hasPubKey() == true){
            return em1.multiply(em2).mod(nsquare);
        }

        return  BigInteger.ZERO;
    }

    public String cipherAdd(String em1, String em2) {
        if (hasPubKey() == true){
            return Base58.encode(Base58.decodeToBigInteger(em1).multiply(Base58.decodeToBigInteger(em2)).mod(nsquare).toByteArray());
        }

        return  Base58.encode(BigInteger.ZERO.toByteArray());
    }



    public BigInteger Decryption(String str) {
        if(hasFullKey() == true)
        {
            BigInteger c = Base58.decodeToBigInteger(str);
            BigInteger u = g.modPow(lambda, nsquare).subtract(BigInteger.ONE).divide(n).modInverse(n);
            return c.modPow(lambda, nsquare).subtract(BigInteger.ONE).divide(n).multiply(u).mod(n);
        }

        return BigInteger.ZERO;
    }

    public String Encryption(String str_value) {
        BigInteger m = new BigInteger(str_value);
        BigInteger r = BigInteger.ONE;
        if (hasPubKey() == true){
            return Base58.encode(g.modPow(m, nsquare).multiply(r.modPow(n, nsquare)).mod(nsquare).toByteArray());
        }
        return Base58.encode(BigInteger.ZERO.toByteArray());
    }


    public boolean Compare(String em1, String em2)
    {
        if(hasFullKey() == true)
        {
            BigInteger m1 = Decryption(em1);
            BigInteger m2 = Decryption(em2);
            if (m1.subtract(m2) == BigInteger.ZERO)
            {
                return true;
            }
        }
        else
        {
            if (em1.compareTo(em2) == 0)
            {
                return true;
            }
        }
        return false;
    }

    public int getBitLength() {
        return bitLength;
    }

    public boolean hasPubKey() {
        if (keyStat == KEYSTAT.hasPubKey.getCode() || keyStat == KEYSTAT.hasFullKey.getCode()) {
            return true;
        }
        else{
            return false;
        }
    }

    public boolean hasFullKey() {
        if (keyStat == KEYSTAT.hasFullKey.getCode()) {
            return true;
        }
        else{
            return false;
        }
    }

    public boolean tooBig(BigInteger b) {
        if (b.compareTo(nsquare) >= 0){
            return true;
        }
        return false;
    }

    public boolean tooBigRandom(BigInteger r) {
        return false;
    }
}

/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package com.higgs.trust.evmcontract.util;

import com.higgs.trust.evmcontract.vm.DataWord;
import org.spongycastle.util.encoders.Hex;

import javax.swing.*;
import java.math.BigInteger;
import java.net.URL;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;

public class Utils {
    private static final DataWord DIVISOR = new DataWord(64);

    private static SecureRandom random = new SecureRandom();

    /**
     * @param number should be in form '0x34fabd34....'
     * @return String
     */
    public static BigInteger unifiedNumericToBigInteger(String number) {

        boolean match = Pattern.matches("0[xX][0-9a-fA-F]+", number);
        if (!match) {
            return (new BigInteger(number));
        } else {
            number = number.substring(2);
            number = number.length() % 2 != 0 ? "0".concat(number) : number;
            byte[] numberBytes = Hex.decode(number);
            return (new BigInteger(1, numberBytes));
        }
    }

    /**
     * Return formatted Date String: yyyy.MM.dd HH:mm:ss
     * Based on Unix's time() input in seconds
     *
     * @param timestamp seconds since start of Unix-time
     * @return String formatted as - yyyy.MM.dd HH:mm:ss
     */
    public static String longToDateTime(long timestamp) {
        Date date = new Date(timestamp * 1000);
        DateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        return formatter.format(date);
    }


    public static ImageIcon getImageIcon(String resource) {
        URL imageURL = ClassLoader.getSystemResource(resource);
        ImageIcon image = new ImageIcon(imageURL);
        return image;
    }

    static BigInteger _1000_ = new BigInteger("1000");

    public static String getValueShortString(BigInteger number) {
        BigInteger result = number;
        int pow = 0;
        while (result.compareTo(_1000_) == 1 || result.compareTo(_1000_) == 0) {
            result = result.divide(_1000_);
            pow += 3;
        }
        return result.toString() + "\u00b7(" + "10^" + pow + ")";
    }


    public static boolean isValidAddress(byte[] addr) {
        return addr != null && addr.length == 20;
    }


    public static SecureRandom getRandom() {
        return random;
    }

    public static double JAVA_VERSION = getJavaVersion();

    static double getJavaVersion() {
        String version = System.getProperty("java.version");

        // on android this property equals to 0
        if (version.equals("0")) {
            return 0;
        }

        int pos = 0, count = 0;
        for (; pos < version.length() && count < 2; pos++) {
            if (version.charAt(pos) == '.') {
                count++;
            }
        }
        return Double.parseDouble(version.substring(0, pos - 1));
    }


    public static String align(String s, char fillChar, int targetLen, boolean alignRight) {
        if (targetLen <= s.length()) {
            return s;
        }
        String alignString = repeat("" + fillChar, targetLen - s.length());
        return alignRight ? alignString + s : s + alignString;

    }

    public static String repeat(String s, int n) {
        if (s.length() == 1) {
            byte[] bb = new byte[n];
            Arrays.fill(bb, s.getBytes()[0]);
            return new String(bb);
        } else {
            StringBuilder ret = new StringBuilder();
            for (int i = 0; i < n; i++) {
                ret.append(s);
            }
            return ret.toString();
        }
    }


}
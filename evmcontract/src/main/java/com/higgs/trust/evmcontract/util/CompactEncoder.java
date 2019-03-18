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

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.higgs.trust.evmcontract.util.ByteUtil.appendByte;
import static java.util.Arrays.copyOf;
import static java.util.Arrays.copyOfRange;
import static org.spongycastle.util.Arrays.concatenate;
import static org.spongycastle.util.encoders.Hex.encode;

/**
 * Compact encoding of hex sequence with optional terminator
 * <p>
 * The traditional compact way of encoding a hex string is to convert it into binary
 * - that is, a string like 0f1248 would become three bytes 15, 18, 72. However,
 * this approach has one slight problem: what if the length of the hex string is odd?
 * In that case, there is no way to distinguish between, say, 0f1248 and f1248.
 * <p>
 * Additionally, our application in the Merkle Patricia tree requires the additional feature
 * that a hex string can also have a special "terminator symbol" at the end (denoted by the 'T').
 * A terminator symbol can occur only once, and only at the end.
 * <p>
 * An alternative way of thinking about this to not think of there being a terminator symbol,
 * but instead treat bit specifying the existence of the terminator symbol as a bit specifying
 * that the given node encodes a final node, where the value is an actual value, rather than
 * the hash of yet another node.
 * <p>
 * To solve both of these issues, we force the first nibble of the final byte-stream to encode
 * two flags, specifying oddness of length (ignoring the 'T' symbol) and terminator status;
 * these are placed, respectively, into the two lowest significant bits of the first nibble.
 * In the case of an even-length hex string, we must introduce a second nibble (of value zero)
 * to ensure the hex-string is even in length and thus is representable by a whole number of bytes.
 * <p>
 * Examples:
 * &gt; [ 1, 2, 3, 4, 5 ]
 * '\x11\x23\x45'
 * &gt; [ 0, 1, 2, 3, 4, 5 ]
 * '\x00\x01\x23\x45'
 * &gt; [ 0, 15, 1, 12, 11, 8, T ]
 * '\x20\x0f\x1c\xb8'
 * &gt; [ 15, 1, 12, 11, 8, T ]
 * '\x3f\x1c\xb8'
 */
public class CompactEncoder {

    private final static byte TERMINATOR = 16;
    private final static Map<Character, Byte> HEX_MAP = new HashMap<>();

    static {
        HEX_MAP.put('0', (byte) 0x0);
        HEX_MAP.put('1', (byte) 0x1);
        HEX_MAP.put('2', (byte) 0x2);
        HEX_MAP.put('3', (byte) 0x3);
        HEX_MAP.put('4', (byte) 0x4);
        HEX_MAP.put('5', (byte) 0x5);
        HEX_MAP.put('6', (byte) 0x6);
        HEX_MAP.put('7', (byte) 0x7);
        HEX_MAP.put('8', (byte) 0x8);
        HEX_MAP.put('9', (byte) 0x9);
        HEX_MAP.put('a', (byte) 0xa);
        HEX_MAP.put('b', (byte) 0xb);
        HEX_MAP.put('c', (byte) 0xc);
        HEX_MAP.put('d', (byte) 0xd);
        HEX_MAP.put('e', (byte) 0xe);
        HEX_MAP.put('f', (byte) 0xf);
    }

    /**
     * Pack nibbles to binary
     *
     * @param nibbles sequence. may have a terminator
     * @return hex-encoded byte array
     */
    public static byte[] packNibbles(byte[] nibbles) {
        int terminator = 0;

        if (nibbles[nibbles.length - 1] == TERMINATOR) {
            terminator = 1;
            nibbles = copyOf(nibbles, nibbles.length - 1);
        }
        int oddlen = nibbles.length % 2;
        int flag = 2 * terminator + oddlen;
        if (oddlen != 0) {
            byte[] flags = new byte[]{(byte) flag};
            nibbles = concatenate(flags, nibbles);
        } else {
            byte[] flags = new byte[]{(byte) flag, 0};
            nibbles = concatenate(flags, nibbles);
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (int i = 0; i < nibbles.length; i += 2) {
            buffer.write(16 * nibbles[i] + nibbles[i + 1]);
        }
        return buffer.toByteArray();
    }

    public static boolean hasTerminator(byte[] packedKey) {
        return ((packedKey[0] >> 4) & 2) != 0;
    }

    /**
     * Unpack a binary string to its nibbles equivalent
     *
     * @param str of binary data
     * @return array of nibbles in byte-format
     */
    public static byte[] unpackToNibbles(byte[] str) {
        byte[] base = binToNibbles(str);
        base = copyOf(base, base.length - 1);
        if (base[0] >= 2) {
            base = appendByte(base, TERMINATOR);
        }
        if (base[0] % 2 == 1) {
            base = copyOfRange(base, 1, base.length);
        } else {
            base = copyOfRange(base, 2, base.length);
        }
        return base;
    }

    /**
     * Transforms a binary array to hexadecimal format + terminator
     *
     * @param str byte[]
     * @return array with each individual nibble adding a terminator at the end
     */
    public static byte[] binToNibbles(byte[] str) {

        byte[] hexEncoded = encode(str);
        byte[] hexEncodedTerminated = Arrays.copyOf(hexEncoded, hexEncoded.length + 1);

        for (int i = 0; i < hexEncoded.length; ++i) {
            byte b = hexEncodedTerminated[i];
            hexEncodedTerminated[i] = HEX_MAP.get((char) b);
        }

        hexEncodedTerminated[hexEncodedTerminated.length - 1] = TERMINATOR;
        return hexEncodedTerminated;
    }


    public static byte[] binToNibblesNoTerminator(byte[] str) {

        byte[] hexEncoded = encode(str);

        for (int i = 0; i < hexEncoded.length; ++i) {
            byte b = hexEncoded[i];
            hexEncoded[i] = HEX_MAP.get((char) b);
        }

        return hexEncoded;
    }
}

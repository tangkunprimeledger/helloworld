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
package com.higgs.trust.evmcontract.core;


import com.higgs.trust.evmcontract.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * The block in Ethereum is the collection of relevant pieces of information
 * (known as the blockheader), H, together with information corresponding to
 * the comprised transactions, R, and a set of other blockheaders U that are known
 * to have a parent equal to the present block’s parent’s parent
 * (such blocks are known as uncles).
 *
 * @author Roman Mandeleil
 * @author Nick Savers
 * @since 20.05.2014
 */
public class Block {

    private static final Logger logger = LoggerFactory.getLogger("blockchain");


    /* Transactions */
    private List<Transaction> transactionsList = new CopyOnWriteArrayList<>();



    /* Private */

    private byte[] rlpEncoded;
    private boolean parsed = false;

    /* Constructors */

    public Block() {
    }

    public Block(byte[] rawData) {
        logger.debug("new from [" + ByteUtil.toHexString(rawData) + "]");
        this.rlpEncoded = rawData;
    }


    public byte[] getHash() {
        return "ox12".getBytes();
    }

    public byte[] getParentHash() {
        return "ox11".getBytes();
    }


    public byte[] getCoinbase() {
        return "ox14".getBytes();
    }

    public long getTimestamp() {
        return System.currentTimeMillis();
    }

    public long getNumber() {
        return 2L;
    }

    public byte[] getGasLimit() {
        return "90".getBytes();
    }

    public byte[] getDifficulty() {
        return "90".getBytes();
    }

}

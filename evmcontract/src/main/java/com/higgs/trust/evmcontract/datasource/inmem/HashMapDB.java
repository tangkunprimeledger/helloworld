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
package com.higgs.trust.evmcontract.datasource.inmem;

import com.higgs.trust.evmcontract.datasource.DbSettings;
import com.higgs.trust.evmcontract.datasource.DbSource;
import com.higgs.trust.evmcontract.util.ALock;
import com.higgs.trust.evmcontract.util.ByteArrayMap;
import com.higgs.trust.evmcontract.util.FastByteComparisons;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Anton Nashatyrev on 12.10.2016.
 */
public class HashMapDB<V> implements DbSource<V> {

    protected final Map<byte[], V> storage;

    protected ReadWriteLock rwLock = new ReentrantReadWriteLock();
    protected ALock readLock = new ALock(rwLock.readLock());
    protected ALock writeLock = new ALock(rwLock.writeLock());

    public HashMapDB() {
        this(new ByteArrayMap<V>());
    }

    public HashMapDB(ByteArrayMap<V> storage) {
        this.storage = storage;
    }

    @Override
    public void put(byte[] key, V val) {
        if (val == null) {
            delete(key);
        } else {
            try (ALock l = writeLock.lock()) {
                //System.out.println("key:+\t" + Hex.toHexString(key) + " value:\t" + Hex.toHexString((byte[]) val));
                storage.put(key, val);
            }
        }
//        System.out.println("storage size:{}" + storage.size());
//        storage.entrySet().forEach(item -> {
//            System.out.println("put key:" + Hex.toHexString(item.getKey()) + ":\t" + Hex.toHexString((byte[]) item.getValue()));
//        });
    }

    @Override
    public V get(byte[] key) {
        //System.out.println("get key:+\t" + Hex.toHexString(key));
        try (ALock l = readLock.lock()) {
            return storage.get(key);
        }
    }

    @Override
    public void delete(byte[] key) {
        try (ALock l = writeLock.lock()) {
            storage.remove(key);
        }
    }

    @Override
    public boolean flush() {
        return true;
    }

    @Override
    public String getName() {
        return "in-memory";
    }

    @Override
    public void setName(String name) {
    }

    @Override
    public void init() {
    }

    @Override
    public void init(DbSettings settings) {
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public void close() {
    }

    @Override
    public Set<byte[]> keys() {
        try (ALock l = readLock.lock()) {
            return getStorage().keySet();
        }
    }

    @Override
    public void reset() {
        try (ALock l = writeLock.lock()) {
            storage.clear();
        }
    }

    @Override
    public V prefixLookup(byte[] key, int prefixBytes) {
        try (ALock l = readLock.lock()) {
            for (Map.Entry<byte[], V> e : storage.entrySet()) {
                if (FastByteComparisons.compareTo(key, 0, prefixBytes, e.getKey(), 0, prefixBytes) == 0) {
                    return e.getValue();
                }
            }

            return null;
        }
    }

    @Override
    public void updateBatch(Map<byte[], V> rows) {
        try (ALock l = writeLock.lock()) {
            for (Map.Entry<byte[], V> entry : rows.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    public Map<byte[], V> getStorage() {
        return storage;
    }
}

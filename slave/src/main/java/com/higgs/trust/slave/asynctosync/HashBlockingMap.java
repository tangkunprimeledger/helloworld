package com.higgs.trust.slave.asynctosync;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @param <V>
 * @author tangfashuang
 */
public class HashBlockingMap<V> implements BlockingMap<V> {

    private ConcurrentLinkedHashMap<String, Item<V>> map;

    private final ReentrantLock lock = new ReentrantLock();

    public HashBlockingMap(int maxSize) {
        map = new ConcurrentLinkedHashMap.Builder<String, Item<V>>().maximumWeightedCapacity(maxSize).build();
    }

    @Override public void put(String key, V o) throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            if (map.containsKey(key)) {
                Item<V> item = map.get(key);
                item.put(o);
            } else {
                Item<V> item = new Item<V>();
                map.put(key, item);
                item.put(o);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override public V take(String key) throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            if (!map.containsKey(key)) {
                map.put(key, new Item<V>());
            }
        } finally {
            lock.unlock();
        }

        Item<V> item = map.get(key);
        V x = item.take();
        map.remove(key);

        return x;
    }

    @Override public V poll(String key, long timeout) throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            if (!map.containsKey(key)) {
                map.put(key, new Item<V>());
            }
        } finally {
            lock.unlock();
        }

        Item<V> item = map.get(key);
        V x = item.poll(timeout);
        map.remove(key);

        return x;
    }

    private static class Item<E> {

        private final ReentrantLock lock = new ReentrantLock();

        private final Condition cond = lock.newCondition();

        private E obj = null;

        private void put(E o) throws InterruptedException {
            if (o == null) {
                throw new NullPointerException();
            }
            final ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            try {
                obj = o;
                cond.signal();
            } finally {
                lock.unlock();
            }
        }

        private E take() throws InterruptedException {
            E x;
            final ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            try {
                try {
                    while (obj == null) {
                        cond.await();
                    }
                } catch (InterruptedException ie) {
                    cond.signal();
                    throw ie;
                }
                x = obj;
            } finally {
                lock.unlock();
            }
            return x;
        }

        private E poll(long timeout) throws InterruptedException {
            timeout = TimeUnit.MILLISECONDS.toNanos(timeout);
            E x;
            final ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            try {
                for (; ; ) {
                    if (obj != null) {
                        x = obj;
                        break;
                    }
                    if (timeout <= 0) {
                        return null;
                    }
                    try {
                        timeout = cond.awaitNanos(timeout);
                    } catch (InterruptedException ie) {
                        cond.signal();
                        throw ie;
                    }
                }
            } finally {
                lock.unlock();
            }
            return x;
        }
    }
}

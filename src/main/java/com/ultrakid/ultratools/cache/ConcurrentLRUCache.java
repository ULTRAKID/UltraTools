package com.ultrakid.ultratools.cache;

import java.security.InvalidParameterException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 基于LRU策略的缓存map
 *
 * @author ultrakid
 * @version 1.0
 * @date 2021/05/07 14:53
 */
public class ConcurrentLRUCache<K, V> extends LinkedHashMap<K, V> {
    private int maxSize;  //最大大小
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(); //读写锁
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    public ConcurrentLRUCache(int initLength) {
        this(initLength, Integer.MAX_VALUE >> 1);
    }

    public ConcurrentLRUCache(int initLength, int maxSize) {
        super(initLength, 0.75f, true);
        if (maxSize <= 0) {
            throw new InvalidParameterException("ConcurrentLRUCache max size should be greater than 0, " +
                    "but actual " + maxSize);
        }
        this.maxSize = maxSize;
    }

    /**
     * 添加列表，key值根据给定的函数进行计算
     *
     * @param valueList   待添加数据的列表
     * @param calcKeyFunc key值计算的函数
     */
    public void putList(List<V> valueList, Function<V, K> calcKeyFunc) {
        if (valueList == null || valueList.isEmpty()) {
            return;
        }
        writeLock.lock();
        try {
            valueList.forEach(value -> {
                K key = calcKeyFunc.apply(value);
                super.put(key, value);
            });
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean containsValue(Object value) {
        readLock.lock();
        try {
            return super.containsValue(value);
        } finally {
            readLock.unlock();
        }

    }

    @Override
    public V get(Object key) {
        readLock.lock();
        try {
            return super.get(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        readLock.lock();
        try {
            return super.getOrDefault(key, defaultValue);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            super.clear();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 只加了读锁，多线程如果有写操作的请慎用
     *
     * @param action 待执行动作
     */
    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        readLock.lock();
        try {
            super.forEach(action);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        writeLock.lock();
        try {
            super.replaceAll(function);
        } finally {
            writeLock.unlock();
        }
    }


    @Override
    public boolean containsKey(Object key) {
        readLock.lock();
        try {
            return super.containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        writeLock.lock();
        try {
            return super.put(key, value);
        } finally {
            writeLock.unlock();
        }

    }


    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        writeLock.lock();
        try {
            super.putAll(m);
        } finally {
            writeLock.unlock();
        }
    }


    @Override
    public V remove(Object key) {
        writeLock.lock();
        try {
            return super.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V putIfAbsent(K key, V value) {
        writeLock.lock();
        try {
            return super.putIfAbsent(key, value);
        } finally {
            writeLock.lock();
        }
    }

    @Override
    public boolean remove(Object key, Object value) {
        writeLock.lock();
        try {
            return super.remove(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        writeLock.lock();
        try {
            return super.replace(key, oldValue, newValue);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V replace(K key, V value) {
        writeLock.lock();
        try {
            return super.replace(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        writeLock.lock();
        try {
            return super.computeIfAbsent(key, mappingFunction);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        writeLock.lock();
        try {
            return super.computeIfPresent(key, remappingFunction);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        writeLock.lock();
        try {
            return super.compute(key, remappingFunction);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        writeLock.lock();
        try {
            return super.merge(key, value, remappingFunction);
        } finally {
            writeLock.unlock();
        }

    }

    /**
     * 决定何时移除最久远的元素
     * 这里的策略是超过最大大小时移除
     *
     * @param eldest 最久远的元素
     * @return true表示需要移除
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
}

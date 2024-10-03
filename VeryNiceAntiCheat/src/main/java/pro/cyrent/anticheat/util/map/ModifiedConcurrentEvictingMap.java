package pro.cyrent.anticheat.util.map;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ModifiedConcurrentEvictingMap<K, V> {
    private static final Object PRESENT = new Object();

    private final Cache<K, Object> cache;

    public ModifiedConcurrentEvictingMap(int max) {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(max)
                .build();
    }

    public boolean remove(Object key, Object value) {
        return cache.asMap().remove(key, PRESENT);
    }

    public V putIfAbsent(K key, V value) {
        return cache.asMap().putIfAbsent(key, PRESENT) == null ? null : value;
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    public V put(K key, V value) {
        cache.put(key, PRESENT);
        return value;
    }

    public void clear() {
        cache.invalidateAll();
    }

    public V remove(Object key) {
        return cache.asMap().remove(key) == null ? null : null;
    }

    public int size() {
        return (int) cache.size();
    }

    public boolean containsKey(Object key) {
        return cache.asMap().containsKey(key);
    }

    public V get(Object key) {
        return cache.asMap().containsKey(key) ? null : null;
    }

    public void removeFromCache(K key) {
        cache.invalidate(key);
    }

    public void removeFromCacheAll(Iterable<K> keys) {
        cache.invalidateAll(keys);
    }
}
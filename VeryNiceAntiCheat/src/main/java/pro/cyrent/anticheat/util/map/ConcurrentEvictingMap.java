package pro.cyrent.anticheat.util.map;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConcurrentEvictingMap<K, V> extends ConcurrentHashMap<K, V> {
    private static final Object PRESENT = new Object();

    private final Cache<K, V> cache;
    private final ConcurrentMap<K, V> map;

    public ConcurrentEvictingMap(int max) {

        this.cache = (Cache<K, V>) CacheBuilder.newBuilder()
                .maximumSize(max)
                .build(new CacheLoader<Object, Object>() {
                    @SuppressWarnings("NullableProblems")
                    public Object load(Object o) {
                        return PRESENT;
                    }
                });

        this.map = this.cache.asMap();
    }

    @Override
    public boolean remove(Object key, Object value) {
        return this.map.remove(key, value);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return this.map.putIfAbsent(key, value);
    }

    @Override
    public V put(K key, V value) {
        return this.map.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public V remove(Object key) {
        return this.map.remove(key);
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public V get(Object key) {
        return this.map.get(key);
    }
}
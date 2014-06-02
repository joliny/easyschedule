package com.samueli.easyschedule.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class CacheHolder {

    private static LRUCache<Key, Object> cache = new LRUCache<CacheHolder.Key, Object>(50);

    public static Object get(Key key) {
        return cache.get(key);
    }

    public static void set(Key key, Object obj) {
        cache.put(key, obj);
    }

    public enum Key {
        DISTRIBUTE_RESULT
    }

    static class LRUCache<K, V> extends LinkedHashMap<K, V> {

        private static final long serialVersionUID = 1L;
        private final int         maxSize;

        public LRUCache(int maxSize){
            this(maxSize, 16, 0.75f, false);
        }

        public LRUCache(int maxSize, int initialCapacity, float loadFactor, boolean accessOrder){
            super(initialCapacity, loadFactor, accessOrder);
            this.maxSize = maxSize;
        }

        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return this.size() > this.maxSize;
        }

    }

}

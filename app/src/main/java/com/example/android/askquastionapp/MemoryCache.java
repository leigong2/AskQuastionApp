package com.example.android.askquastionapp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by yibin on 2017-01-17.
 */

public class MemoryCache {
    private static MemoryCache INSTANCE = null;
    private Map<String, Object> map = new HashMap<>();

    public static MemoryCache getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new MemoryCache();
        }
        return INSTANCE;
    }

    private MemoryCache() {
    }

    public <T> void put(T t) {
        if (t != null) {
            map.put(t.getClass().getName(), t);
        }
    }

    public <T> T get(Class<T> cls) {
        return (T) map.get(cls.getName());
    }

    public <T> T clear(Class<T> cls) {
        T remove = (T) map.remove(cls.getName());
        return remove;
    }

    public void put(String key, Object obj) {
        map.put(key, obj);
    }

    public <T> T get(String key) {
        return (T) map.get(key);
    }

    public <T> T remove(String key) {
        return (T)map.remove(key);
    }

    public void removeByStartKey(String key) {
        Iterator iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String k = (String) iterator.next();
            if (k.startsWith(key)) {
                iterator.remove();
                map.remove(k);
            }
        }
    }

}

package com.logicalpractice.phash;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
public class NonLockingHashMap<K,V> extends AbstractMap<K,V> {

    private final AtomicReference<Hash<K,V>> reference = new AtomicReference<>(Hash.<K,V>emptyHash());

    @Override
    public Set<Entry<K, V>> entrySet() {
        return reference.get().entrySet();
    }

    @Override
    public V put(K key, V value) {

        Hash<K, V> current;
        do {
            current = reference.get();
        } while ( ! reference.compareAndSet(current, current.with(key, value)));

        return current.get(key); // will be the old reference
    }

    @Override
    public V get(Object key) {
        return reference.get().get(key);
    }
}

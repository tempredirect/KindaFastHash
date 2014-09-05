package com.logicalpractice.kindafasthash;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
public class CopyOnWriteHashMap<K,V> extends AbstractMap<K,V> {

    private final AtomicReference<Hash<K,V>> reference = new AtomicReference<>(Hash.<K,V>emptyHash());

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new HashEntrySetView(reference.get());
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

    @Override
    public boolean containsKey(Object key) {
        return key != null && reference.get().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        if (value == null) return false; // null aren't allowed as values either
        return super.containsValue(value);
    }

    @Override
    public V remove(Object key) {
        if (key == null) return null; //
        Hash<K, V> current;
        do {
            current = reference.get();
        } while ( ! reference.compareAndSet(current, current.without(key)));

        return current.get(key);
    }

    /**
     * @see java.util.concurrent.ConcurrentMap#remove(Object, Object)
     */
    public boolean remove(Object key, Object value) {
        Hash<K, V> current;
        check(key, value);
        do {
            current = reference.get();
            Object mapValue = current.get(key);
            if (!Objects.equals(value, mapValue))
                return false;
        } while ( ! reference.compareAndSet(current, current.without(key)));
        return true; //only gets here if a removal has happened
    }

    private void check(Object key, Object value) {
        checkKey(key);
        checkValue(value);
    }

    private void checkValue(Object value) {
        if (value == null) {
            throw new NullPointerException("null values are not allowed");
        }
    }

    private void checkKey(Object key) {
        if (key == null) {
            throw new NullPointerException("null keys are not allowed");
        }
    }


    @Override
    public void clear() {
        reference.set(Hash.<K,V>emptyHash());
    }

    private class HashEntrySetView extends AbstractSet<Entry<K, V>> {
        private Hash<K,V> hash;

        private HashEntrySetView(Hash<K, V> hash) {
            this.hash = hash;
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new Iterator<Entry<K, V>>() {
                private Iterator<Entry<K,V>> delegate = hash.iterator();
                private Entry<K,V> last;

                @Override
                public boolean hasNext() {
                    return delegate.hasNext();
                }

                @Override
                public Entry<K, V> next() {
                    return last = delegate.next();
                }

                @Override
                public void remove() {
                    if (last == null)
                        throw new IllegalStateException("remove() cannot be called before next() has been");
                    HashEntrySetView.this.remove(last);
                    last = null;
                }
            };
        }

        @Override
        public int size() {
            return hash.size();
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean remove(Object o) {
            if (o instanceof Entry) {
                Entry<K,V> entry = (Entry<K, V>) o;
                if (Objects.equals(hash.get(entry.getKey()), entry.getValue())){
                    // if the entry is in the current Hash then
                    Hash<K, V> hashWithout = hash.without(entry.getKey());
                    if (!reference.compareAndSet(hash, hashWithout)) {
                        throw new ConcurrentModificationException();
                    }
                    this.hash = hashWithout;
                    return true;
                }
            }
            return false;
        }


        @Override
        public void clear() {
            if (!reference.compareAndSet(hash, Hash.<K,V>emptyHash())) {
                throw new ConcurrentModificationException();
            }
            hash = Hash.emptyHash();
        }
    }
}

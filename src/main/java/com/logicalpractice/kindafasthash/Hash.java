package com.logicalpractice.kindafasthash;

import com.google.common.collect.AbstractIterator;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Simple implementation of a persistent Hash.
 */
public class Hash<K, V> implements Iterable<Map.Entry<K, V>> {
    private static int INITIAL_TABLE_SIZE = 16; // must be power of 2 or indexFor will assumes this
    private static EntryNode[] EMPTY_TABLE = new EntryNode[INITIAL_TABLE_SIZE];
    private static Hash EMPTY_HASH = new Hash(EMPTY_TABLE, 0);

    private static final float LOAD_FACTOR = 0.75f;

    private final EntryNode<K, V>[] entries;
    private final int size;
    private final int threshold;

    private Hash(EntryNode<K, V>[] entries, int size) {
        this.entries = entries;
        this.size = size;
        this.threshold = (int) (entries.length * LOAD_FACTOR);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Hash<K, V> emptyHash() {
        return EMPTY_HASH;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    private static class EntryNode<K, V> implements Map.Entry<K, V> {
        final K key;
        final V value;
        final int hash;
        final EntryNode<K, V> next;

        private EntryNode(K key, V value, int hash, EntryNode<K, V> next) {
            this.key = key;
            this.value = value;
            this.hash = hash;
            this.next = next;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException("Updates are not supported");
        }

        @Override
        public boolean equals(Object o) {
            // from the spec of Map.Entry
            if (this == o) return true;
            if (!(o instanceof Map.Entry)) return false;

            Map.Entry mapEntry = (Map.Entry) o;
            return key.equals(mapEntry.getKey()) && value.equals(mapEntry.getValue());
        }

        @Override
        public int hashCode() {
            // from the spec of Map.Entry
            return (key.hashCode()) ^ (value.hashCode());
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }

    public V get(Object key) {
        if (size == 0) {
            return null;
        }

        int hashCode = hash(key);
        int index = indexFor(hashCode, entries.length);

        EntryNode<K, V> current = entries[index];
        if (current == null) {
            return null;
        }

        do {
            if (eq(current.key, key)) {
                return current.value;
            }
            current = current.next;
        } while (current != null);
        return null;
    }

    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    public Hash<K, V> with(K key, V value) {
        if (key == null)
            throw new NullPointerException("null keys are not allowed");
        if (value == null)
            throw new NullPointerException("null values are not allowed");

        int hashCode = hash(key);
        int index = indexFor(hashCode, entries.length);
        int size = this.size;
        EntryNode[] newTable = Arrays.copyOf(entries, entries.length);
        EntryNode<K, V> current = entries[index];
        EntryNode<K, V> found = find(current, key);

        if (found != null) {
            // E1 -> E2 -> E3 -> E4
            Deque<EntryNode<K, V>> deque = headUpTo(current, found);
            EntryNode<K, V> replacement = new EntryNode<>(key, value, hashCode, found.next);
            newTable[index] = prependEntries(deque, replacement);
        } else {
            // new head
            newTable[index] = new EntryNode<>(key, value, hashCode, current);
            size += 1;
        }
        if (size > threshold) {
            newTable = resize(newTable);
        }
        return new Hash<K, V>(newTable, size);
    }

    public Hash<K,V> without(Object key) {
        if (key == null)
            throw new NullPointerException("null keys are not allowed");
        int hashCode = hash(key);
        int index = indexFor(hashCode, entries.length);
        EntryNode<K,V> current = entries[index];
        EntryNode<K,V> found = find(current, key);

        if (found != null) {
            EntryNode[] newTable = Arrays.copyOf(entries, entries.length);
            Deque<EntryNode<K, V>> deque = headUpTo(current, found);
            newTable[index] = prependEntries(deque, found.next);
            return new Hash<K,V>(newTable, size - 1);
        }
        return this; // remove is a noop if not present
    }

    private EntryNode<K, V> prependEntries(Deque<EntryNode<K, V>> deque, EntryNode<K, V> tail) {
        EntryNode<K, V> current;EntryNode<K, V> newHead = tail, lastTail = tail;
        while ((current = deque.pollLast()) != null) {
            newHead = new EntryNode<>(current.key, current.value, current.hash, lastTail);
            lastTail = newHead;
        }
        return newHead;
    }

    private Deque<EntryNode<K, V>> headUpTo(EntryNode<K, V> current, EntryNode<K, V> found) {
        Deque<EntryNode<K, V>> deque = new ArrayDeque<>();
        while (current != found) {
            deque.add(current);
            current = current.next;
        }
        return deque;
    }

    private EntryNode[] resize(EntryNode[] table) {
        EntryNode[] newTable = new EntryNode[table.length * 2];

        for (EntryNode<K, V> entry : entries) {
            if (entry != null) {
                EntryNode<K, V> current = entry;
                do {
                    int hashCode = current.hash;
                    int index = indexFor(hashCode, newTable.length);
                    newTable[index] = new EntryNode<K, V>(current.key, current.value, current.hash, newTable[index]);
                } while ((current = current.next) != null);
            }
        }
        return newTable;
    }

    private EntryNode<K,V> find(EntryNode head, Object key) {
        if (head == null) {
            return null;
        }
        do {
            if (eq(head.key, key)) {
                return head;
            }
            head = head.next;
        } while (head != null);
        return null;
    }

    public int size() {
        return size;
    }

    private boolean eq(Object o1, Object o2) {
        return o1 == o2 || o1.equals(o2);
    }

    private int indexFor(int hashCode, int length) {
        return hashCode & (length - 1);
    }

    private static int hash(Object key) {
        int h;
        return (h = key.hashCode()) ^ (h >>> 16);
    }

    Set<Map.Entry<K, V>> entrySet() {
        LinkedHashSet<Map.Entry<K, V>> entrySet = new LinkedHashSet<>();

        for (EntryNode<K, V> entry : entries) {
            if (entry != null) {
                EntryNode<K, V> current = entry;
                do {
                    entrySet.add(current);
                } while ((current = current.next) != null);
            }
        }
        return Collections.unmodifiableSet(entrySet);
    }

    public Iterator<Map.Entry<K, V>> iterator() {
        return new AbstractIterator<Map.Entry<K,V>>() {
            private int index = -1;
            private EntryNode<K, V> last = null;

            @Override
            protected Map.Entry<K, V> computeNext() {
                if (last != null && last.next != null) {
                    return last = last.next; // move next and return it
                }
                for (index ++; index < entries.length; index++) {
                    EntryNode<K, V> entry = entries[index];
                    if (entry != null) {
                        return last = entry;
                    }
                }
                return endOfData();
            }
        };
    }

    public static void main(String[] args) {
        Hash<String, String> h = Hash.emptyHash();

        assert h.get("Hello") == null;

        h = h.with("Hello", "Wibble");

        assert h.get("Hello").equals("Wibble");

        assert h.size() == 1;

        h = h.with("Hello", "Wibble");

        assert h.get("Hello").equals("Wibble");

        assert h.size() == 1;

        h = h.with("Hello2", "Wibble");

        assert h.get("Hello2").equals("Wibble");

        assert h.size() == 2;

    }


}

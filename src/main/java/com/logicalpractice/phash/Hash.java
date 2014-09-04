package com.logicalpractice.phash;

import java.util.*;

/**
 * Simple implementation of a persistent Hash.
 *
 */
public class Hash<K,V> {
    private static int INITIAL_TABLE_SIZE = 16 ; // must be power of 2 or indexFor will assumes this
    private static EntryNode [] EMPTY_TABLE = new EntryNode[INITIAL_TABLE_SIZE];
    private static Hash EMPTY_HASH = new Hash(EMPTY_TABLE,0);

    private static final float LOAD_FACTOR = 0.75f;

    private final EntryNode<K,V> [] entries;
    private final int size ;
    private final int threshold;

    private Hash(EntryNode<K, V>[] entries, int size) {
        this.entries = entries;
        this.size = size;
        this.threshold = (int)(entries.length * LOAD_FACTOR);
    }

    @SuppressWarnings("unchecked")
    public static <K,V> Hash<K,V> emptyHash() {
        return EMPTY_HASH;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    private static class EntryNode<K,V> implements Map.Entry<K,V> {
        final K key;
        final V value;
        final int hash;
        final EntryNode<K,V> next;

        private EntryNode(K key, V value, int hash, EntryNode<K, V> next) {
            this.key = key;
            this.value = value;
            this.hash = hash;
            this.next = next;
        }

        @Override
        public K getKey() {
            return key ;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException("Updates are not supported");
        }
    }

    public V get(Object key) {
        if( size == 0 ) {
            return null;
        }

        int hashCode = hash(key);
        int index = indexFor(hashCode, entries.length);

        EntryNode<K,V> current = entries[index];
        if( current == null ) {
            return null;
        }

        do {
            if( eq(current.key, key) ) {
                return current.value;
            }
            current = current.next;
        } while( current != null );
        return null;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    public Hash<K,V> with(K key, V value) {
        assert key != null ;
        assert value != null ;

        int hashCode = hash(key);
        int index = indexFor(hashCode, entries.length);
        EntryNode [] newTable = Arrays.copyOf(entries, entries.length);

        // only handles new keys
        EntryNode<K,V> current = entries[index];
        int size = this.size;
        if( containsKey(key) ) {
            // E1 -> E2 -> E3 -> E4
            Deque<EntryNode<K,V>> deque = new ArrayDeque<>();
            while( !eq(current.key,key) ) {
                deque.add(current);
                current = current.next;
            }
            EntryNode<K,V> replacement = new EntryNode<>(key, value, hashCode, current.next);
            EntryNode<K,V> newHead = replacement, lastTail = replacement ;
            while( (current = deque.pollLast()) != null ) {
                newHead = new EntryNode<>(current.key, current.value, current.hash, lastTail );
                lastTail = newHead;
            }
            newTable[index] = newHead;
        } else {
            // new head
            newTable[index] = new EntryNode<>(key, value, hashCode, current);
            size += 1;
        }
        if ( size > threshold ) {
            newTable = resize( newTable );
        }
        return new Hash<K,V>(newTable, size);
    }

    private EntryNode[] resize(EntryNode[] table) {
        EntryNode [] newTable = new EntryNode[table.length * 2];

        for (EntryNode<K, V> entry : entries) {
            if( entry != null ) {
                EntryNode<K,V> current = entry;
                do {
                    int hashCode = current.hash;
                    int index = indexFor(hashCode, newTable.length);
                    newTable[index] = new EntryNode<K,V>(current.key, current.value, current.hash, newTable[index]);
                } while ( (current = current.next) != null );
            }
        }
        return newTable;
    }

    public int size() { return size; }

    private boolean eq(Object o1, Object o2) {
        return o1 == o2 || o1.equals(o2);
    }

    private int indexFor(int hashCode, int length) {
        return hashCode & ( length - 1);
    }

    private int hash(Object key) {
        return key.hashCode();
    }

    Set<Map.Entry<K,V>> entrySet() {
        LinkedHashSet<Map.Entry<K,V>> entrySet = new LinkedHashSet<>();

        for (EntryNode<K, V> entry : entries) {
            if( entry != null ) {
                EntryNode<K,V> current = entry;
                do {
                    entrySet.add( current );
                } while ( (current = current.next) != null );
            }
        }
        return Collections.unmodifiableSet(entrySet);
    }

    public static void main(String[] args) {
        Hash<String,String> h = Hash.emptyHash();

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

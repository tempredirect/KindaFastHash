package com.logicalpractice.kindafasthash;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class MapBenchmark {
    private static int NUMBER_OF_KEYS = Integer.getInteger("NUMBER_OF_KEYS", 10_000);

    public static class MapHolder {
        Map<String,Object> map;

        void populateMap() {
            for (String key : Keys.sequentialKeys(NUMBER_OF_KEYS)) {
                map.put(key, new Object());
            }
        }
    }

    @State(Scope.Benchmark)
    public static class CopyOnWriteMapHolder extends MapHolder {
        @Setup
        public void setup() {
            map = new CopyOnWriteHashMap<>();
            populateMap();
        }
    }

    @State(Scope.Benchmark)
    public static class ConcurrentHashMapHolder extends MapHolder {
        @Setup
        public void setup() {
            map = new ConcurrentHashMap<>();
            populateMap();
        }
    }

    @State(Scope.Benchmark)
    public static class KeyList {
        private final String [] keys = Keys.sequentialKeys(NUMBER_OF_KEYS);
    }

    @State(Scope.Thread)
    public static class Counter {
        int count = 0;

        public int nextUpTo(int limit) {
            return (count ++) % limit;
        }
    }

    @Benchmark
    public void copyOnWriteHashMapPuts(
            Blackhole bh,
            CopyOnWriteMapHolder holder,
            KeyList keys,
            Counter counter
    ) {
        put(bh, holder, keys, counter);
    }

    @Benchmark
    public void concurrentHashMapPuts(
            Blackhole bh,
            ConcurrentHashMapHolder holder,
            KeyList keys,
            Counter counter
    ) {
        put(bh, holder, keys, counter);
    }

    private void put(Blackhole bh, MapHolder holder, KeyList keys, Counter counter) {
        bh.consume(holder.map.put(keys.keys[counter.nextUpTo(keys.keys.length)], new Object()));
    }

    @Benchmark
    public void copyOnWriteHashMapGet(
            Blackhole bh,
            CopyOnWriteMapHolder holder,
            KeyList keys,
            Counter counter
    ) {
        get(bh, holder, keys, counter);
    }

    @Benchmark
    public void concurrentHashMapGet(
            Blackhole bh,
            ConcurrentHashMapHolder holder,
            KeyList keys,
            Counter counter
    ) {
        get(bh, holder, keys, counter);
    }

    private void get(Blackhole bh, MapHolder holder, KeyList keys, Counter counter) {
        bh.consume(holder.map.get(keys.keys[counter.nextUpTo(keys.keys.length)]));
    }
}

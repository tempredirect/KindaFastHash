package com.logicalpractice.kindafasthash.benchmarks;

import com.logicalpractice.kindafasthash.CopyOnWriteHashMap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class MapBenchmark {

    public static abstract class MapHolder {
        Map<String,Object> map;
        private String [] keys;
        void populateMap() {
            keys = Keys.sequentialKeys(numberOfKeys());
            for (String key : keys) {
                map.put(key, new Object());
            }
        }
       abstract int numberOfKeys();
    }

    @State(Scope.Benchmark)
    public static class CopyOnWriteMapHolder extends MapHolder {
        @Param({"10", "1000", "10000", "100000"})
        int numberOfKeys = 10;

        @Setup
        public void setup() {
            map = new CopyOnWriteHashMap<>();
            populateMap();
        }

        @Override
        int numberOfKeys() {
            return numberOfKeys;
        }
    }

    @State(Scope.Benchmark)
    public static class ConcurrentHashMapHolder extends MapHolder {
        @Param({"10", "1000", "10000", "100000"})
        int numberOfKeys = 10;

        @Setup
        public void setup() {
            map = new ConcurrentHashMap<>();
            populateMap();
        }

        @Override
        int numberOfKeys() {
            return numberOfKeys;
        }

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
            Counter counter
    ) {
        put(bh, holder, counter);
    }

    @Benchmark
    public void concurrentHashMapPuts(
            Blackhole bh,
            ConcurrentHashMapHolder holder,
            Counter counter
    ) {
        put(bh, holder, counter);
    }

    private void put(Blackhole bh, MapHolder holder, Counter counter) {
        bh.consume(holder.map.put(holder.keys[counter.nextUpTo(holder.keys.length)], new Object()));
    }

    @Benchmark
    public void copyOnWriteHashMapGet(
            Blackhole bh,
            CopyOnWriteMapHolder holder,
            Counter counter
    ) {
        get(bh, holder, counter);
    }

    @Benchmark
    public void concurrentHashMapGet(
            Blackhole bh,
            ConcurrentHashMapHolder holder,
            Counter counter
    ) {
        get(bh, holder, counter);
    }

    private void get(Blackhole bh, MapHolder holder, Counter counter) {
        bh.consume(holder.map.get(holder.keys[counter.nextUpTo(holder.keys.length)]));
    }
}

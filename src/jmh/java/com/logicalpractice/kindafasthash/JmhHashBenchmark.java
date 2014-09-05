package com.logicalpractice.kindafasthash;


import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

/**
 *
 */
public class JmhHashBenchmark {

    @State(Scope.Benchmark)
    public static class HashReference {
        Hash<String,Object> hash = Hash.emptyHash();
        String [] keysToAdd;

        @Setup
        public void setup(){
            keysToAdd = Keys.sequentialKeys(10_000);
            for(String key: keysToAdd) {
                hash = hash.with(key, new Object());
            }
        }

        @TearDown
        public void check() {
            assert hash.size() == 1000: "Size should not have changed";
        }

        public String nextKey(ArrayCounter counter) {
            return keysToAdd[(counter.counter ++) % keysToAdd.length];
        }
    }

    @State(Scope.Thread)
    public static class ArrayCounter {
        int counter = 0;
    }

    @Benchmark
    public void put(Blackhole bh, HashReference hashReference, ArrayCounter counter) {
        String key = hashReference.nextKey(counter);
        bh.consume(hashReference.hash.with(key, new Object()));
    }

    @Benchmark
    public void get(Blackhole bh, HashReference hashReference, ArrayCounter counter) {
        String key = hashReference.keysToAdd[(counter.counter ++) % hashReference.keysToAdd.length];
        bh.consume(hashReference.hash.with(key, new Object()));
    }
}

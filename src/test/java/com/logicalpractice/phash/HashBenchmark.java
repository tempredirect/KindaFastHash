package com.logicalpractice.phash;

import com.google.caliper.Benchmark;
import com.google.caliper.Param;
import com.google.caliper.runner.CaliperMain;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.*;

import static com.logicalpractice.phash.Keys.generateKeys;

/**
 *
 */
public class HashBenchmark extends Benchmark {

    public static void main(String[] args) {
        CaliperMain.main(HashBenchmark.class, args);
    }

    @Param({"1","5","10","20"}) int threads = 10;

    @Param({"0","1","2"}) int implementation = 0;

    private ExecutorService threadPool ;

    private Map<String, Integer> map;
    String [] keys;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        threadPool = Executors.newFixedThreadPool(threads);
        keys = generateKeys(10000);

        switch (implementation) {
            case 0:
                map = new NonLockingHashMap<>();
                break;
            case 1:
                map = Collections.synchronizedMap(new HashMap<String, Integer>());
                break;
            case 2:
                map = new ConcurrentHashMap<>();
                break;
            default:
                throw new Error();
        }
        for (String key : keys) {
            map.put(key, 1);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        threadPool.shutdownNow();
        super.tearDown();
    }

    public long timeReadonly(int reps) throws Exception {
        return runBenchWith(ReaderCallable.class, reps);
    }

    @SuppressWarnings("unchecked")
    private long runBenchWith(Class<?> clazz, int reps) throws Exception {
        List<Callable<Long>> callables = new ArrayList<>(threads);

        Constructor constructor = clazz.getConstructor(Map.class, String[].class, int.class);

        int each = reps / threads;

        for (int i = 0; i < threads; i++ ){
            int calls = each;
            if( i == threads - 1 ) {
                calls += reps % threads;
            }
            callables.add((Callable<Long>) constructor.newInstance(map, keys, calls));
        }

        List<Future<Long>> futures = threadPool.invokeAll(callables);

        long result = 0L;
        for (Future<Long> future : futures) {
            result += future.get();
        }

        return result;
    }

    public static class ReaderCallable implements Callable<Long> {

        private Map<String,Integer> map;
        private String [] keys;
        private final int reps;

        public ReaderCallable(Map<String,Integer> map, String [] keys, int reps) {
            this.map  = map;
            this.keys = keys;
            this.reps = reps;
        }

        @Override
        public Long call() throws Exception {
            long result = 0L;

            for( int i=0; i < reps; i++ ) {
                result += map.get(keys[reps % keys.length]);
            }

            return result;
        }
    }
}

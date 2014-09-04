package com.logicalpractice.kindafasthash;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class MapConcurrencyTest {

    private static final long MIN_WARMUP_TIME_NS = TimeUnit.SECONDS.toNanos(20);
    private static final long TEST_TIME = TimeUnit.SECONDS.toNanos(5);

    Map<String, Integer> testObject = new NonLockingHashMap<>();

    String [] keys = Keys.generateKeys(10000);

    MetricRegistry metrics = new MetricRegistry();

    Timer operationTime = metrics.timer("operationTime");

    @Before
    public void setUp() throws Exception {
        for (String key : keys) {
            testObject.put(key, 1);
        }
        warmUpReads();
        System.gc();
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("Test finished " + operationTime.getCount());
        operationTime.getSnapshot().dump(System.out);
    }

    @Test
    public void singleThreaded() {
        long start = System.nanoTime();
        int offset = 0;
        int batchSize = 100;
        long result = 0L;
        while( timeTaken(start) < TEST_TIME ) {
            try( Timer.Context t = operationTime.time() ) {
                result += runKeys(batchSize, offset);
                offset += batchSize;
            }
        }
        if( System.currentTimeMillis() < 0 ){
            System.out.println(result);
        }
    }

    private void warmUpReads() {
        long result = 0;
        int offset = 0;
        long start = System.nanoTime();
        while( timeTaken( start ) < MIN_WARMUP_TIME_NS) {
            result += runKeys(1000,offset);
            offset += 1000;
        }
        if( System.currentTimeMillis() < 0 ){
            System.out.println(result);
        }

    }

    private long timeTaken(long start) {
        return System.nanoTime() - start;
    }

    private long runKeys(int reps, int offset) {
        Map<String,Integer> map = testObject;
        String [] localKeys = keys;
        int result = 0;
        for( int i = 0; i < reps; reps ++ ) {
            result += map.get(localKeys[(i + offset) % reps]);
        }
        return result;
    }
}

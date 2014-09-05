package com.logicalpractice.kindafasthash.benchmarks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class Keys {

    public static String[] sequentialKeys(int count) {
        List<String> keys = new ArrayList<>(count);

        for( int i = 0; i < count; i ++ ) {
            String key = String.format("%6x", i);
            keys.add(key);
        }

        Collections.shuffle(keys);
        return keys.toArray(new String[count]);
    }

}

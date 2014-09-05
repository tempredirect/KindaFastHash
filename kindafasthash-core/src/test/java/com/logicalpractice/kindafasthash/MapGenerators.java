package com.logicalpractice.kindafasthash;

import com.google.common.collect.testing.TestStringMapGenerator;

import java.util.Map;

/**
 *
 */
public class MapGenerators {

    public static class CopyOnWriteHashMapGenerator extends TestStringMapGenerator {
        @Override
        protected Map<String, String> create(Map.Entry<String, String>[] entries) {
            CopyOnWriteHashMap<String,String> result = new CopyOnWriteHashMap<>();
            for (Map.Entry<String, String> entry : entries) {
                result.put(entry.getKey(), entry.getValue());
            }
            return result;
        }
    }
}

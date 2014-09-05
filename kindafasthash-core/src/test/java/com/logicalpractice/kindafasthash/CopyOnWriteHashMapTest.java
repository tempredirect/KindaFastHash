package com.logicalpractice.kindafasthash;

import com.google.common.collect.testing.MapTestSuiteBuilder;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Iterator;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

public class CopyOnWriteHashMapTest extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(CopyOnWriteHashMapTest.class);

        suite.addTest(MapTestSuiteBuilder.using(new MapGenerators.CopyOnWriteHashMapGenerator())
                .withFeatures(
                        CollectionSize.ANY,
//                  CollectionFeature.SERIALIZABLE_INCLUDING_VIEWS,
//                  MapFeature.REJECTS_DUPLICATES_AT_CREATION,
                        MapFeature.SUPPORTS_PUT,
                        MapFeature.SUPPORTS_REMOVE,
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                        CollectionFeature.ALLOWS_NULL_QUERIES)
                .named("CopyOnWriteHashMap")
                .createTestSuite());

        return suite;
    }

    public void testKeySet() throws Exception {
        CopyOnWriteHashMap<Integer,Integer> testObject = new CopyOnWriteHashMap<>();

        testObject.put(1, 2);
        testObject.put(2, 4);
        testObject.put(3, 6);

        assertThat(testObject.keySet(), hasItems(1, 2, 3));
    }

    public void testKeySetClear() throws Exception {
        CopyOnWriteHashMap<Integer,Integer> testObject = new CopyOnWriteHashMap<>();

        testObject.put(1, 2);
        testObject.put(2, 4);
        testObject.put(3, 6);

        Set<Integer> keySet = testObject.keySet();
        keySet.clear();

        assertThat("keySet.isEmpty()", testObject.keySet().isEmpty(), equalTo(true));
        assertThat("Map.isEmpty()", testObject.isEmpty(), equalTo(true));
    }

    public void testKeySetRemoveAllViaIterator() throws Exception {
        CopyOnWriteHashMap<Integer,Integer> testObject = new CopyOnWriteHashMap<>();

        testObject.put(1, 2);
        testObject.put(2, 4);
        testObject.put(3, 6);

        Set<Integer> keySet = testObject.keySet();

        for(Iterator<Integer> iter = keySet.iterator(); iter.hasNext();) {
            iter.next();
            iter.remove();
        }

        assertThat("keySet.isEmpty()", testObject.keySet().isEmpty(), equalTo(true));
        assertThat("Map.isEmpty()", testObject.isEmpty(), equalTo(true));
    }


}
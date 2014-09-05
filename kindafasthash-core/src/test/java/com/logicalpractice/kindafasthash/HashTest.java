package com.logicalpractice.kindafasthash;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterators.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class HashTest {

    @Test
    public void empty() throws Exception {
        assertThat(Hash.emptyHash().isEmpty(), equalTo(true));
        assertThat(Hash.emptyHash().with("Wibble",1).isEmpty(), equalTo(false));
    }

    @Test
    public void with() throws Exception {

        Hash<String,Integer> testObject = Hash.<String,Integer>emptyHash().with("Wibble", 10).with("Wobble",11);

        assertThat(testObject.size(), equalTo(2));
        assertThat(testObject.get("Wibble"), equalTo(10));
        assertThat(testObject.containsKey("Wibble"), equalTo(true));
    }

    @Test
    public void withDupKeys() throws Exception {

        Hash<String,Integer> testObject = Hash.<String,Integer>emptyHash().with("Wibble", 10).with("Wibble", 11);

        assertThat(testObject.size(), equalTo(1));
        assertThat(testObject.get("Wibble"), equalTo(11));
    }

    @Test
    public void without() throws Exception {

        Hash<String,Integer> testObject = Hash.<String,Integer>emptyHash()
                .with("Wibble", 10)
                .with("Wobble",11)
                .without("Wibble");

        assertThat(testObject.get("Wibble"), nullValue());

        assertThat(testObject.containsKey("Wibble"), equalTo(false));
    }

    @Test
    public void withHashCollisions_TwoElementsReplaceFirstInLinkedList() throws Exception {

        Hash<ConstantHashCode, Integer> testObject = Hash.emptyHash();
        testObject = testObject.with(new ConstantHashCode("Wibble"),1)
                                .with(new ConstantHashCode("Wobble"), 2);

        assertThat( testObject.size(), equalTo(2));
        assertThat( testObject.get(new ConstantHashCode("Wibble")), equalTo(1));
        assertThat( testObject.get(new ConstantHashCode("Wobble")), equalTo(2));

        // replace the last key (will be at the head of the LinkedList)
        testObject = testObject.with(new ConstantHashCode("Wobble"), 3);
        assertThat( testObject.size(), equalTo(2));
        assertThat( testObject.get(new ConstantHashCode("Wobble")), equalTo(3));
    }

    @Test
    public void withHashCollisions_TwoElementsReplaceLastInLinkedList() throws Exception {

        Hash<ConstantHashCode, Integer> testObject = Hash.emptyHash();
        testObject = testObject.with(new ConstantHashCode("Wibble"),1)
                                .with(new ConstantHashCode("Wobble"), 2);

        assertThat( testObject.size(), equalTo(2));
        assertThat( testObject.get(new ConstantHashCode("Wibble")), equalTo(1));
        assertThat( testObject.get(new ConstantHashCode("Wobble")), equalTo(2));

        // replace the tail of the linkedlist (first key we added
        testObject = testObject.with(new ConstantHashCode("Wibble"), 3);
        assertThat( testObject.size(), equalTo(2));
        assertThat( testObject.get(new ConstantHashCode("Wibble")), equalTo(3));
    }

    @Test
    public void withHashCollisions_ThreeElementsReplaceMiddleInLinkedList() throws Exception {

        Hash<ConstantHashCode, Integer> testObject = Hash.emptyHash();
        testObject = testObject.with(new ConstantHashCode("Wibble"),1)
                                .with(new ConstantHashCode("Wobble"), 2)
                                .with(new ConstantHashCode("Foo"), 3);

        assertThat( testObject.size(), equalTo(3));

        // replace the middle of the linkedlist
        testObject = testObject.with(new ConstantHashCode("Wobble"), 7);
        assertThat( testObject.size(), equalTo(3));
        assertThat( testObject.get(new ConstantHashCode("Wobble")), equalTo(7));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void iterator_shouldContainAllEntries() throws Exception {

        Hash<String, Integer> testObject = Hash.emptyHash();
        testObject = testObject.with("Wibble",1)
                .with("Wobble", 2)
                .with("Foo", 3);
        List<String> keys = newArrayList(transform(testObject.iterator(), this.<String>key()));
        assertThat(keys, hasItems("Wibble", "Wobble", "Foo"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void iterator_withConstantHashCodes_ShouldContainAllEntries() throws Exception {
        Hash<ConstantHashCode, Integer> testObject = Hash.emptyHash();
        testObject = testObject.with(new ConstantHashCode("Wibble"),1)
                .with(new ConstantHashCode("Wobble"), 2)
                .with(new ConstantHashCode("Foo"), 3);
        List<String> keys = newArrayList(
                transform(
                        transform(testObject.iterator(), this.<ConstantHashCode>key()),
                Functions.toStringFunction()));
        assertThat(keys, hasItems("Wibble", "Wobble", "Foo"));
    }

    @Test
    public void iterator_shouldBeEmptyForEmptyHash() throws Exception {
        Hash<String, Integer> testObject = Hash.emptyHash();
        assertThat(testObject.iterator().hasNext(), equalTo(false));
    }

    private <K> Function<? super Map.Entry<K,Integer>,K> key() {
        return new Function<Map.Entry<K, Integer>, K>() {
            @Nullable
            @Override
            public K apply(@Nullable Map.Entry<K, Integer> input) {
                assert input != null;
                return input.getKey();
            }
        };
    }

    static class ConstantHashCode {
        private final String value ;

        ConstantHashCode(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ConstantHashCode that = (ConstantHashCode) o;

            return value.equals(that.value);
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}



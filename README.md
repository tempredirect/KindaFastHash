Kinda (no so) Fast Hash
=======================

This isn't really a serious project, but a learning exercise.

I set out to create a persistent hash data structure. This turns out to be `com.logicalpractice.kindafasthash.Hash`
it's a very simple API

    Hash<Integer,String> hash = Hash.emptyHash().with(1, "one").with(2, "two");

    System.out.println(hash.get(2));

    hash = hash.without(2);

    System.out.println(hash.containsKey(2));

I then decided that what I really wanted to implement a full `java.util.Map` implementation on top of this
structure. Surely that won't be hard.

`com.logicalpractice.kindafasthash.CopyOnWriteHashMap` is a full implementation of `Map` that promises to be 
thread safe.

Trouble is after benchmarking my new class against `ConcurrentHashMap` from jdk8, it turns out that 
`ConcurrentHashMap` (at least in oracle jdk8) is insanely fast and beats by implementation on 
even on read only work loads.
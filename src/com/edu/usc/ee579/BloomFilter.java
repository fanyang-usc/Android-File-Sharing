package com.edu.usc.ee579;

import java.util.BitSet;

public class BloomFilter {
    private static final int SIZE = 1<<25; 
    private static final int[] seeds = new int[] { 5, 7, 11, 13, 31, 37, 61 };
    private BitSet bits = new BitSet(SIZE);

    private SimpleHash[] func = new SimpleHash[seeds.length];

    public BloomFilter() {
	for (int i = 0; i < seeds.length; i++){
	    func[i] = new SimpleHash(SIZE, seeds[i]);
	}
    }

    public void mark(String value){
	for (SimpleHash f : func){
	    bits.set(f.hash(value), true);
	}
    }

    public boolean test(String value){
	if (value == null){
	    return false;
	}
	boolean ret = true;
	for (SimpleHash f : func){
	    ret = ret && bits.get(f.hash(value));
	}
	return ret;
    }

    public static class SimpleHash{
	private int cap;
	private int seed;

	public SimpleHash(int cap, int seed){
	    this.cap = cap;
	    this.seed = seed;
	}

	public int hash(String value){
	    int result = 0;
	    int len = value.length();
	    for (int i = 0; i < len; i++){
		result = seed * result + value.charAt(i);
	    }
	    return (cap - 1) & result;
	}
    }
}
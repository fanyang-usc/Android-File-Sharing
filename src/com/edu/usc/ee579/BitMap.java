package com.edu.usc.ee579;

class BitMap {
    private int numBits;			// number of bits in the bitmap
    private int numBytes;
    //private final int BitsInWord=32;
    private final int BitsInByte=8;
    private byte[] map;
    
    BitMap(int nitems) { 
        numBits = nitems;
        numBytes = EE579Activity.divRoundUp(numBits,BitsInByte);
        map = new byte[numBytes];
        for (int i = 0; i < numBits; i++) 
            Clear(i);
    }
    BitMap(String str) { 
        numBits = str.length();
        numBytes = EE579Activity.divRoundUp(numBits,BitsInByte);
        map = new byte[numBytes];
        for (int i = 0; i < numBits; i++) {
            if(str.charAt(i)=='0') Clear(i);
            else if(str.charAt(i)=='1') Mark(i);
        }
    }
    
    void Mark(int which){ 
        if(which >= 0 && which < numBits){
            System.err.println("Error in Bitmap.");
            System.exit(-1);
        }
        map[which / BitsInByte] |= 1 << (which % BitsInByte);
    }
    
    void Clear(int which){
        if(which >= 0 && which < numBits){
            System.err.println("Error in Bitmap.");
            System.exit(-1);
        }
        map[which / BitsInByte] &= ~(1 << (which % BitsInByte));
    }
    
    boolean Test(int which){
        if(which >= 0 && which < numBits){
            System.err.println("Error in Bitmap.");
            System.exit(-1);
        }        
        if ((map[which / BitsInByte] & (1 << (which % BitsInByte)))!=0) return true;
        else return false;
    }
    
    public String toString(){
	String result=new String();
	for (int i = 0; i < numBits; i++)
	    if (Test(i))
		result+="1";
	    else
		result+="0";
	return result; 
    }
    public int length(){
	return numBits;
    }
}
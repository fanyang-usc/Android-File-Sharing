package com.edu.usc.ee579;

// A bitmap class, referenced and modified from CS402 project.
class BitMap {
    private int numBits;			// number of bits in the bitmap
    private int numBytes;			// number of bytes used
    private final int BitsInByte=8;
    private byte[] map;
    
    // construct a bitmap with certain number of bits. 
    BitMap(int nitems) { 
        numBits = nitems;
        numBytes = divRoundUp(numBits,BitsInByte);
        // use the min number of bytes and set each bits to 0;
        map = new byte[numBytes];
        for (int i=0; i<numBits; i++) 
            Clear(i);
    }
    
    // construct a bitmap from a 1/0 string.
    BitMap(String str) { 
        numBits = str.length();
        numBytes =divRoundUp(numBits,BitsInByte);
        map = new byte[numBytes];
        for (int i=0; i<numBits; i++) {
            if(str.charAt(i)=='0') Clear(i);
            else if(str.charAt(i)=='1') Mark(i);
        }
    }
    
    // set the bit at the certain position in the bitmap to 1.
    void Mark(int which){ 
        if(which<0||which>=numBits){
            System.err.println("Error in Bitmap.");
            System.exit(-1);
        }
        map[which/BitsInByte]|= 1<<(which%BitsInByte);
    }
    
    // clear the bit at the position
    void Clear(int which){
        if(which<0||which>=numBits){
            System.err.println("Error in Bitmap.");
            System.exit(-1);
        }
        map[which/BitsInByte]&= ~(1<<(which%BitsInByte));
    }
    
    // see if the bit at the position is set or not. true if set.
    boolean Test(int which){
        if(which<0||which>=numBits){
            System.err.println("Error in Bitmap.");
            System.exit(-1);
        }        
        if ((map[which/BitsInByte]&(1<<(which%BitsInByte)))!=0) return true;
        else return false;
    }
    
    // convert the bitmap to string for print use
    public String toString(){
	String result=new String();
	for (int i=0; i<numBits; i++)
	    if (Test(i))
		result+="1";
	    else
		result+="0";
	return result; 
    }
    
    //get the length of the bitmap, num of bits
    public int length(){
	return numBits;
    }
    
    //return the number of bits marked to 1
    int numMarked() 
    {
        int count=0;
        for (int i=0; i<numBits; i++)
    	if (Test(i)) count++;
        return count;
    }

    // round up the division. get min number of bytes need
    public static int divRoundUp(int n,int s){
	return (((n) / (s)) + ((((n) % (s)) > 0) ? 1 : 0));
    }
}
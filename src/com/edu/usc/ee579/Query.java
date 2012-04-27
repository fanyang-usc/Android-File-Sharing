package com.edu.usc.ee579;

import java.util.ArrayList;
import android.util.Log;

public class Query{
    
    //function for the server to check whether it has the files or chunks needed.
    public static ArrayList<String> checkAvailablility(String listStr){
	ArrayList<String> result=new ArrayList<String>();
	String[] buffer=listStr.split(",");
	int count=0;
	//check the availability using hash table.
	/*Query method for pure hashtable implementation	
	 * 
	 * for(int i=0;i<buffer.length;i=i+2){
	    if(EE579Activity.availableFileChunks.get(buffer[i])!=null){
		HashMap<String,Boolean> chunkTable=EE579Activity.availableFileChunks.get(buffer[i]);
		String[] chunkList=buffer[i+1].split("+");
		for(int j=0;j<chunkList.length;j++){
		    if(chunkTable.get(chunkList[j])!=null){
			result.put(chunkList[j], buffer[i]);
			count++;
		    }
		}
	    }
	}*/
	
	/*Query method for BloomFolter solution
	 * 
       for(int i=0;i<buffer.length;i++){
          if(EE579Activity.availableChunk.test(buffer[i])){
          	result.add(buffer[i]);
          	count++;
      		}
       } */
	
	
	BitMap chunkMap;
	BitMap needChunkMap;
	for(int i=0;i<buffer.length;i=i+2){
	    chunkMap=EE579Activity.availableChunkMap.get(buffer[i]);
	    if(chunkMap!=null){	
		needChunkMap=new BitMap(buffer[i+1]);
		if(chunkMap.length()!=needChunkMap.length()){
		    Log.d("EE579","Error in Query.java");
		    System.exit(-1);
		}
		for(int j=0;j<chunkMap.length();j++){
		    if(needChunkMap.Test(j)&&chunkMap.Test(j)){
			result.add(buffer[i]+","+new Integer(j).toString());
			count++;
		    }
		}
	    }
	}	
	
	if(count==0) return null;
	else return result;
    }
    
    

}
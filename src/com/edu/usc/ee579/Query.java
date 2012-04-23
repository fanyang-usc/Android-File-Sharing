package com.edu.usc.ee579;

import java.util.HashMap;

public class Query{
    
    //function for the server to check whether it has the files or chunks needed.
    public static HashMap<String,String> checkAvailablility(String listStr){
	HashMap<String,String> result=new HashMap<String,String>();
	String[] buffer=listStr.split(",");
	int count=0;
	//check the availability using hash table.
	for(int i=0;i<buffer.length;i=i+2){
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
	}
	if(count==0) return null;
	else return result;
    }

}
package com.edu.usc.ee579;

import java.util.HashMap;
import java.util.Hashtable;

public class Query{
    
    //function for the server to chech whether it has the files or chunks needed.
    public static String checkAvailablility(String listStr,Hashtable hashtable){
	String result=new String();
	String[] buffer=listStr.split(",");
	//get file needed list from the client.
	int[] fileList=new int[buffer.length];
	int count=0;
	for(int i=0;i<buffer.length;i++){
	    fileList[i]=Integer.parseInt(buffer[i]);
	}
	//check the availability using hashtable.
	for(int i=0;i<fileList.length;i++){
	    if(hashtable.get(fileList[i])!=null){
		count++;
		if(i!=fileList.length){
		    result+=fileList[i]+",";
		}else{
		    result+=fileList[i];
		}		
	    }
	}
	if(count!=0)  return result;
	else return null;
    }
/*    public static int[] getAvailableList(String stringList){
	
    }*/
}
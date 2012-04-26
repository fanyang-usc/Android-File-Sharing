package com.edu.usc.ee579;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.util.Log;

class CombineFile extends Thread{
    private String fileNumStr=new String();
    CombineFile(String fileNumStr){
	this.fileNumStr=fileNumStr;
    }
    public void run() {
	combineFiles();
	return;
    }
    public void combineFiles(){	
	try {
	    File file= new File("/sdcard/ee579/"+EE579Activity.allFileList.get(fileNumStr));
	    FileOutputStream out=new FileOutputStream(file);
	    int count=EE579Activity.numOfChunks.get(fileNumStr);
	    int i=0;
	    while(i<count){
		File tmpFile= new File("/sdcard/ee579/tmp/"+fileNumStr+"-"+i+".tmp");
		FileInputStream in=new FileInputStream(tmpFile);
		byte[] buffer=new byte[EE579Activity.BYTESPERCHUNK];
		int numOfBytesRead=in.read(buffer);
		if(numOfBytesRead!=EE579Activity.BYTESPERCHUNK){
		    byte[] newbuffer=new byte[numOfBytesRead];
		    for(int j=0;j<numOfBytesRead;j++) newbuffer[j]=buffer[j];
		    out.write(newbuffer);
		}else{
		    out.write(buffer);
		}
		out.flush();		
		in.close();
		tmpFile.delete();
		i++;		
	    }
	    out.close();
	} catch (IOException e) {
	    Log.d("EE579","IO error when combining files");
	}
    }
}
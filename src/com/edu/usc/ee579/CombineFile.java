package com.edu.usc.ee579;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.util.Log;

class CombineFile extends Thread{
    private String fileNumStr=new String();
    
    // constructor, indicate the file number of the file need to be combined
    CombineFile(String fileNumStr){
	this.fileNumStr=fileNumStr;
    }
    
    // override the run method from thread class.
    public void run() {
	combineFiles();
	return;
    }
    
    // combine the tmp files of a particular file to one file.
    public void combineFiles(){	
	try {
	    // get the filename and create a final file with the name
	    File file= new File("/sdcard/ee579/"+EE579Activity.allFileList.get(fileNumStr));
	    FileOutputStream out=new FileOutputStream(file);
	    // get the total number of chunks of that file
	    int count=EE579Activity.numOfChunks.get(fileNumStr);
	    int i=0;
	    while(i<count){
		// read the tmp chunk file and write to the final file
		File tmpFile= new File("/sdcard/ee579/tmp/"+fileNumStr+"-"+i+".tmp");
		FileInputStream in=new FileInputStream(tmpFile);
		byte[] buffer=new byte[EE579Activity.BYTESPERCHUNK];
		int numOfBytesRead=in.read(buffer);
		// if it is the last chunk, the number of bytes may be different
		// then write out the correct number of bytes
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
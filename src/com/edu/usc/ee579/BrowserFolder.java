package com.edu.usc.ee579;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

// A simple Directory Browser and support several types of media playback.
public class BrowserFolder extends ListActivity {

    private List<String> items = null;
    private File lastDirectory=null;
    @Override
    public void onCreate(Bundle icicle) {
	super.onCreate(icicle);
	setContentView(R.layout.folder);
	// show the APP directory
	getFiles(new File("/sdcard/ee579").listFiles());
	lastDirectory=new File("/sdcard/");
    }
    
    // perform according to the click
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){
	int selectedRow = (int)id;
	// the first row is always the Back function.
	if(selectedRow == 0){
	    if(lastDirectory.getPath().equals("/")){
		Toast.makeText(this, "You have reached the root directory.",Toast.LENGTH_SHORT).show();
	    }
	    // show the content of last level of directory.
	    getFiles(lastDirectory.listFiles());
	    // calculate the new last level directory for next use of "Back"
	    String[] buffer=lastDirectory.getPath().split("\\/");
	    String fileName=new String();
	    for(int i=0;i<buffer.length-1;i++){
		fileName+=buffer[i]+"/";
	    }
	    fileName+="/";
	    lastDirectory=new File(fileName);
	}else{		
	    // get the file name of the row clicked.
	    File file = new File(items.get(selectedRow));
	    if(file.isDirectory()){
		// show the content of the directory if it is a directory
		getFiles(file.listFiles());
		// calculate the new last level directory for next use of "Back"
		String[] buffer=file.getPath().split("\\/");
		String fileName=new String();
		for(int i=0;i<buffer.length-1;i++){
		    fileName+=buffer[i]+"/";
		}
		fileName+="/";
		lastDirectory=new File(fileName);
	    }else{
		// if it is a file instead of a directory, execute the file according to the type using proper activity.
		Intent intent = new Intent("android.intent.action.VIEW"); 
		intent.addCategory("android.intent.category.DEFAULT");  
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		Uri uri = Uri.fromFile(file);
		switch(getFileType(file.getName())){
		case 0:		            	   
		    intent.setDataAndType(uri, "text/plain"); 
		    break;
		case 1:  		    		  
		    intent.setDataAndType(uri, "application/pdf");
		    break;
		case 2:
		    intent.setDataAndType(uri, "image/*");
		    break;
		case 3:
		    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  
		    intent.putExtra("oneshot", 0);  
		    intent.putExtra("configchange", 0); 
		    intent.setDataAndType(uri, "audio/*");
		    break;
		case 4:
		    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  		    
		    intent.putExtra("oneshot", 0);  		  
		    intent.putExtra("configchange", 0);  		  
		    intent.setDataAndType(uri, "video/*"); 
		    break;
		default:
		    Toast.makeText(this, "Please View the content of the file with a File Manager.",Toast.LENGTH_SHORT).show();
		    intent=null;
		}
		if(intent!=null) startActivity(intent);
	    }
	}
    }
    
    //decide the file type from the file name
    private int getFileType(String fileName){
	String[] buffer=fileName.split("\\.");
	if(buffer[buffer.length-1].equals("txt")) return 0;
	else if(buffer[buffer.length-1].equals("pdf")) return 1;
	else if(buffer[buffer.length-1].equals("jpg")||buffer[buffer.length-1].equals("png")) return 2;
	else if(buffer[buffer.length-1].equals("mp3")) return 3;
	else if(buffer[buffer.length-1].equals("avi")||buffer[buffer.length-1].equals("mp4")) return 4;
	else return -1;
    }
    
    //get all the files in a directory and use the arrayadapter to show them on the arraylist
    private void getFiles(File[] files){
	items = new ArrayList<String>();
	items.add("Back To Previous Directory");
	//add every file to the list
	for(File file : files){
	    if(file.getPath().equals("/root")) continue;
	    items.add(file.getPath());
	}
	ArrayAdapter<String> fileList = new ArrayAdapter<String>(this,R.layout.rowfolder, items);
	setListAdapter(fileList);
    }
} 
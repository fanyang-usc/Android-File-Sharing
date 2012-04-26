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

public class BrowserFolder extends ListActivity {

    private List<String> items = null;
    private File lastDirectory=null;
    @Override
    public void onCreate(Bundle icicle) {
	super.onCreate(icicle);
	setContentView(R.layout.folder);
	getFiles(new File("/sdcard/ee579").listFiles());
	lastDirectory=new File("/sdcard/");
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){
	int selectedRow = (int)id;
	if(selectedRow == 0){
	    if(lastDirectory.getPath().equals("/")){
		Toast.makeText(this, "You have reached the root directory.",Toast.LENGTH_SHORT).show();
	    }
	    getFiles(lastDirectory.listFiles());
	    String[] buffer=lastDirectory.getPath().split("\\/");
	    String fileName=new String();
	    for(int i=0;i<buffer.length-1;i++){
		fileName+=buffer[i]+"/";
	    }
	    fileName+="/";
	    lastDirectory=new File(fileName);
	}else{		
	    File file = new File(items.get(selectedRow));
	    if(file.isDirectory()){
		getFiles(file.listFiles());
		String[] buffer=file.getPath().split("\\/");
		String fileName=new String();
		for(int i=0;i<buffer.length-1;i++){
		    fileName+=buffer[i]+"/";
		}
		fileName+="/";
		lastDirectory=new File(fileName);
	    }else{
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
    private int getFileType(String fileName){
	String[] buffer=fileName.split("\\.");
	if(buffer[buffer.length-1].equals("txt")) return 0;
	else if(buffer[buffer.length-1].equals("pdf")) return 1;
	else if(buffer[buffer.length-1].equals("jpg")||buffer[buffer.length-1].equals("png")) return 2;
	else if(buffer[buffer.length-1].equals("mp3")) return 3;
	else if(buffer[buffer.length-1].equals("avi")) return 4;
	else return -1;
    }
    private void getFiles(File[] files){
	items = new ArrayList<String>();
	items.add("Back To Previous Directory");
	for(File file : files){
	    if(file.getPath().equals("/root")) continue;
	    items.add(file.getPath());
	}
	ArrayAdapter<String> fileList = new ArrayAdapter<String>(this,R.layout.rowfolder, items);
	setListAdapter(fileList);
    }
} 
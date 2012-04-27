package com.edu.usc.ee579;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class EE579Activity extends Activity implements ChannelListener{
    /** Called when the activity is first created. */

    private WifiP2pManager manager;
    private Channel channel;
    private BroadcastReceiver receiver;
    private final IntentFilter intentFilter = new IntentFilter();
    private boolean isWifiP2pEnabled = false;
    Context CONTEXT=this;
    private boolean retryChannel=false;
    public final static int BYTESPERCHUNK=100000;
    static HashMap<String, String> allFileList= new HashMap<String, String>();
    static HashMap<String, Integer> numOfChunks= new HashMap<String, Integer>();
    static String fileNeeded=new String();
    
    //private WifiP2pDevice device;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        File file= new File("/sdcard/ee579");
        if(!file.exists()) file.mkdirs();
        initialization();
        fileNeeded=getFileNeeded();
        //register for the events we want to capture 
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        
        //create necessary manager and channel
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
        //searchPeer();
    }
    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
    
    //create option menu. now there is only a close button. may add more in the future.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menuitem, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit: 
        	disconnect();
        	cancelDisconnect();
        	unregisterReceiver(receiver);
        	this.finish();
        	System.exit(0);
        	return true;
            case R.id.folder:
        	Intent browseFolder= new Intent(this,BrowserFolder.class);
        	startActivity(browseFolder);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //indicate the wifi direct state
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
        return;
    }
    
    //to use a toast to show message on screen 
    public void showMessage(String str){
	Toast.makeText(CONTEXT, str,Toast.LENGTH_SHORT).show();
    }
    
    //function to be called when search button is clicked.
    public void searchButton(View view){
	searchPeer();
	return;
    }
    
    //function to perform the wifi direct search
    public void searchPeer(){
	//check if wifi direct is enabled
        if(!isWifiP2pEnabled){
            new AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_launcher)
            .setTitle("WiFi Direct is Disabled!")
            .setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS)); 
                }
            }).show();
            return;
        }
        if(fileNeeded==null){
            showMessage("You have ALL files updated. Don't need to tranfer anymore.");
            return;
        }
/*        DeviceDetailFragment devicefragment = (DeviceDetailFragment)getFragmentManager().findFragmentById(R.id.devicedetail);
	if(devicefragment.device!=null&&devicefragment.isConnected){
	    showMessage("Please disconnect the current connection first.");
	    return;
	}*/
        //use fragment class to display all devices
	final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
	                .findFragmentById(R.id.devicelist);
	fragment.onInitiateDiscovery();
	fragment.getView().setVisibility(View.VISIBLE);
	manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
             @Override
             public void onSuccess() {
                 //Toast.makeText(CONTEXT, "Searching",Toast.LENGTH_SHORT).show();
                 return;
             }
             @Override
             public void onFailure(int reasonCode) {
                 Toast.makeText(CONTEXT, "Search Failed: "+reasonCode,Toast.LENGTH_SHORT).show();
                 return;
             }
         });
    }

    //map the status code with words
    public static String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }
    
    // Round up division result
    public static int divRoundUp(int n,int s){
	return (((n) / (s)) + ((((n) % (s)) > 0) ? 1 : 0));
    }
    
    //show device info on screen
    public void updateThisDevice(WifiP2pDevice device) {
        TextView view = (TextView)findViewById(R.id.mystatus);
        view.setText("My Name: "+device.deviceName+"\nMy Address: "+device.deviceAddress+"\nMy Status: "+getDeviceStatus(device.status));
        return;
    }
    
    //wifi direct connect function
    public void connect(WifiP2pConfig config){
	 manager.connect(channel, config, new ActionListener() {
	     @Override
	     public void onSuccess() {
		 // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
	     }

	     @Override
	     public void onFailure(int reason) {
		 showMessage("Connect failed: "+reason);
	     }
	 });           
	 return;
    }
 
    //wifi direct disconnect function
    public void disconnect(){
        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.devicedetail);
        fragment.blockDetail();
        updateRecord();
        manager.removeGroup(channel, new ActionListener() {
            @Override
            public void onFailure(int reasonCode) {
                showMessage("Disconnect failed. Reason :" + reasonCode);
            }

            @Override
            public void onSuccess() {
                //fragment.getView().setVisibility(View.GONE); 
        	showMessage("Disconnected.");
            }
        });
        return;
    }
    
    //prevent channel lose
    @Override
    public void onChannelDisconnected() {
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
        return;	
    }
    
    //cancel ongoing connect action
    public void cancelDisconnect() {
        if (manager != null) {
            final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                    .findFragmentById(R.id.devicedetail);
            if (fragment.device == null
                    || fragment.device.status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.device.status == WifiP2pDevice.AVAILABLE
                    || fragment.device.status == WifiP2pDevice.INVITED) {

                manager.cancelConnect(channel, new ActionListener() {

                    @Override
                    public void onSuccess() {
                        showMessage("Aborting connection");
                        return;
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        showMessage("Connect abort request failed. Reason Code: " + reasonCode);
                    }
                });
            }
        }
        return;
    }

    
/* The following part is pure Hash Table solution.
 * It will maintain a HashTable for the Files it has and each File also has a HashTable of chunk list.
 * 
 * 
 * 
    static HashMap<String, HashMap<String, Boolean>> availableFileChunks= new HashMap<String, HashMap<String, Boolean>>();
    static HashMap<String, HashMap<String, Boolean>> neededFileChunks= new HashMap<String, HashMap<String, Boolean>>();
    void initialization(){
	File listFile=new File("/sdcard/ee579filelist.txt");
	File recordFile=new File("/sdcard/ee579record.txt");
	try {
	    BufferedReader inputReader = new BufferedReader(new FileReader(listFile));
	    String buffer = new String(); 
	    while((buffer=inputReader.readLine())!=null){  
		String [] fileInfo= buffer.split(",");		
		allFileList.put(fileInfo[0], fileInfo[1]);
		int num=divRoundUp(Integer.parseInt(fileInfo[2]),BYTESPERCHUNK);
		numOfChunks.put(fileInfo[0],num);
	    }
	    inputReader.close();
	    inputReader=new BufferedReader(new FileReader(recordFile));
	    while((buffer=inputReader.readLine())!=null){  
		String [] fileInfo= buffer.split(",");		
		if((buffer=inputReader.readLine())!=null){
		    HashMap<String, Boolean> chunkMap= new HashMap<String, Boolean>();
		    String [] chunkNum= buffer.split(",");
		    for(int i=0;i<chunkNum.length;i++){
			chunkMap.put(chunkNum[i], true);
		    }
		    availableFileChunks.put(fileInfo[0], chunkMap);
		}
	    }
	    inputReader.close();
	    Set<String>files= allFileList.keySet();
	    Iterator<String> it=files.iterator();
	    while(it.hasNext()){
		buffer=it.next();
		if(availableFileChunks.get(buffer)==null){
		    HashMap<String, Boolean> chunkMap= new HashMap<String, Boolean>();
		    for(int i=0;i<numOfChunks.get(buffer);i++){
			chunkMap.put(new Integer(i).toString(), true);
		    }
		    neededFileChunks.put(buffer, chunkMap);
		}else{
		    HashMap<String, Boolean> chunkMap=availableFileChunks.get(buffer);
		    HashMap<String, Boolean> neededChunkMap= new HashMap<String, Boolean>();
		    for(int i=0;i<numOfChunks.get(buffer);i++){
			if(chunkMap.get(new Integer(i).toString())==null){
			    neededChunkMap.put(new Integer(i).toString(), true);
			}
		    } 
		    neededFileChunks.put(buffer, neededChunkMap);
		}
	    }
	} catch (IOException e) {
	    showMessage("IO Error.");
	}
	
    }
    public static String getFileNeeded(){
	String result=new String();
	Set<String> files=neededFileChunks.keySet();
	if(files.isEmpty()) return null;
	Iterator<String> it=files.iterator();
	int i=0;
	while(it.hasNext()){
	    if(i++!=0) result+=",";
	    String buffer=it.next();
	    result+=buffer+",";
	    HashMap<String, Boolean> neededChunkMap=neededFileChunks.get(buffer);
	    Set<String> chunks=neededChunkMap.keySet();
	    Iterator<String> chunkit=chunks.iterator();
	    int j=0;
	    while(chunkit.hasNext()){
		if(j++!=0) result+="+";
		result+=chunkit.next();
	    }
	}
	return result;	
    }
    
    public void updateRecord(){
	File recordFile=new File("/sdcard/ee579record.txt");
	try {
	    BufferedWriter outputWriter = new BufferedWriter(new FileWriter(recordFile,false));
	    Set<String>files= availableFileChunks.keySet();
	    Iterator<String> it=files.iterator();
	    while(it.hasNext()){
		outputWriter.write(it.next()+"\n");
		HashMap<String, Boolean> chunkMap=availableFileChunks.get(it.next());
		Set<String> chunks= chunkMap.keySet();
		Iterator<String> chunkit=chunks.iterator();
		String chunkList=new String();
		int i=0;
		while(chunkit.hasNext()){
		    if(i++!=0) chunkList+=",";
		    chunkList+=chunkit.next();
		}
		outputWriter.write(chunkList+"\n");
	    }
	    outputWriter.flush();
	    outputWriter.close();
	} catch (IOException e) {
	    showMessage("IO Error.");
	}
    }*/

    /* The following part is a Hash Table + BitMap solution.
     * A hash table maintain the info of all available files and each file maintain its chunk list using a bitmap.
     * */
    public static HashMap<String, BitMap> availableChunkMap= new HashMap<String, BitMap>();
    public static HashMap<String, BitMap> neededChunkMap= new HashMap<String, BitMap>();
    void initialization(){
	File listFile=new File("/sdcard/ee579filelist.txt");
	File recordFile=new File("/sdcard/ee579bitmaprecord.txt");
	try {
	    if(!listFile.exists()){
		showMessage("Fatal Error: Config file not found.");
		return;
	    }
	    BufferedReader inputReader = new BufferedReader(new FileReader(listFile));
	    String buffer = new String(); 
	    while((buffer=inputReader.readLine())!=null){  
		String [] fileInfo= buffer.split(",");		
		allFileList.put(fileInfo[0], fileInfo[1]);
		int num=divRoundUp(Integer.parseInt(fileInfo[2]),BYTESPERCHUNK);
		numOfChunks.put(fileInfo[0],num);
		File oneFile= new File("/sdcard/ee579/"+fileInfo[1]);
		if(oneFile.exists()){
		    BitMap chunkMap= new BitMap(num);
		    for(int i=0;i<num;i++) chunkMap.Mark(i);
		    availableChunkMap.put(fileInfo[0], chunkMap);
		}
	    }
	    inputReader.close();
	    recordFile.createNewFile();
	    inputReader=new BufferedReader(new FileReader(recordFile));
	    while((buffer=inputReader.readLine())!=null){  
		String [] fileInfo= buffer.split(",");	
		if(availableChunkMap.get(fileInfo[0])!=null){
		    buffer=inputReader.readLine();
		    continue;
		}
		if((buffer=inputReader.readLine())!=null){		    
		    BitMap chunkMap= new BitMap(buffer);
		    if(chunkMap.length()!=numOfChunks.get(fileInfo[0])){
			showMessage("Error: BitMap length not correct");
			return;
		    }
		    availableChunkMap.put(fileInfo[0], chunkMap);
		}
	    }
	    inputReader.close();
	    Set<String>files= allFileList.keySet();
	    Iterator<String> it=files.iterator();
	    while(it.hasNext()){
		buffer=it.next();
		if(availableChunkMap.get(buffer)==null){
		    BitMap chunkMap=new BitMap(numOfChunks.get(buffer));
		    for(int i=0;i<numOfChunks.get(buffer);i++){
			chunkMap.Mark(i);
		    }
		    neededChunkMap.put(buffer, chunkMap);
		}else{
		    BitMap chunkMap=availableChunkMap.get(buffer);
		    if(chunkMap.numMarked()==numOfChunks.get(buffer)) continue;
		    BitMap neededChunk= new BitMap(numOfChunks.get(buffer));
		    for(int i=0;i<numOfChunks.get(buffer);i++){
			if(!chunkMap.Test(i)){
			    neededChunk.Mark(i);
			}
		    } 
		    neededChunkMap.put(buffer, neededChunk);
		}
	    }
	} catch (IOException e) {
	    showMessage("IO Error: "+e.toString());
	}	
    }
    
    public static String getFileNeeded(){
	String result=new String();
	Set<String> files=neededChunkMap.keySet();
	if(files.isEmpty()) return null;
	Iterator<String> it=files.iterator();
	int i=0;
	while(it.hasNext()){
	    if(i++!=0) result+=",";
	    String buffer=it.next();
	    result+=buffer+",";
    	    result+=neededChunkMap.get(buffer).toString();
	}
	return result;	
    }
    
    public static void updateRecord(){
	File recordFile=new File("/sdcard/ee579bitmaprecord.txt");
	try {
	    BufferedWriter outputWriter = new BufferedWriter(new FileWriter(recordFile,false));
	    Set<String>files= availableChunkMap.keySet();
	    Iterator<String> it=files.iterator();
	    while(it.hasNext()){
		String buffer=it.next();
		outputWriter.write(buffer+","+allFileList.get(buffer)+"\n");
		BitMap chunkMap=availableChunkMap.get(buffer);
		outputWriter.write(chunkMap.toString()+"\n");
	    }
	    outputWriter.flush();
	    outputWriter.close();
	} catch (IOException e) {
	    Log.d("EE579","IO Error.");
	}
    }
    
    /* The following part is Bloom Filter implementation. 
     * */
/*    public static BloomFilter availableChunks=new BloomFilter();
    void initialization(){
	File listFile=new File("/sdcard/ee579filelist.txt");
	try {
	    if(!listFile.exists()){
		showMessage("Fatal Error: Config file not found.");
		return;
	    }
	    BufferedReader inputReader = new BufferedReader(new FileReader(listFile));
	    String buffer = new String(); 
	    while((buffer=inputReader.readLine())!=null){  
		String [] fileInfo= buffer.split(",");		
		allFileList.put(fileInfo[0], fileInfo[1]);
		int num=divRoundUp(Integer.parseInt(fileInfo[2]),BYTESPERCHUNK);
		numOfChunks.put(fileInfo[0],num);
		File oneFile= new File("/sdcard/ee579/"+fileInfo[1]);
		if(oneFile.exists()){
		    for(int i=0;i<num;i++){
			availableChunks.mark(fileInfo[0]+"-"+i);
		    }
		}
	    }
	    inputReader.close();
	    File oneFile= new File("/sdcard/ee579/tmp");
	    if(oneFile.exists()){
		File[] files=oneFile.listFiles();
		for(int i=0;i<files.length;i++){
		    String fileName= files[i].getName().split("\\.")[0];
		    availableChunks.mark(fileName);
		}
	    }
	}catch(IOException e){
	    showMessage("IO Error: "+e.toString());
	}
    }
    public static String getFileNeeded(){
	String result=new String();
	Set<String>files= allFileList.keySet();
	Iterator<String> it=files.iterator();
	while(it.hasNext()){
	    String buffer=it.next();
	    for(int i=0;i<numOfChunks.get(buffer);i++){
		if(!availableChunks.test(buffer+"-"+i)){
		    result+=buffer+"-"+i+",";
		}
	    } 
	}
	return result;	
    }*/
}
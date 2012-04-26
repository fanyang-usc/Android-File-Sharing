package com.edu.usc.ee579;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class Client extends AsyncTask<Void, Void, String>{
    private OutputStream outToServer = null;
    private InputStream inFromServer= null;
    private Socket clientSocket = null;
    private int numberOfTry=0;
    private String serverIP=null;
    private int serverPort=0;
    Handler myHandler=null;

    public Client(String serverIP, int serverPort, Handler myHandler) {
	this.serverIP=serverIP;
	this.serverPort=serverPort;
	this.myHandler=myHandler;
    }
    
    private boolean initializeClient(String serverIP, int serverPort) {
	try {
	    //create socket and connect to server. try several times in case client start first.
	    numberOfTry++;
	    clientSocket = new Socket(serverIP, serverPort); 
	    //showMessage("Client: Socket created.");
	    outToServer = clientSocket.getOutputStream();   
	    inFromServer =  clientSocket.getInputStream();
	    return true;
	} catch (UnknownHostException e) {                   
	    if(numberOfTry<10) initializeClient(serverIP, serverPort);
	    else showMessage("Client Error: Cannot Connect to the Server after 10 attempts");
	    return false;
	} catch (IOException e) {
	    showMessage("Client Error: IO Error.");
	    return false;
	}
    }

    
    public void showMessage(String str){
	//str.replace('\n', '\0');
	Message msg=new Message();
	msg.obj=(Object)str;
	myHandler.sendMessage(msg);
	return;
    }
    
    public boolean closeConnection() {
	try {
	    clientSocket.close();
	    return true;
	}catch(IOException e) {
	    showMessage("Client Error: IO Error.");
	    return false;
	}
    }
    
    
    //send message to server and show on screen
    public void sendMessage(int msgType, int fileNum, int chunkNum, String msg){ 
	if(msgType==1){
	    String[] buffer=msg.split(",");
	    String message="Client: The files I need are: ";
	    int j=0;
	    for(int i=0;i<buffer.length;i++){
		if(j++!=0) message+=",";
		message+=buffer[i++];
	    }
	    showMessage(message);
	}
	Packet packet=new Packet(msgType, fileNum, chunkNum, msg.getBytes());
	if(!packet.sendPacket(outToServer)){
	    showMessage("Client Error: Send Message Error.");
	}
    }
    
    //read from server and cast into packet
    public Packet readMessage(){  
	Packet packet=new Packet();
	if(packet.readPacket(inFromServer)){
	    return packet;
	}
	else{
	    showMessage("Client Error: Read Message Error.");
	    return null;
	}
    }
    //communicate with server in background. check files and chunks needed and whether the server has them.
    @Override
    protected String doInBackground(Void... arg0) {
	if(!initializeClient(serverIP, serverPort)){
	    showMessage("Client Error.");
	    return null;
	}
	//checkFileNeed()
	if(EE579Activity.fileNeeded!=null&&EE579Activity.fileNeeded!="")
	    sendMessage(1,0,0,EE579Activity.fileNeeded);
	else{
	    showMessage("No file need.");
	    sendMessage(7,0,0,"Exit");
	    closeConnection();
	    return null;
	}
	while(true){
	    Packet packet=readMessage();
	    if(packet==null){
		showMessage("No Packet Available.");
		break;
	    }
	    if(packet.getMessageType()==2){	
		int fileNum=packet.getFileNum();
		int chunkNum=packet.getChunkNum();
		String fileNumStr=new Integer(fileNum).toString();
		String chunkNumStr=new Integer(chunkNum).toString();
		showMessage("Client: Get file No."+fileNum+" chunk No."+chunkNum);
		File tmpFolder= new File("/sdcard/ee579/tmp/");
		File tmpFile= new File("/sdcard/ee579/tmp/"+fileNumStr+"-"+chunkNumStr+".tmp");
		try {
		    tmpFolder.mkdirs();
		    tmpFile.createNewFile();
		    FileOutputStream fileOut= new FileOutputStream(tmpFile);
		    fileOut.write(packet.getMessageByte());
		    fileOut.flush();
		    fileOut.close();
		} catch (IOException e) {
		    showMessage("IO ERROR"+e.toString());
		    break;
		}
		int numOfAvailableChunks=0;
		/*HashMap<String,Boolean> chunkMap=EE579Activity.availableFileChunks.get(fileNumStr);		
		if(chunkMap==null){
		    numOfAvailableChunks=1;
		    HashMap<String,Boolean> newChunkMap=new HashMap<String,Boolean>();
		    newChunkMap.put(chunkNumStr, true);
		    EE579Activity.availableFileChunks.put(fileNumStr, newChunkMap);
		}else{
		    chunkMap.put(chunkNumStr, true);
		    EE579Activity.availableFileChunks.remove(fileNumStr);
		    EE579Activity.availableFileChunks.put(fileNumStr, chunkMap);
		    numOfAvailableChunks=chunkMap.size();
		}
		HashMap<String,Boolean> needChunkMap=EE579Activity.neededFileChunks.get(fileNumStr);
		EE579Activity.neededFileChunks.remove(fileNumStr);
		needChunkMap.remove(chunkNumStr);
		if(!needChunkMap.isEmpty())
		    EE579Activity.neededFileChunks.put(fileNumStr, needChunkMap);
		*/
		BitMap chunkMap=EE579Activity.availableChunkMap.get(fileNumStr);
		if(chunkMap==null){
		    numOfAvailableChunks=1;
		    BitMap newChunkMap=new BitMap(EE579Activity.numOfChunks.get(fileNumStr));
		    newChunkMap.Mark(chunkNum);
		    EE579Activity.availableChunkMap.put(fileNumStr, newChunkMap);
		}else{
		    chunkMap.Mark(chunkNum);
		    numOfAvailableChunks=chunkMap.numMarked();
		}
		BitMap needChunkMap=EE579Activity.neededChunkMap.get(fileNumStr);
		needChunkMap.Clear(chunkNum);
		if(needChunkMap.numMarked()==0){
		    EE579Activity.neededChunkMap.remove(fileNumStr);
		}
		
		EE579Activity.getFileNeeded();
		int numOfChunks=EE579Activity.numOfChunks.get(fileNumStr);
		if(numOfAvailableChunks==numOfChunks){
		    CombineFile cf=new CombineFile(fileNumStr);
		    cf.start();
		}
		EE579Activity.updateRecord();
	    }else if(packet.getMessageType()==7&&packet.getMessage().equals("None")){
		showMessage("Server: Don't have any file available");
		break;
	    }else if(packet.getMessageType()==7&&packet.getMessage().equals("Exit")){
		showMessage("Server: All available files have been transmitted.");
		break;
	    }else{
		showMessage("Error: Wrong Packet from Server.");
		break;
	    }
	}
	closeConnection();
	return null;
    }
    
/*    public void combineFiles(String fileNumStr){	
	try {
	    File file= new File("/sdcard/ee579/"+EE579Activity.allFileList.get(fileNumStr));
	    FileOutputStream out=new FileOutputStream(file);
	    int count=EE579Activity.numOfChunks.get(fileNumStr);
	    int i=0;
	    while(i<count){
		File tmpFile= new File("/sdcard/ee579/tmp/"+fileNumStr+"-"+i+".tmp");
		FileInputStream in=new FileInputStream(tmpFile);
		int buffer;
		while((buffer=in.read())!=-1)
		    out.write(buffer);
		in.close();
		tmpFile.deleteOnExit();
		i++;
	    }
	    out.flush();
	    out.close();
	} catch (IOException e) {
	    showMessage("IO Error.");
	}
    }*/
    
    @Override
    protected void onPreExecute() {
        showMessage("Client: Started.");
    }
    @Override
    protected void onPostExecute(String result) {
	showMessage("Client: Finished.");
    }

}
package com.edu.usc.ee579;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

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
    public void sendMessage(int msgType, int fileNum, int chunkNum, byte[] msg){ 
	if(msgType==1){
	    String msgStr=new String(msg);
	    String[] buffer=msgStr.split(",");
	    String message="Client: The files I need are: ";
	    int j=0;
	    for(int i=0;i<buffer.length;i++){
		if(j++!=0) message+=",";
		message+=buffer[i++];
	    }
	    showMessage(message);
	}else if(msgType==7&&new String(msg).equals("None")){
	    showMessage("Client: Don't have any file needed.");
	}else if(msgType==7&&new String(msg).equals("Nofile")){
	    showMessage("Client: Don't have any file available to transmit.");
	}else if(msgType==7&&new String(msg).equals("Exit")){
	    showMessage("Client: All available files have been transmitted.");
	}else if(msgType==2){
	    showMessage("Client: Transfering File No."+fileNum+" Chunk No."+chunkNum);
	}
	Packet packet=new Packet(msgType, fileNum, chunkNum, msg);
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
	    sendMessage(1,0,0,EE579Activity.fileNeeded.getBytes());
	else{
	    sendMessage(7,0,0,"None".getBytes());
	}
	while(true){
	    Packet packet=readMessage();
	    if(packet==null){
		showMessage("No Packet Available.");
		break;
	    }
	    if(packet.getMessageType()==1){
		showMessage("Server: The Files I need are "+packet.getMessage());
		ArrayList<String> result=Query.checkAvailablility(packet.getMessage());
		if(result==null){
		    sendMessage(7,0,0,"Exit".getBytes());
		    break;
		}
		else{	    
		    while(result.size()!=0){
			int numOfChunks=result.size();
			String chunkInfo=new String();
			int index=new Random().nextInt(1000000)%numOfChunks;
			chunkInfo=result.get(index);
			result.remove(index);			
			String[] fileDetail= chunkInfo.split("\\,");
			String fileNum=fileDetail[0];
			String chunkNum=fileDetail[1];		
			File file= new File("/sdcard/ee579/"+EE579Activity.allFileList.get(fileNum));
			byte[] buffer=new byte[EE579Activity.BYTESPERCHUNK];
			int numOfBytesRead=0;
			try {
			    FileInputStream in=new FileInputStream(file);
			    in.skip(Integer.parseInt(chunkNum)*EE579Activity.BYTESPERCHUNK);
			    numOfBytesRead=in.read(buffer);
			    in.close();
			}catch (FileNotFoundException e) {
			    showMessage("No such File");
			}catch (IOException e) {
			    showMessage("IO Error.");
			}
			if(numOfBytesRead!=EE579Activity.BYTESPERCHUNK){
			    byte[] msg=new byte[numOfBytesRead];
			    for(int i=0;i<numOfBytesRead;i++) msg[i]=buffer[i];
			    sendMessage(2,Integer.parseInt(fileNum),Integer.parseInt(chunkNum),msg);
			}else{
			    sendMessage(2,Integer.parseInt(fileNum),Integer.parseInt(chunkNum),buffer);
			}
		    }
		    sendMessage(7,0,0,"Exit".getBytes());
		    break;
		}
	    }
	    else if(packet.getMessageType()==2){	
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
		
		/*This part is the corresponding parts if you choose the Pure Hash Table implementation.
		 * 
		HashMap<String,Boolean> chunkMap=EE579Activity.availableFileChunks.get(fileNumStr);		
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
		
		/*HashTable+Bitmap solution*/
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
		
		//common codes for both implementation.
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
    
    @Override
    protected void onPreExecute() {
        showMessage("Client: Started.");
    }
    @Override
    protected void onPostExecute(String result) {
	showMessage("Client: Finished.");
    }

}
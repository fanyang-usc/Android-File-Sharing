package com.edu.usc.ee579;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class Server extends AsyncTask<Void, Void, String> {
    private ServerSocket serverSocket = null;
    private int serverPort = 9777;
    private Socket clientSocket = null;
    private OutputStream outToClient = null;
    private InputStream inFromClient = null;
    Handler myHandler=null;

    public Server(Handler myHandler) {
	this.myHandler=myHandler;
    }

    public Server(int serverPort,Handler myHandler) {
	this.serverPort = serverPort;
	this.myHandler=myHandler;
    }

    private boolean initializeServer(int serverPort) {
	try {
	    //create serversocket
	    serverSocket = new ServerSocket(serverPort); 
	    //showMessage("Server: Socket Created.");
	} catch (IOException e) {
	    showMessage("Server Error: Could not listen on port: "+serverPort);
	    return false;
	} 
	try {
	    //showMessage("Server: Waiting for Client.");
	    clientSocket = serverSocket.accept();
	    showMessage("Server: Client Accepted");
	} catch (IOException e) {
	    showMessage("Server Error: Accept failed.");
	    return false;
	}
	try {
	    //get streams for socket.	
	    outToClient = clientSocket.getOutputStream();
	    inFromClient = clientSocket.getInputStream();
	} catch (IOException e) {
	    showMessage("Server Error: I/O error for the connection");
	    return false;
	} 
	return true;
    }

    public boolean closeConnection() {
	try {
	    serverSocket.close();
	    return true;
	}catch(IOException e) {
	    showMessage("Server Error: IO Error.");
	    return false;
	}
    }
    
    //use handler to show message on UI Thread
    public void showMessage(String str){
	//str.replace('\n', '\0');
	Message msg=new Message();
	msg.obj=(Object)str;
	myHandler.sendMessage(msg);
	return;
    }

    //function for file transfer
    public void sendMessage(int msgType, int fileNum, int chunkNum, byte[] msg){
	if(msgType==7&&new String(msg).equals("None")){
	    showMessage("Server: Don't have any file available.");
	}else if(msgType==7&&new String(msg).equals("Exit")){
	    showMessage("Server: All available files have been transmitted.");
	}else if(msgType==2){
	    showMessage("Server: Transfering File No."+fileNum+" Chunk No."+chunkNum);
	}else if(msgType==1){
	    String msgStr=new String(msg);
	    String[] buffer=msgStr.split(",");
	    String message="Server: The files I need are: ";
	    int j=0;
	    for(int i=0;i<buffer.length;i++){
		if(j++!=0) message+=",";
		message+=buffer[i++];
	    }
	    showMessage(message);
	}
	Packet packet=new Packet(msgType, fileNum, chunkNum, msg);
	if(!packet.sendPacket(outToClient)){
	    showMessage("Server Error: Send Message Error.");
	}
    }

    //read message from client and cast into packet.
    public Packet readMessage(){
	Packet packet=new Packet();
	if(packet.readPacket(inFromClient)){
	    return packet;
	}
	else{
	    showMessage("Server Error: Read Message Error.");
	    return null;
	}
    }

    //use asynctask to do the client/server. All functions are done in background
    @Override
    protected String doInBackground(Void... arg0) {
	//initialize server
	if(!initializeServer(serverPort)){
	    showMessage("Server Error.");
	    return null;
	}
	while(true){
	    //use packet class to build packet and read a packet from client
	    Packet packet=readMessage();
	    if(packet==null){
		showMessage("Server Error.");
		break;
	    }
	    //respond accordingly to the message get from client
	    if(packet.getMessageType()==1){
		showMessage("Client: The Files I need are "+packet.getMessage());
		ArrayList<String> result=Query.checkAvailablility(packet.getMessage());
		if(result==null){
		    sendMessage(7,0,0,"None".getBytes());
		    break;
		}
		else{	    
		    while(result.size()!=0){
			int numOfChunks=result.size();
			String chunkInfo=new String();
			int index=new Random().nextInt(1000000)%numOfChunks;
			chunkInfo=result.get(index);
			/*use this if Bloom Filter
			File file= new File("/sdcard/ee579/"+chunkInfo);
			*/
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
		    if(EE579Activity.fileNeeded!=null&&EE579Activity.fileNeeded!="")
			sendMessage(1,0,0,EE579Activity.fileNeeded.getBytes());
		    else{
			sendMessage(7,0,0,"Exit".getBytes());
		    }
		}
	    }else if(packet.getMessageType()==2){	
		int fileNum=packet.getFileNum();
		int chunkNum=packet.getChunkNum();
		String fileNumStr=new Integer(fileNum).toString();
		String chunkNumStr=new Integer(chunkNum).toString();
		showMessage("Server: Get file No."+fileNum+" chunk No."+chunkNum);
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
		if(EE579Activity.fileNeeded!=null&&EE579Activity.fileNeeded!="")
		    sendMessage(1,0,0,EE579Activity.fileNeeded.getBytes());
		else{
		    sendMessage(7,0,0,"Exit".getBytes());
		}
	    }else if(packet.getMessageType()==7&&packet.getMessage().equals("Exit")){
		showMessage("Client: All available files have been transmitted.");
		break;
	    }else{
		showMessage("Error: Wrong Packet from Client.");
		break;
	    }
	}
	closeConnection();
	EE579Activity.updateRecord();
	return null;
    }

    @Override
    protected void onPreExecute() {
	showMessage("Server: Started.");
    }
    protected void onPostExecute(String result) {
	showMessage("Server: Finished.");
    }
}
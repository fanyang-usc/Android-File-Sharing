package com.edu.usc.ee579;

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
    private ArrayList<Integer> availableFileList=new ArrayList<Integer>();
    private ArrayList<Integer> availableChunkList=new ArrayList<Integer>();
    
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
    
    //send message to server and show on screen
    public void sendMessage(int msgType, int fileNum, int chunkNum, String msg){ 
	if(msgType==1){
	    showMessage("Client: The files I need are: "+msg);
	}else if(msgType==3){
	    showMessage("Client: The chunks I need in File No."+fileNum+" are: "+msg);
	}else if(msgType==5){
	    showMessage("Client: I need File No."+fileNum+" Chunk No. "+chunkNum);
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
    
    //communicate with server in background. check files and chunks needed and whether the server has them.
    @Override
    protected String doInBackground(Void... arg0) {
	if(!initializeClient(serverIP, serverPort)){
	    showMessage("Client Error.");
	    return null;
	}
	//checkFileNeed()
	sendMessage(1,0,0,"3,5,8");
	while(true){
	    Packet packet=readMessage();
	    if(packet==null){
		showMessage("Client Error.");
		break;
	    }
	    if(packet.getMessageType()==2){
		showMessage("Server: The files I have are: "+packet.getMessage());
		//if server has no file the client need, just disconnect.
		if(packet.getMessage()=="None")
		    sendMessage(7,0,0,"Exit");
		else{
		    //get all available file list
		    String[] buffer=packet.getMessage().split(",");
		    //choose one file randomly and ask for chunks, put others in a list for future use
		    int index=new Random(1).nextInt()%buffer.length;
		    for(int i=0;i<buffer.length;i++){
			if(i==index){
			    //check chunk need
			    sendMessage(3,Integer.parseInt(buffer[i]),0,"242,234");
			}else{
			    availableFileList.add(Integer.parseInt(buffer[i]));
			}
		    }    
		}
	    }else if(packet.getMessageType()==4){
		showMessage("Server: The chunks I have in File No."+packet.getFileNum()+" are: "+packet.getMessage());
		if(packet.getMessage()=="None"){
		    if(!availableFileList.isEmpty()){
			int index=new Random(1).nextInt()%availableFileList.size();
			//check chunk need
			sendMessage(3,availableFileList.get(index),0,"Transfer");
			availableFileList.remove(index);
		    }else{
			sendMessage(7,0,0,"Exit");
			break;
		    }
		}
		else{
		    String[] buffer=packet.getMessage().split(",");
		    int index=new Random(1).nextInt()%buffer.length;
		    for(int i=0;i<buffer.length;i++){
			if(i==index){
			    sendMessage(5,packet.getFileNum(),Integer.parseInt(buffer[i]),"Transfer");
			}else{
			    availableChunkList.add(Integer.parseInt(buffer[i]));
			}
		    }    
		}
	    }else if(packet.getMessageType()==6){
		//check if the transfer is successful
		showMessage("Client: geting file.");
		//if successful try to transfer next chunk available.
		if(!availableChunkList.isEmpty()){
		    int index=new Random(1).nextInt()%availableChunkList.size();
		    sendMessage(5,packet.getFileNum(),availableChunkList.get(index),"Transfer");
		    availableChunkList.remove(index);
		}else if(!availableFileList.isEmpty()){
		    //if no chunks left see if the server has other files available.
		    int index=new Random(1).nextInt()%availableFileList.size();
		    //check chunk need
		    sendMessage(3,availableFileList.get(index),0,"Transfer");
		    availableFileList.remove(index);
		}else{
		    //disconnect when nothing to transfer.
		    sendMessage(7,0,0,"Exit");
		    break;
		}

	    }else{
		showMessage("Wrong Packet.");
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
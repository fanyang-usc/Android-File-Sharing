package com.edu.usc.ee579;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

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
    
    //use asynctast to do the client/server. All functions are done in background
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
		showMessage("Client: The Files I need are: "+packet.getMessage());
		String result="3,4,5";//Query.checkAvailablility(packet.getMessage(),fileTable);
		if(result==null)
		    sendMessage(2,0,0,"None");
		else
		    sendMessage(2,0,0,result);
	    }else if(packet.getMessageType()==3){
		int index=packet.getFileNum();
		showMessage("Client: The Chunks I need in File No."+index+" are: "+packet.getMessage());
		String result="242";//Query.checkAvailablility(packet.getMessage(),fileTable);
		if(result==null)
		    sendMessage(4,packet.getFileNum(),0,"None");
		else
		    sendMessage(4,packet.getFileNum(),0,result);
	    }else if(packet.getMessageType()==5){
		showMessage("Client: I need File No."+packet.getFileNum()+" Chunk No. "+packet.getChunkNum());
		sendFile(6,packet.getFileNum(),packet.getChunkNum(),"Tranfer".getBytes());
	    }else if(packet.getMessageType()==7){
		break;
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
        showMessage("Server: Started.");
    }
    protected void onPostExecute(String result) {
	showMessage("Server: Finished.");
    }

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
    
    //send message to client and show them on screen
    public void sendMessage(int msgType, int fileNum, int chunkNum, String msg){
	if(msgType==2){
	    showMessage("Server: The files I have are: "+msg);
	}else if(msgType==4){
	    showMessage("Server: The chunks I have in File No."+fileNum+" are: "+msg);
	}else if(msgType==6){
	    showMessage("Server: Sending File No."+fileNum+" Chunk No."+chunkNum);
	}else{
	    showMessage("Wrong Message.");
	}
	Packet packet=new Packet(msgType, fileNum, chunkNum, msg.getBytes());
	if(!packet.sendPacket(outToClient)){
	    showMessage("Server Error: Send Message Error.");
	}
    }
    
    //function for file transfer
    public void sendFile(int msgType, int fileNum, int chunkNum, byte[] msg){
	showMessage("Transfering File No."+fileNum+" Chunk No."+chunkNum);
	Packet packet=new Packet(msgType, fileNum, chunkNum, msg);
	packet.sendPacket(outToClient);
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
    
    //use handler to show message on UI Thread
    public void showMessage(String str){
	//str.replace('\n', '\0');
	Message msg=new Message();
	msg.obj=(Object)str;
	myHandler.sendMessage(msg);
	return;
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
}
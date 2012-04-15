package com.edu.usc.ee579;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class Client extends AsyncTask<Void, Void, String>{
    private OutputStream outToServer = null;
    private BufferedReader inFromServer= null;
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
	    numberOfTry++;
	    clientSocket = new Socket(serverIP, serverPort); 
	    showMessage("Client: Socket created.");
	    outToServer = clientSocket.getOutputStream();   
	    inFromServer =  new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
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
    public void sendMessage(String msg){  
	showMessage("Client: "+msg);
	try {
	    msg+="\n";
	    outToServer.write(msg.getBytes());
	    outToServer.flush();	    
	} catch (IOException e) {
	    showMessage("Client Error: IO Error.");
	} 
	
    }
    public String readMessage(){  
	try {
	    String buffer=inFromServer.readLine();
	    showMessage("Server: "+buffer);
	    return buffer;
	} catch (IOException e) {
	    showMessage("Client Error: IO Error.");
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
    @Override
    protected String doInBackground(Void... arg0) {
	if(!initializeClient(serverIP, serverPort)){
	    showMessage("Client Error.");
	    return null;
	}
	sendMessage("Hello!");
	readMessage();
	sendMessage("Hello!");
	readMessage();
	sendMessage("Hello!");
	readMessage();
	sendMessage("Hello!");
	readMessage();
	sendMessage("Hello!");
	readMessage();
	sendMessage("Hello!");
	readMessage();
	sendMessage("Hello!");
	readMessage();
	sendMessage("Hello!");
	readMessage();
	sendMessage("Hello!");
	readMessage();
	sendMessage("Hello!");
	readMessage();
	sendMessage("Hello!");
	readMessage();
	sendMessage("Hello!");
	readMessage();
	sendMessage("Hello!");
	readMessage();
	closeConnection();
	return null;
    }
    @Override
    protected void onPreExecute() {
        showMessage("Started.");
    }
    @Override
    protected void onPostExecute(String result) {
	showMessage("Finished.");
    }

}
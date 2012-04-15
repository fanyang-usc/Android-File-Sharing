package com.edu.usc.ee579;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    private BufferedReader inFromClient = null;
    Handler myHandler=null;
    @Override
    protected String doInBackground(Void... arg0) {
	if(!initializeServer(serverPort)){
	    showMessage("Server Error.");
	    return null;
	}
	readMessage();
	sendMessage("Hello too!");
	readMessage();
	sendMessage("Hello too!");
	readMessage();
	sendMessage("Hello too!");
	readMessage();
	sendMessage("Hello too!");
	readMessage();
	sendMessage("Hello too!");
	readMessage();
	sendMessage("Hello too!");
	readMessage();
	sendMessage("Hello too!");
	readMessage();
	sendMessage("Hello too!");
	readMessage();
	sendMessage("Hello too!");
	readMessage();
	sendMessage("Hello too!");
	
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
    
    //functions from diagnostic test to initialize a server
    public Server(int serverPort,Handler myHandler) {
      this.serverPort = serverPort;
      this.myHandler=myHandler;
    }
    private boolean initializeServer(int serverPort) {
	try {
	    serverSocket = new ServerSocket(serverPort); 
	    showMessage("Server: Socket Created.");
	} catch (IOException e) {
	    showMessage("Server Error: Could not listen on port: "+serverPort);
	    return false;
	} 
	try {
	    showMessage("Server: Waiting for Client.");
	    clientSocket = serverSocket.accept();
	    showMessage("Server: Client Accepted");
	} catch (IOException e) {
	    showMessage("Server Error: Accept failed.");
	    return false;
	}
	try {
	    //get streams for socket and create files to keep data.	
	    outToClient = clientSocket.getOutputStream();
	    inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	} catch (IOException e) {
	    showMessage("Server Error: I/O error for the connection");
	    return false;
	} 
	return true;
    }
    public void sendMessage(String msg){
	showMessage("Server: "+msg);
	msg+="\n"; 
	try {
	    outToClient.write(msg.getBytes());
	    outToClient.flush();
	} catch (IOException e) {
	    showMessage("Server Error: IO Error.");
	}
    }
    public String readMessage(){
	try {
	    String buffer=inFromClient.readLine();
	    showMessage("Client: "+buffer);
	    return buffer;
	} catch (IOException e) {
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
	    serverSocket.close();
	    return true;
	}catch(IOException e) {
	    showMessage("Server Error: IO Error.");
	    return false;
	}
    }
}
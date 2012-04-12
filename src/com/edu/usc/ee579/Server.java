package com.edu.usc.ee579;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class Server extends AsyncTask<Void, Void, String> {
    private ServerSocket serverSocket = null;
    private int serverPort = 9777;
    private Socket clientSocket = null;
    private OutputStream outToClient = null;
    private BufferedReader inFromClient = null;
    private EE579Activity activity;
    private TextView textView=null;
    
    @Override
    protected String doInBackground(Void... arg0) {
	if(!initializeServer(serverPort)){
	    showMessage("Error.");
	    System.exit(-1);
	}
	//showMessage(readMessage());
	Log.d("EE579", readMessage());
	closeConnection();
	return null;
    }
    @Override
    protected void onPreExecute() {
        showMessage("Started.");
    }
    protected void onPostExecute(String result) {
	showMessage("Finished.");
    }

    public Server(TextView textView) {
	this.textView=textView;
    }
    
    //functions from diagnostic test to initialize a server
    public Server(int serverPort,TextView textView) {
      this.serverPort = serverPort;
      this.textView=textView;
    }
    private boolean initializeServer(int serverPort) {
	try {
	    serverSocket = new ServerSocket(serverPort); 
	    //showMessage("Socket Created.");
	} catch (IOException e) {
	    textView.setText("Server Error: Could not listen on port: "+serverPort);
	    return false;
	} 
	try {
	    //showMessage("Waiting for Client.");
	    clientSocket = serverSocket.accept();
	    //showMessage("Client Accepted");
	} catch (IOException e) {
	    textView.setText("Server Error: Accept failed.");
	    return false;
	}
	try {
	    //get streams for socket and create files to keep data.	
	    outToClient = clientSocket.getOutputStream();
	    inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	    //showMessage("Stream Created.");
	} catch (IOException e) {
	    textView.setText("Server Error: I/O error for the connection");
	    return false;
	} 
	return true;
    }
    public void sendMessage(String msg){
	msg+="\n"; 
	try {
	    outToClient.write(msg.getBytes());
	} catch (IOException e) {
	    textView.setText("Server Error: IO Error.");
	}
    }
    public String readMessage(){
	try {
	    return inFromClient.readLine();
	} catch (IOException e) {
	    return null;
	}
    }
    public void showMessage(String str){
	textView.setText(textView.getText()+"\nServer: "+str);
	return;
    }
    public boolean closeConnection() {
	try {
	    serverSocket.close();
	    return true;
	}catch(IOException e) {
	    textView.setText("Server Error: IO Error.");
	    return false;
	}
    }
}
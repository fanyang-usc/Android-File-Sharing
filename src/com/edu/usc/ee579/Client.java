package com.edu.usc.ee579;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.widget.TextView;

public class Client extends AsyncTask<Void, Void, String>{
    private OutputStream outToServer = null;
    private BufferedReader inFromServer= null;
    private Socket clientSocket = null;
    private EE579Activity activity;
    private int numberOfTry=0;
    private String serverIP=null;
    private int serverPort=0;
    private TextView textView=null;
    
    public Client(String serverIP, int serverPort, TextView textView) {
	this.serverIP=serverIP;
	this.serverPort=serverPort;
	this.textView=textView;
    }
    private boolean initializeClient(String serverIP, int serverPort) {
	try {
	    numberOfTry++;
	    clientSocket = new Socket(serverIP, serverPort); 
	    //showMessage("Socket created.");
	    outToServer = clientSocket.getOutputStream();   
	    inFromServer =  new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	    return true;
	} catch (UnknownHostException e) {                   
	    if(numberOfTry<10) initializeClient(serverIP, serverPort);
	    else textView.setText("Client: Cannot Connect to the Server after 10 attempts");
	    return false;
	} catch (IOException e) {
	    //textView.setText("Client: IO Error.");
	    return false;
	}
    }
    public void sendMessage(String msg){           
	try {
	    msg+="\n";
	    outToServer.write(msg.getBytes());
	    outToServer.flush();
	    //showMessage(msg);
	} catch (IOException e) {
	    textView.setText("Client: IO Error.");
	} 
	
    }
    public String readMessage(){  
	try {
	    return inFromServer.readLine();
	} catch (IOException e) {
	    textView.setText("Client: IO Error.");
	    return null;
	}	
    }
    public void showMessage(String str){
	textView.setText(textView.getText()+"\nClient: "+str);
	return;
    }
    public boolean closeConnection() {
	try {
	    clientSocket.close();
	    return true;
	}catch(IOException e) {
	    textView.setText("Client: IO Error.");
	    return false;
	}
    }
    @Override
    protected String doInBackground(Void... arg0) {
	if(!initializeClient(serverIP, serverPort)){
	    //textView.setText("Client Error.");
	    System.exit(-1);
	}
	sendMessage("Hello.");
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
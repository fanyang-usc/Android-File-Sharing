package com.edu.usc.ee579;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Packet{
    private byte[] msg;
    private int msgType;
    private int msgLength;
    private int totalLength;
    private int fileNum;
    private int chunkNum;
    private byte[] packet;
    private final int MSGTYPELENGTH = 4; //length of the APITYPE field in bytes
    private final int TOTALLENLENGTH = 4; //length of the TOTALLEN field in bytes
    private final int MSGLEN_LENGTH = 4; //length of the MSG_LEN field in bytes
    private final int FILENUMLENGTH =4;
    private final int CHUNKNUMLENGTH =4;
    private final int HEADERLEG=MSGTYPELENGTH+TOTALLENLENGTH+FILENUMLENGTH+CHUNKNUMLENGTH+MSGLEN_LENGTH;

    public Packet(int msgType, int fileNum, int chunkNum, byte[] msg) {
	    this.msg = msg;
	    this.msgType=msgType;
	    this.fileNum=fileNum;
	    this.chunkNum=chunkNum;
	    msgLength = msg.length;
	    totalLength =HEADERLEG + msgLength;
	    packet=new byte[totalLength];
	    byte[] msgTypeByte= intToBytes(msgType);
	    byte[] msgLenByte= intToBytes(msgLength);
	    byte[] totalLenByte= intToBytes(totalLength);
	    byte[] fileNumByte= intToBytes(fileNum);
	    byte[] chunkNumByte= intToBytes(chunkNum);
	    for(int i=0;i<4;i++) packet[i]=msgTypeByte[i];
	    for(int i=0;i<4;i++) packet[i+4]=totalLenByte[i];
	    for(int i=0;i<4;i++) packet[i+8]=msgLenByte[i];
	    for(int i=0;i<4;i++) packet[i+12]=fileNumByte[i];
	    for(int i=0;i<4;i++) packet[i+16]=chunkNumByte[i];
	    for(int i=0;i<msgLength;i++) packet[i+20]=msg[i]; 
    }
    
    public Packet() {
    }

    public int getMessageType() {
	return msgType; 
    }

    public String getMessage() {
	return new String(msg);
    }

    public int getMessageLength() {
	return msgLength;
    }

    public int getTotalLength(){
	return totalLength;
    }
    
    public int getFileNum(){
	return fileNum;
    }
    
    public int getChunkNum(){
	return chunkNum;
    }
    
    public boolean readPacket(InputStream inFromBuffer) {
	try {
	    packet=new byte[100020];
	    int numOfByteRead=inFromBuffer.read(packet);
	    byte[] msgTypeByte= new byte[4];
	    byte[] msgLenByte= new byte[4];
	    byte[] totalLenByte= new byte[4];
	    byte[] fileNumByte= new byte[4];
	    byte[] chunkNumByte= new byte[4];
	    for(int i=0;i<4;i++) msgTypeByte[i]=packet[i];
	    for(int i=0;i<4;i++) totalLenByte[i]=packet[i+4];
	    for(int i=0;i<4;i++) msgLenByte[i]=packet[i+8];
	    for(int i=0;i<4;i++) fileNumByte[i]=packet[i+12];
	    for(int i=0;i<4;i++) chunkNumByte[i]=packet[i+16];
	    msgType=bytesToInt(msgTypeByte);
	    totalLength=bytesToInt(totalLenByte);
	    msgLength=bytesToInt(msgLenByte);
	    fileNum=bytesToInt(fileNumByte);
	    chunkNum=bytesToInt(chunkNumByte);
	    if(numOfByteRead!=totalLength){
		return false;
	    }
	    msg=new byte[msgLength];
	    for(int i=0;i<msgLength;i++) msg[i]=packet[i+20];
	} catch (IOException e) {
	    return false;
	}
	return true;
    }
    
    public boolean sendPacket(OutputStream out){
	try {
	    out.write(packet);
	    out.flush();
	} catch (IOException e) {
	    return false;
	}
	return true;
    }
       
    
    /********The Following two methods are referenced and modified from a blog 
    in the following link "http://flowercat.iteye.com/blog/380861"***********/  
   //This method is used to convert integers to byte streams. 
   //Convert int number to network byte order
   public static byte[] intToBytes(int v) {
     byte[] b = new byte[4];
     b[0] = (byte) ((v >>> 24));
     b[1] = (byte) ((v >>> 16));
     b[2] = (byte) ((v >>> 8));
     b[3] = (byte) ((v >>> 0));
     return b;
     }
   //This method is used to convert byte streams back to integer. 
   //Get each byte of the number back according the inToBytes order
   public static int bytesToInt(byte[] b) {
     int v = 0;
     v |= (b[0] & 0xff) << 24;
     v |= (b[1] & 0xff) << 16;
     v |= (b[2] & 0xff) << 8;
     v |= (b[3] & 0xff) << 0;
     return v;
     } 

}
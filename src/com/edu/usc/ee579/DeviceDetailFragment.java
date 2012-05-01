package com.edu.usc.ee579;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;


public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {
    private View myContentView = null;
    WifiP2pDevice device=null;
    private ProgressDialog progressDialog=null;
    //private WifiP2pInfo info=null;
    private Server server=null;
    private Client client=null;
    boolean isConnected=false;
    TextView textView=null;
    ScrollView scroll=null;
    
    //call back function when connection is setup
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        //this.info = info;
        //change display layout
        if(this.getView().getVisibility()!=View.VISIBLE) this.getView().setVisibility(View.VISIBLE);   
	myContentView.findViewById(R.id.connect).setVisibility(View.GONE);
	myContentView.findViewById(R.id.disconnect).setVisibility(View.VISIBLE);
	isConnected=true;
	textView = (TextView) myContentView.findViewById(R.id.peerdevice);
	scroll=(ScrollView) myContentView.findViewById(R.id.scrollView1);
	//set a handler for client/server to show messages
	Handler myHandler = new Handler() {  
	    public void handleMessage(Message msg) {   
		textView.setText(textView.getText()+"\n"+(String)msg.obj);           	
            	scroll.fullScroll (ScrollView.FOCUS_DOWN);
		}     
	    };  
	//group owner will become a server and otherwise client    
	if(info.groupFormed &&info.isGroupOwner){
	    textView.setText("Connected. IP Address: "+info.groupOwnerAddress.getHostAddress());
	    server= new Server(myHandler);	
	    server.execute();
	}else if(info.groupFormed){
	    textView.setText("Connected. Group Owner IP Address: "+info.groupOwnerAddress.getHostAddress());
	    client= new Client(info.groupOwnerAddress.getHostAddress(),9777,myHandler);
	    client.execute();
	}
	return;
    }
    
    //layout for the fragment
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        return;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        myContentView = inflater.inflate(R.layout.devicedetail, null);
        //set the buttons
        myContentView.findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
        	// connect function, need a wifip2pconfig with the device address set.
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                config.groupOwnerIntent=0;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "WiFi Direct","Connecting to: "+device.deviceName+"\nPress Back Button to Cancel"
                	, true, true, new DialogInterface.OnCancelListener() {
                                                @Override
                                               public void onCancel(DialogInterface dialog) {
                                                    ((EE579Activity) getActivity()).cancelDisconnect();
                                               }
                                            }
                	);
                ((EE579Activity) getActivity()).connect(config);
                return;
            }
        });

        //disconnect button.
        myContentView.findViewById(R.id.disconnect).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                	isConnected=false;
                        ((EE579Activity) getActivity()).disconnect();
                        return;
                    }
                });
       myContentView.findViewById(R.id.cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((EE579Activity) getActivity()).cancelDisconnect();
                        return;
                    }
                });
        return myContentView;
    }

    //show device info
    public void showDetails(WifiP2pDevice device){
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) myContentView.findViewById(R.id.peerdevice);
        view.setText(" "+device.toString());
        return;
    }
    
    //clear the device info when disconnect
    public void blockDetail() {
        myContentView.findViewById(R.id.connect).setVisibility(View.VISIBLE);
        myContentView.findViewById(R.id.disconnect).setVisibility(View.GONE);
        TextView view = (TextView) myContentView.findViewById(R.id.peerdevice);
        view.setText("");
        this.getView().setVisibility(View.GONE);
        this.device=null;
        return;
    }
}
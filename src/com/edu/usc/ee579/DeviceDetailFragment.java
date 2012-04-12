package com.edu.usc.ee579;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {
    private View myContentView = null;
    WifiP2pDevice device=null;
    private ProgressDialog progressDialog=null;
    private WifiP2pInfo info=null;
    private Server server=null;
    private Client client=null;
    boolean isConnected=false;
    
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        if(this.getView().getVisibility()!=View.VISIBLE) this.getView().setVisibility(View.VISIBLE);   
	myContentView.findViewById(R.id.connect).setVisibility(View.GONE);
	myContentView.findViewById(R.id.disconnect).setVisibility(View.VISIBLE);
	isConnected=true;
	if(info.groupFormed &&info.isGroupOwner){
	    TextView view = (TextView) myContentView.findViewById(R.id.peerdevice);
	    view.setText("Connected. IP Address: "+info.groupOwnerAddress.getHostAddress());
	    //call server socket.
	    new Server(view).execute();	    
	}else if(info.groupFormed){
	    TextView view = (TextView) myContentView.findViewById(R.id.peerdevice);
	    view.setText("Connected. GroupOwner IP Address: "+info.groupOwnerAddress.getHostAddress());
	    new Client(info.groupOwnerAddress.getHostAddress(),9777,view).execute();
	}
	return;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        return;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        myContentView = inflater.inflate(R.layout.devicedetail, null);
        myContentView.findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
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

    public void showDetails(WifiP2pDevice device){
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) myContentView.findViewById(R.id.peerdevice);
        view.setText(" "+device.toString());
        return;
    }
    
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
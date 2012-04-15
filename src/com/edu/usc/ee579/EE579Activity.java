package com.edu.usc.ee579;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class EE579Activity extends Activity implements ChannelListener{
    /** Called when the activity is first created. */

    private WifiP2pManager manager;
    private Channel channel;
    private BroadcastReceiver receiver;
    private final IntentFilter intentFilter = new IntentFilter();
    private boolean isWifiP2pEnabled = false;
    Context CONTEXT=this;
    private boolean retryChannel=false;
    //private WifiP2pDevice device;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
        //searchPeer();
    }
    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menuitem, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit: 
        	disconnect();
        	cancelDisconnect();
        	unregisterReceiver(receiver);
        	this.finish();
        	System.exit(0);
        	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
        return;
    }
    
    public void showMessage(String str){
	Toast.makeText(CONTEXT, str,Toast.LENGTH_SHORT).show();
    }
    
    public void searchButton(View view){
	searchPeer();
	return;
    }
    
    public void searchPeer(){
        if(!isWifiP2pEnabled){
            new AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_launcher)
            .setTitle("WiFi Direct is Disabled!")
            .setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS)); 
                }
            }).show();
            return;
        }
/*        DeviceDetailFragment devicefragment = (DeviceDetailFragment)getFragmentManager().findFragmentById(R.id.devicedetail);
	if(devicefragment.device!=null&&devicefragment.isConnected){
	    showMessage("Please disconnect the current connection first.");
	    return;
	}*/
	final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
	                .findFragmentById(R.id.devicelist);
	fragment.onInitiateDiscovery();
	fragment.getView().setVisibility(View.VISIBLE);
	manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
             @Override
             public void onSuccess() {
                 //Toast.makeText(CONTEXT, "Searching",Toast.LENGTH_SHORT).show();
                 return;
             }
             @Override
             public void onFailure(int reasonCode) {
                 Toast.makeText(CONTEXT, "Search Failed: "+reasonCode,Toast.LENGTH_SHORT).show();
                 return;
             }
         });
    }

    public static String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }
    
    public void updateThisDevice(WifiP2pDevice device) {
        TextView view = (TextView)findViewById(R.id.mystatus);
        view.setText("My Name: "+device.deviceName+"\nMy Address: "+device.deviceAddress+"\nMy Status: "+getDeviceStatus(device.status));
        return;
    }
    
    public void connect(WifiP2pConfig config){
	 manager.connect(channel, config, new ActionListener() {
	     @Override
	     public void onSuccess() {
		 // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
	     }

	     @Override
	     public void onFailure(int reason) {
		 showMessage("Connect failed: "+reason);
	     }
	 });           
	 return;
    }
    
    public void disconnect(){
        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.devicedetail);
        fragment.blockDetail();
        manager.removeGroup(channel, new ActionListener() {
            @Override
            public void onFailure(int reasonCode) {
                showMessage("Disconnect failed. Reason :" + reasonCode);
            }

            @Override
            public void onSuccess() {
                //fragment.getView().setVisibility(View.GONE); 
        	showMessage("Disconnected.");
            }
        });
        return;
    }
    
    @Override
    public void onChannelDisconnected() {
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
        return;
	
    }
    public void cancelDisconnect() {
        if (manager != null) {
            final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                    .findFragmentById(R.id.devicedetail);
            if (fragment.device == null
                    || fragment.device.status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.device.status == WifiP2pDevice.AVAILABLE
                    || fragment.device.status == WifiP2pDevice.INVITED) {

                manager.cancelConnect(channel, new ActionListener() {

                    @Override
                    public void onSuccess() {
                        showMessage("Aborting connection");
                        return;
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        showMessage("Connect abort request failed. Reason Code: " + reasonCode);
                    }
                });
            }
        }
        return;

    }


}
package com.edu.usc.ee579;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.view.View;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private Channel channel;
    private EE579Activity activity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
            EE579Activity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }   
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct is enabled
        	activity.setIsWifiP2pEnabled(true);
            } else {
                // Wi-Fi Direct is not enabled
        	activity.setIsWifiP2pEnabled(false);
            }
            return;

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            if (manager != null) {
        	manager.requestPeers(channel,(PeerListListener)activity.getFragmentManager()
                        .findFragmentById(R.id.devicelist));
        	return;
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                // we are connected with the other device, request connection info to find group owner IP
        	final DeviceListFragment listfragment = (DeviceListFragment) activity.getFragmentManager().findFragmentById(R.id.devicelist);
        	listfragment.getView().setVisibility(View.GONE);
        	DeviceDetailFragment fragment = (DeviceDetailFragment) activity
                        .getFragmentManager().findFragmentById(R.id.devicedetail);
        	//fragment.showDetails(device);
                manager.requestConnectionInfo(channel, fragment);
            } else {
                // It's a disconnect
        	final DeviceListFragment listfragment = (DeviceListFragment) activity.getFragmentManager().findFragmentById(R.id.devicelist);
        	listfragment.getView().setVisibility(View.VISIBLE);
                final DeviceDetailFragment fragment = (DeviceDetailFragment) activity.getFragmentManager()
                        .findFragmentById(R.id.devicedetail);
                fragment.blockDetail();
            }
            return;
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
            activity.updateThisDevice((WifiP2pDevice)intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
            return;
        }

    }
}

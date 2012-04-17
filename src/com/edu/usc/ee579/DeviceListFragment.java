package com.edu.usc.ee579;

import java.util.ArrayList;
import java.util.List;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DeviceListFragment extends ListFragment implements PeerListListener {

    private List<WifiP2pDevice> peerList = new ArrayList<WifiP2pDevice>();
    View myContentView = null;
    ProgressDialog progressDialog=null;
    
    //callback function when searching is done.
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
	myContentView.findViewById(R.id.peerlisttext).setVisibility(View.VISIBLE);
	peerList.clear();
	//use a list view to update the result.
	peerList.addAll(peers.getDeviceList());
	((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
	if (peerList.size() == 0) {
	    ((EE579Activity)getActivity()).showMessage("No device found.");
	}
	return;
    }
    
    //layout for the fragment
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.rowdevice, peerList));
        return;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myContentView = inflater.inflate(R.layout.devicelist, null);
        return myContentView;
    }
 
    //list adapter for the list view. update the device list when there are changes.
    private class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {
        private List<WifiP2pDevice> items;

        public WiFiPeerListAdapter(Context context, int textViewResourceId, List<WifiP2pDevice> objects) {
            super(context, textViewResourceId, objects);
            items = objects;
            return;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.rowdevice, null);
            }
            WifiP2pDevice device = items.get(position);
            if (device != null) {
                TextView onedevice = (TextView) view.findViewById(R.id.onedevice);
                if (onedevice != null) {
                    onedevice.setText("Device name: "+device.deviceName+"\nAddress: "+device.deviceAddress+
                	    "\nStatus: "+EE579Activity.getDeviceStatus(device.status));
                }
            }
            return view;
        }
    }
    
    //call when clicking on the list items. will bring out the device detail info.
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(position);
        this.getView().setVisibility(View.GONE);
        DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager().findFragmentById(R.id.devicedetail);
        fragment.showDetails(device);
        return;
    }

    //show a process dialog when clicking the search button.
    public void onInitiateDiscovery() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(getActivity(), "WiFi Direct", "Finding peers...\nPress back button to cancel", true,true);
        return;
    }

}

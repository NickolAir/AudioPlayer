package com.example.audioplayer;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.net.InetAddress;

public class NsdDiscovery {

    private static final String SERVICE_TYPE = "_http._tcp.";
    private Context mContext;
    private NsdManager mNsdManager;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    public String IP;
    public String host;

    public NsdDiscovery(Context context) {
        mContext = context;
        mNsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);
    }

    public NsdManager getNsdManager() {
        return mNsdManager;
    }

    public void startDiscovery() {
        mDiscoveryListener = new IpDiscoveryListener(this);
        mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void stopDiscovery() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }

    public void handleIpAddress(String hostName, String ipAddress) {
        Log.d("IP", ipAddress);
        IP = ipAddress;
        host = hostName;
    }

    public String getIP() {
        return IP;
    }
}

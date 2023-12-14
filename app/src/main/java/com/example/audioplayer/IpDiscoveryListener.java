package com.example.audioplayer;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;
import android.widget.Toast;

import java.net.InetAddress;

public class IpDiscoveryListener implements NsdManager.DiscoveryListener {

    private static final String SERVICE_TYPE = "_services._dns-sd._udp"; //"_http._tcp";
    private NsdDiscovery nsdDiscovery;

    public IpDiscoveryListener(NsdDiscovery nsdDiscovery) {
        this.nsdDiscovery = nsdDiscovery;
    }

    @Override
    public void onStartDiscoveryFailed(String serviceType, int errorCode) {
        Log.e("NsdDiscovery", "Discovery failed: " + errorCode);
    }

    @Override
    public void onStopDiscoveryFailed(String serviceType, int errorCode) {
        Log.e("NsdDiscovery", "Stop discovery failed: " + errorCode);
    }

    @Override
    public void onDiscoveryStarted(String serviceType) {
        Log.d("NsdDiscovery", "Discovery started");
    }

    @Override
    public void onDiscoveryStopped(String serviceType) {
        Log.d("NsdDiscovery", "Discovery stopped");
    }

    @Override
    public void onServiceFound(NsdServiceInfo serviceInfo) {

        if(!serviceInfo.getServiceName().equals("Server"))
            return;
        System.out.println("Service found: " + serviceInfo.getServiceName());

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        NsdManager.ResolveListener resolveListener = new IpResolveListener(nsdDiscovery);
        Log.d("RESOLVING", "IP RES");
        nsdDiscovery.getNsdManager().resolveService(serviceInfo, resolveListener);
    }

    @Override
    public void onServiceLost(NsdServiceInfo serviceInfo) {
        Log.d("NsdDiscovery", "Service lost: " + serviceInfo.getServiceName());
    }
}

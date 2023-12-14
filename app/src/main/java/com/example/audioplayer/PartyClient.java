package com.example.audioplayer;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PartyClient extends AppCompatActivity implements View.OnClickListener {

    public static final int SERVERPORT = 7777;

    private static final String SERVICE_TYPE = "_http._tcp.";
    private NsdDiscovery nsdDiscovery;
    private NsdManager nsdManager;
    private NsdManager.ResolveListener resolveListener;
    private NsdServiceInfo discoveredService;
    private NsdManager.DiscoveryListener discoveryListener;

    private String SERVER_IP;
    private ClientThread clientThread;
    private Thread thread;
    private LinearLayout msgList;
    private Handler handler;
    private EditText edMessage;

    List<Music> playlist = new ArrayList<>();
    MusicAdapter musicAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        setTitle("Client");

        handler = new Handler();
        msgList = findViewById(R.id.msgList);
        edMessage = findViewById(R.id.edMessage);

        nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

        resolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                System.out.println("Resolve failed for service: " + serviceInfo.getServiceName() +
                        " with error code: " + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                // Обработка успешного разрешения (определения адреса) сервиса
                SERVER_IP = nsdDiscovery.getIP();
                System.out.println("SERVER IP " + SERVER_IP);
                System.out.println("Service resolved: " + serviceInfo.getServiceName());
                discoveredService = serviceInfo;
                connectToServer();
            }
        };

        musicAdapter = new MusicAdapter(playlist);
        loadMusic();
    }

    private void loadMusic() {
        playlist.clear();
        musicAdapter.notifyDataSetChanged();
        playlist.addAll(Helper.allMusic);
        musicAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (discoveredService != null) {
            nsdManager.stopServiceDiscovery(discoveryListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        discoverServer();
    }

    private void discoverServer() {
        NsdManager.DiscoveryListener discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                // Обработка ошибки при начале обнаружения сервисов
                Toast.makeText(PartyClient.this, "Start discovery failed with error code: " + errorCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                // Обработка ошибки при завершении обнаружения сервисов
                Toast.makeText(PartyClient.this, "Stop discovery failed with error code: " + errorCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                // Обработка начала обнаружения сервисов
                Toast.makeText(PartyClient.this, "Discovery started for service type: " + serviceType, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                // Обработка завершения обнаружения сервисов
                Toast.makeText(PartyClient.this, "Discovery stopped for service type: " + serviceType, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                // Обработка обнаруженного сервиса
                if (serviceInfo.getServiceType().equals(SERVICE_TYPE)) {
                    nsdManager.resolveService(serviceInfo, resolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                // Обработка потери сервиса
                Toast.makeText(PartyClient.this, "Service lost: " + serviceInfo.getServiceName(), Toast.LENGTH_SHORT).show();
            }
        };

        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    private void connectToServer() {
        if (discoveredService != null) {
            msgList.removeAllViews();
            clientThread = new ClientThread();
            thread = new Thread(clientThread);
            thread.start();
        }
    }

    public TextView createTextView(String message) {
        if (message == null || message.trim().isEmpty()) {
            message = "<Empty Message>";
        }

        TextView tv = new TextView(this);
        tv.setTextColor(getColor(R.color.black)); // Set text color to black
        tv.setText(message + " [" + getTime() + "]");
        tv.setTextSize(20);
        tv.setPadding(0, 5, 0, 0);
        return tv;
    }

    public void showMessage(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                msgList.addView(createTextView(message));
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.connect_server) {
            nsdDiscovery = new NsdDiscovery(this);
            nsdDiscovery.startDiscovery();

            msgList.removeAllViews();
            showMessage("Connecting to Server...");
            clientThread = new ClientThread();
            thread = new Thread(clientThread);
            thread.start();
            //System.out.println(nsdDiscovery.getIP());
        } else if (view.getId() == R.id.send_data) {
            String clientMessage = edMessage.getText().toString().trim();
            showMessage(clientMessage);
            if (clientThread != null) {
                clientThread.sendMessage(clientMessage);
            }
        }
    }

    class ClientThread implements Runnable {

        private Socket socket;
        private BufferedReader input;

        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP); //null!!!!!!!!!
                System.out.println(serverAddr);
                socket = new Socket(serverAddr, SERVERPORT);

                while (!Thread.currentThread().isInterrupted()) {
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message = input.readLine();
                    if (message == null || "Disconnect".contentEquals(message)) {
                        Thread.interrupted();
                        message = "Server Disconnected.";
                        showMessage(message);
                        break;
                    }
                    showMessage("Server: " + message);
                }

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        void sendMessage(final String message) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (socket != null) {
                            PrintWriter out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream())),
                                    true);
                            out.println(message);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clientThread != null) {
            clientThread.sendMessage("Disconnect");
            clientThread = null;
        }
        //nsdManager.stopServiceDiscovery(null);
    }
}
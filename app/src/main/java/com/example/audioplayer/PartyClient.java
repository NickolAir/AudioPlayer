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
import java.util.Date;

public class PartyClient extends AppCompatActivity implements View.OnClickListener {

    public static final int SERVERPORT = 7777;

    private static final String SERVICE_TYPE = "_http._tcp.";
    private NsdManager nsdManager;
    private NsdManager.ResolveListener resolveListener;
    private NsdServiceInfo discoveredService;
    private String SERVER_IP;
    private ClientThread clientThread;
    private Thread thread;
    private LinearLayout msgList;
    private Handler handler;
    private EditText edMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        setTitle("Client");

        handler = new Handler();
        msgList = findViewById(R.id.msgList);
        edMessage = findViewById(R.id.edMessage);

        nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

        discoverServer();
    }

    private void discoverServer() {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setServiceName("Server");

        nsdManager.discoverServices(
                serviceInfo.getServiceType(),
                NsdManager.PROTOCOL_DNS_SD,
                new NsdManager.DiscoveryListener() {
                    @Override
                    public void onDiscoveryStarted(String serviceType) {
                        Log.d("NsdDiscovery", "Discovery started");
                    }

                    @Override
                    public void onServiceFound(NsdServiceInfo serviceInfo) {
                        // Обработка обнаруженного сервиса
                        if (serviceInfo.getServiceName().equals("MyServer")) {
                            // Нашли нужный сервер
                            SERVER_IP = serviceInfo.getHost().getHostAddress();
                            // IP-адрес сервера
                            Toast.makeText(PartyClient.this, "Server found at " + SERVER_IP, Toast.LENGTH_LONG).show();

                            // connectToServer(serverIp);
                        }
                    }

                    @Override
                    public void onServiceLost(NsdServiceInfo nsdServiceInfo) {
                        // Обработка потери сервиса
                        Toast.makeText(PartyClient.this, "Service Lost: " + nsdServiceInfo.getServiceName(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDiscoveryStopped(String serviceType) {
                        // Обработка завершения обнаружения сервисов
                        Toast.makeText(PartyClient.this, "Discovery stopped for service type: " + serviceType, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                        // Обработка ошибки при начале обнаружения сервисов
                        Toast.makeText(PartyClient.this, "Discovery start failed for service type: " + serviceType +
                                " with error code: " + errorCode, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                        // Обработка ошибки при завершении обнаружения сервисов
                        Toast.makeText(PartyClient.this, "Discovery stop failed for service type: " + serviceType +
                                " with error code: " + errorCode, Toast.LENGTH_SHORT).show();
                    }
                });
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
            msgList.removeAllViews();
            showMessage("Connecting to Server...");
            clientThread = new ClientThread();
            thread = new Thread(clientThread);
            thread.start();
            showMessage("Connected to Server...");
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
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
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
        nsdManager.stopServiceDiscovery(null);
    }
}
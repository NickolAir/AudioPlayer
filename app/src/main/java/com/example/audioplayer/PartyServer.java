package com.example.audioplayer;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

public class PartyServer extends AppCompatActivity implements View.OnClickListener {

    private static final String SERVICE_TYPE = "_http._tcp.";
    private NsdManager nsdManager;
    private NsdManager.ResolveListener resolveListener;
    private NsdServiceInfo discoveredService;
    private ServerSocket serverSocket;
    private Socket tempClientSocket;
    private Thread serverThread = null;
    public static final int SERVER_PORT = 7777;
    private LinearLayout msgList;
    private Handler handler;
    private EditText edMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        setTitle("Server");

        handler = new Handler();
        msgList = findViewById(R.id.msgList);
        edMessage = findViewById(R.id.edMessage);

        nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

        //initializeServerSocket();
        registerService();
    }

    private void initializeServerSocket() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerService() {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setServiceName("Server");
        serviceInfo.setPort(SERVER_PORT);

        nsdManager.registerService(
                serviceInfo,
                NsdManager.PROTOCOL_DNS_SD,
                new NsdManager.RegistrationListener() {
                    @Override
                    public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                        // Обработка успешной регистрации сервиса
                        Toast.makeText(PartyServer.this, "Server registered: " + serviceInfo.getServiceName(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                        // Обработка ошибки при регистрации сервиса
                        Toast.makeText(PartyServer.this, "Registration failed for service: " + serviceInfo.getServiceName() +
                                " with error code: " + errorCode, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                        // Обработка успешного завершения регистрации сервиса
                        Toast.makeText(PartyServer.this, "Server unregistered: " + serviceInfo.getServiceName(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                        // Обработка ошибки при завершении регистрации сервиса
                        Toast.makeText(PartyServer.this, "Unregistration failed for service: " + serviceInfo.getServiceName() +
                                " with error code: " + errorCode, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public TextView createTextView(String message) {
        if (message == null || message.trim().isEmpty()) {
            message = "<Empty Message>";
        }

        TextView tv = new TextView(this);
        tv.setText(message + " [" + getTime() + "]");
        tv.setTextSize(20);
        tv.setPadding(0, 5, 0, 0);
        return tv;
    }

    public void showMessage(final String message, final int color) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                msgList.addView(createTextView(message));
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.start_server) {
            msgList.removeAllViews();
            showMessage("Server Started.", getColor(R.color.black));
            serverThread = new Thread(new ServerThread());
            serverThread.start();
        } else if (view.getId() == R.id.send_data) {
            String msg = edMessage.getText().toString().trim();
            showMessage("Server : " + msg, getColor(R.color.black));
            sendMessage(msg);
        }
    }

    private void sendMessage(final String message) {
        try {
            if (tempClientSocket != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            PrintWriter out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(tempClientSocket.getOutputStream())),
                                    true);
                            out.println(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ServerThread implements Runnable {

        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                findViewById(R.id.start_server).setVisibility(View.GONE);
            } catch (IOException e) {
                e.printStackTrace();
                showMessage("Error Starting Server : " + e.getMessage(), getColor(R.color.red));
            }

            if (serverSocket != null) {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        socket = serverSocket.accept();
                        CommunicationThread commThread = new CommunicationThread(socket);
                        new Thread(commThread).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                        showMessage("Error Communicating to Client : " + e.getMessage(), getColor(R.color.red));
                    }
                }
            }
        }
    }

    class CommunicationThread implements Runnable {

        private Socket clientSocket;
        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            tempClientSocket = clientSocket;
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
                showMessage("Error Connecting to Client!!", getColor(R.color.red));
            }
            showMessage("Connected to Client!!", getColor(R.color.black));
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    if (read == null || "Disconnect".contentEquals(read)) {
                        Thread.interrupted();
                        read = "Client Disconnected";
                        showMessage("Client : " + read, getColor(R.color.black));
                        break;
                    }
                    showMessage("Client : " + read, getColor(R.color.black));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serverThread != null) {
            sendMessage("Disconnect");
            serverThread.interrupt();
            serverThread = null;
        }
        nsdManager.unregisterService(null);
    }
}
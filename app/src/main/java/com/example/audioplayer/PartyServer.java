package com.example.audioplayer;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PartyServer extends AppCompatActivity {

    private static final String SERVICE_TYPE = "_http._tcp.";
    public static int SERVER_PORT;
    private NsdManager nsdManager;
    private NsdManager.ResolveListener resolveListener;
    private ServerSocket serverSocket;
    private Thread serverThread = null;

    private ExecutorService executorService;

    List<NsdServiceInfo> mDiscoveredServices;
    List<Music> playlist = new ArrayList<Music>();
    List<Socket> clientSockets = new ArrayList<Socket>();
    MusicAdapter musicAdapter;
    RecyclerView recyclerView;

    @Override 
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

        executorService = Executors.newCachedThreadPool();
        mDiscoveredServices = new ArrayList<NsdServiceInfo>();

        serverThread = new Thread(new ServerThread());
        serverThread.start();
        System.out.println("2) server port " + SERVER_PORT);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        resolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                System.out.println("Resolve failed for service: " + serviceInfo.getServiceName() +
                        " with error code: " + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                // Обработка успешного разрешения (определения адреса) сервиса
                System.out.println("Service resolved: " + serviceInfo.getServiceName());
                if (!serviceInfo.getServiceType().equals(SERVICE_TYPE)) {
                    Log.d("SERVICE_TYPE", serviceInfo.getServiceType());
                } else if (serviceInfo.getServiceName().equals("Server")) {
                    Log.d("Our device", serviceInfo.getServiceName());
                } else {
                    mDiscoveredServices.add(serviceInfo);
                }
            }
        };

        registerService();

        startDiscovery();

        recyclerView = findViewById(R.id.recycler_songs);
        musicAdapter = new MusicAdapter(playlist, new MusicAdapter.Action() {
            @Override
            public void onItemClicked(Music music) {
                if (mDiscoveredServices.isEmpty()) {
                    System.out.println("Empty");
                }
                for (Socket clientSocket : clientSockets) {
                    sendSongToClient(music, clientSocket);
                    System.out.println("sent to " + clientSocket.getInetAddress().getHostAddress());
                }
            }
        });
        recyclerView.setAdapter(musicAdapter);
        loadMusic();
    }

    private void startDiscovery() {
        NsdManager.DiscoveryListener mdiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                // Обработка ошибки при начале обнаружения сервисов
                Toast.makeText(PartyServer.this, "Start discovery failed with error code: " + errorCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                // Обработка ошибки при завершении обнаружения сервисов
                Toast.makeText(PartyServer.this, "Stop discovery failed with error code: " + errorCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                // Обработка начала обнаружения сервисов
                Toast.makeText(PartyServer.this, "Discovery started for service type: " + serviceType, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                // Обработка завершения обнаружения сервисов
                Toast.makeText(PartyServer.this, "Discovery stopped for service type: " + serviceType, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                // Обработка обнаруженного сервиса
                System.out.println("Discovery " + serviceInfo.getServiceName());
                if (!serviceInfo.getServiceName().equals("Server")) {
                    nsdManager.resolveService(serviceInfo, resolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                // Обработка потери сервиса
                Toast.makeText(PartyServer.this, "Service lost: " + serviceInfo.getServiceName(), Toast.LENGTH_SHORT).show();
            }
        };
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mdiscoveryListener);
    }

    private void addCompositionsToDatabase(List<Music> playlist) {
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        for (Music music : playlist) {
            File track = new File(music.getPath());

            // Проверяем, существует ли уже запись с таким названием и хэшем в базе данных
            if (!isCompositionExists(db, music.getTitle(), HashUtils.calculateSHA256ForFile(track))) {
                addComposition(db, music.getTitle(), HashUtils.calculateSHA256ForFile(track));
            }
        }

        db.close();
    }

    // Метод добавления композиции в базу данных
    private void addComposition(SQLiteDatabase db, String name, String hash) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_NAME, name);
        values.put(DBHelper.COLUMN_HASH, hash);

        // Вставка записи в таблицу
        long newRowId = db.insert(DBHelper.TABLE_COMPOSITIONS, null, values);

        // Проверка добавления
        if (newRowId != -1) {
            System.out.println("success");
        } else {
            System.out.println("error");
        }
    }

    // Метод проверки существования записи в базе данных
    private boolean isCompositionExists(SQLiteDatabase db, String name, String hash) {
        String[] columns = {DBHelper.COLUMN_NAME, DBHelper.COLUMN_HASH};
        String selection = DBHelper.COLUMN_NAME + " = ? AND " + DBHelper.COLUMN_HASH + " = ?";
        String[] selectionArgs = {name, hash};
        Cursor cursor = db.query(DBHelper.TABLE_COMPOSITIONS, columns, selection, selectionArgs, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    private void loadMusic() {
        playlist.clear();
        musicAdapter.notifyDataSetChanged();
        playlist.addAll(Helper.allMusic);
        musicAdapter.notifyDataSetChanged();
        //addCompositionsToDatabase(playlist);
    }

    class ServerThread implements Runnable {

        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(0);
                SERVER_PORT = serverSocket.getLocalPort();
                System.out.println("1) server port " + SERVER_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (serverSocket != null) {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        socket = serverSocket.accept();
                        System.out.println("connected");
                        if (socket != null) {
                            clientSockets.add(socket);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void registerService() {
        System.out.println("3) server port " + SERVER_PORT);
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

    // Метод для отправки песни клиенту
    private void sendSongToClient(Music music, Socket clientSocket) {
        executorService.execute(new Runnable() { // Используем пул потоков для обработки каждого подключения
            @Override
            public void run() {
                try {
                    if (clientSocket != null) {
                        // Отправляем имя композиции на клиент
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        out.println(music.getTitle());

                        // Открываем поток для отправки файла на клиент
                        BufferedOutputStream bos = new BufferedOutputStream(clientSocket.getOutputStream());
                        FileInputStream fis = new FileInputStream(new File(music.getPath()));
                        byte[] buffer = new byte[1024*4];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            bos.write(buffer, 0, bytesRead);
                        }

                        // Закрываем потоки
                        fis.close();
                        bos.close();

                        Intent intent = new Intent(PartyServer.this, PlayerActivity.class);
                        intent.putExtra("title", music.getTitle());
                        intent.putExtra("album", music.getAlbum());
                        intent.putExtra("artist", music.getArtist());
                        intent.putExtra("path", music.getPath());
                        intent.putExtra("duration", music.getDuration());
                        intent.putExtra("position", music.getPosition());
                        startActivity(intent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serverThread != null) {
            serverThread.interrupt();
            serverThread = null;
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //nsdManager.unregisterService(null);
    }
}
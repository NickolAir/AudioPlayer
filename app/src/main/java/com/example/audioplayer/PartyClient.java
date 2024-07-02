package com.example.audioplayer;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;

public class PartyClient extends AppCompatActivity {

    public static int SERVERPORT;
    private static final String SERVICE_TYPE = "_http._tcp.";
    private NsdDiscovery nsdDiscovery;
    private NsdManager nsdManager;
    private NsdManager.ResolveListener resolveListener;
    private NsdServiceInfo discoveredService;
    private NsdManager.DiscoveryListener discoveryListener;

    private String SERVER_IP;
    private ClientThread clientThread;
    private Thread thread;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        progressBar = findViewById(R.id.pb);

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
                if (serviceInfo.getServiceName().equals("Server")) {
                    SERVER_IP = serviceInfo.getHost().getHostAddress();
                    System.out.println("SERVER IP " + SERVER_IP);
                }
                System.out.println("Service resolved: " + serviceInfo.getServiceName());
                discoveredService = serviceInfo;
                SERVERPORT = serviceInfo.getPort();
                progressBar.setVisibility(View.VISIBLE);
                connectToServer();
            }
        };
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

    @Override
    protected void onPause() {
        super.onPause();
        /*if (discoveredService != null) {
            nsdManager.stopServiceDiscovery(discoveryListener);
        }*/
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
                System.out.println(serviceInfo.getServiceType());

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

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
            clientThread = new ClientThread();
            thread = new Thread(clientThread);
            thread.start();
        }
    }

    class ClientThread implements Runnable {
        @Override
        public void run() {
            try {
                System.out.println("start client");
                System.out.println(SERVER_IP + " " + SERVERPORT);
                Socket socket = new Socket(SERVER_IP, SERVERPORT);
                System.out.println("connected to server");

                // Получение имени файла
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String filename = reader.readLine();
                System.out.println(filename);
                long startTime = Long.parseLong(reader.readLine());
                System.out.println("Start time received: " + startTime);

                // Получение каталога для записи файла во внешнем хранилище приложения
                File externalStorageDir = getExternalFilesDir(null);
                if (externalStorageDir != null) {
                    // Формируем путь для сохранения файла
                    File outputFile = new File(externalStorageDir, filename);
                    System.out.println(outputFile);

                    // Получение файла
                    BufferedInputStream fileReader = new BufferedInputStream(socket.getInputStream());
                    FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                    byte[] buffer = new byte[1024 * 4];
                    int bytesRead;
                    while ((bytesRead = fileReader.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                    System.out.println("File received " + filename);
                    fileOutputStream.close();
                    fileReader.close();

                    Music music = new Music(filename, "", "", outputFile.getPath(), "", 0);

                    // Рассчитываем задержку до начала воспроизведения
                    long currentTime = System.currentTimeMillis();
                    System.out.println("Current time: " + currentTime);
                    long delay = startTime - currentTime;
                    System.out.println("Delay: " + delay);
                    if (delay > 0) {
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    Intent intent = new Intent(PartyClient.this, PlayerActivity.class);
                    intent.putExtra("title", music.getTitle());
                    intent.putExtra("album", music.getAlbum());
                    intent.putExtra("artist", music.getArtist());
                    intent.putExtra("path", music.getPath());
                    intent.putExtra("duration", music.getDuration());
                    intent.putExtra("position", music.getPosition());
                    startActivity(intent);
                } else {
                    System.out.println("External storage directory is null");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clientThread != null) {
            clientThread = null;
        }
        //nsdManager.stopServiceDiscovery(null);
    }
}
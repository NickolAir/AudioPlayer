package com.example.audioplayer;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Helper {
    public static List<Music> allMusic = new ArrayList<>();
    public static boolean isLoaded = false;
    public static void getAllMusic(Activity context) {
        List<Music> tempList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] data = {MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION};
        Cursor cr = context.getContentResolver().query(uri, data, null, null, null);

        int position = 0;

        if (cr != null) {
            while (cr.moveToNext()) {
                String path = cr.getString(0);
                String title = path.substring(path.lastIndexOf("/") + 1);
                String album = cr.getString(1);
                String artist = cr.getString(2);
                String time = getTime(cr.getString(3));

                tempList.add(new Music(title, artist, album, path, time, position));

                position++;
            }
            cr.close();
            Helper.allMusic.addAll(tempList);
            Helper.isLoaded = true;
        }
    }

    private static String getTime(String duration) {
        long allTime = Integer.parseInt(duration);

        // Вычисляем количество минут
        long minutes = (allTime / (1000 * 60)) % 60;

        // Вычисляем количество секунд
        long seconds = (allTime / 1000) % 60;

        String time = "";
        if (seconds < 10) {
            time = minutes + ":0" + seconds;
        } else {
            time = minutes + ":" + seconds;
        }
        return time;
    }
}

package com.example.audioplayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    public static String calculateSHA256ForFile(File file) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[65536];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }

            fis.close();

            // Преобразование байтов в строку
            byte[] bytes = md.digest();
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : bytes) {
                stringBuilder.append(String.format("%02x", b));
            }

            return stringBuilder.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            // Обработка ошибок NoSuchAlgorithmException и IOException
        }

        return null;
    }
}

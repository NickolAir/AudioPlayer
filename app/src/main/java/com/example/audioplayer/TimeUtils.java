package com.example.audioplayer;

public class TimeUtils {
    public static long getNetworkTime() {
        SntpClient client = new SntpClient();
        if (client.requestTime("time.google.com", 30000)) {
            return client.getNtpTime();
        }
        return System.currentTimeMillis();
    }
}
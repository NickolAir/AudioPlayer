package com.example.audioplayer;

import android.os.SystemClock;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SntpClient {
    private static final String TAG = "SntpClient";
    private static final int NTP_PORT = 123;
    private static final int NTP_PACKET_SIZE = 48;
    private static final int NTP_MODE_CLIENT = 3;
    private static final int NTP_VERSION = 3;
    private static final int TRANSMIT_TIME_OFFSET = 40;
    private static final int NTP_SERVER_TIMEOUT = 30000;

    private long ntpTime;
    private long ntpTimeReference;
    private long roundTripTime;

    public boolean requestTime(String ntpHost, int timeout) {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(timeout);
            InetAddress address = InetAddress.getByName(ntpHost);
            byte[] buffer = new byte[NTP_PACKET_SIZE];
            buffer[0] = NTP_MODE_CLIENT | (NTP_VERSION << 3);

            DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, NTP_PORT);
            socket.send(request);

            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.receive(response);

            long originateTime = readTimeStamp(buffer, TRANSMIT_TIME_OFFSET);
            long receiveTime = System.currentTimeMillis();
            long transmitTime = readTimeStamp(buffer, TRANSMIT_TIME_OFFSET);
            long responseTime = System.currentTimeMillis();

            roundTripTime = responseTime - receiveTime - (transmitTime - originateTime);
            ntpTime = transmitTime + (roundTripTime / 2);
            ntpTimeReference = responseTime;

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public long getNtpTime() {
        return ntpTime + (SystemClock.elapsedRealtime() - ntpTimeReference);
    }

    private long readTimeStamp(byte[] buffer, int offset) {
        long seconds = ((buffer[offset] & 0xFFL) << 24)
                | ((buffer[offset + 1] & 0xFFL) << 16)
                | ((buffer[offset + 2] & 0xFFL) << 8)
                | (buffer[offset + 3] & 0xFFL);

        long fraction = ((buffer[offset + 4] & 0xFFL) << 24)
                | ((buffer[offset + 5] & 0xFFL) << 16)
                | ((buffer[offset + 6] & 0xFFL) << 8)
                | (buffer[offset + 7] & 0xFFL);

        return (seconds * 1000) + ((fraction * 1000) / 0x100000000L);
    }
}
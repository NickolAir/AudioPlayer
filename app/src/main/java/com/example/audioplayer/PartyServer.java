package com.example.audioplayer;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class PartyServer extends Thread {
    private static final int SAMPLE_RATE = 44100;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int BUFFER_SIZE = 4096;

    private DatagramSocket socket;
    private InetAddress destinationAddress;
    private int destinationPort;
    private AudioRecord audioRecord;

    public PartyServer(InetAddress destinationAddress, int destinationPort) {
        this.destinationAddress = destinationAddress;
        this.destinationPort = destinationPort;
    }

    @Override
    public void run() {
        try {
            // Initialize audio recording
            int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);
            audioRecord.startRecording();

            // Initialize UDP socket
            socket = new DatagramSocket();

            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket packet;

            while (true) {
                int bytesRead = audioRecord.read(buffer, 0, buffer.length);
                packet = new DatagramPacket(buffer, bytesRead, destinationAddress, destinationPort);
                socket.send(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Cleanup resources
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
            }
            if (socket != null) {
                socket.close();
            }
        }
    }
}
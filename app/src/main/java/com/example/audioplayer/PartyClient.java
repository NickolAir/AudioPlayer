package com.example.audioplayer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class PartyClient extends Thread {
    private static final int SAMPLE_RATE = 44100;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    private static final int BUFFER_SIZE = 4096;

    private DatagramSocket socket;
    private AudioTrack audioTrack;

    public PartyClient(int port) {
        try {
            // Initialize UDP socket for receiving RTP packets
            socket = new DatagramSocket(port);
            socket.setReuseAddress(true);

            // Initialize AudioTrack for audio playback
            int minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
            audioTrack = new AudioTrack(
                    AudioManager.STREAM_VOICE_CALL,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    minBufferSize,
                    AudioTrack.MODE_STREAM
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            // Start audio playback
            audioTrack.play();

            while (true) {
                socket.receive(packet);
                byte[] audioData = packet.getData();
                int audioLength = packet.getLength();

                // Play the received audio data
                audioTrack.write(audioData, 0, audioLength);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Cleanup resources
            if (audioTrack != null) {
                audioTrack.stop();
                audioTrack.release();
            }
            if (socket != null) {
                socket.close();
            }
        }
    }
}
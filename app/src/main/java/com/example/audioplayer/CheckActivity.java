package com.example.audioplayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CheckActivity extends AppCompatActivity {

    private TextView titleTextView;
    private TextView playlistStatusTextView;
    private Button sendTrack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        titleTextView = findViewById(R.id.titleTextView);
        playlistStatusTextView = findViewById(R.id.playlistStatusTextView);

        sendTrack = findViewById(R.id.send_track);

        sendTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), WiFiDirectActivity.class);
                startActivity(intent);
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("title");
            boolean isInPlaylist = intent.getBooleanExtra("isInPlaylist", false);

            titleTextView.setText(title);

            if (isInPlaylist) {
                playlistStatusTextView.setText("Трек в плейлисте на сервере");
            } else {
                playlistStatusTextView.setText("Трек не найден в плейлисте на сервере");
            }
        }
    }
}
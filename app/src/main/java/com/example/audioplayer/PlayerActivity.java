package com.example.audioplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PlayerActivity extends AppCompatActivity {

    String musicPath, musicAlbum, musicArtist, musicDuration, musicTitle;
    ImageView prev, play, next;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Bundle bundle = getIntent().getExtras();
        musicTitle = bundle.getString("title");
        musicAlbum = bundle.getString("album");
        musicArtist = bundle.getString("artist");
        musicDuration = bundle.getString("duration");
        musicPath = bundle.getString("path");

        prev = findViewById(R.id.previous);
        play = findViewById(R.id.pause_play);
        next = findViewById(R.id.next);
        title = findViewById(R.id.song_title);
        title.setText(musicTitle);

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pressPrev();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pressPlay();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pressNext();
            }
        });

    }

    private void pressNext() {

    }

    private void pressPrev() {

    }

    private void pressPlay() {

    }
}
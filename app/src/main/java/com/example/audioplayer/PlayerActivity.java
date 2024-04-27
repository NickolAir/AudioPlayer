package com.example.audioplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PlayerActivity extends AppCompatActivity {

    String musicPath, musicAlbum, musicArtist, musicDuration, musicTitle;
    ImageView prev, play, next, btnReturn;
    TextView title;
    MediaPlayer mediaPlayer;
    List<Music> playlist = new ArrayList<>();
    MusicAdapter musicAdapter;
    int currentTrackIndex;

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
        currentTrackIndex = bundle.getInt("position");

        prev = findViewById(R.id.previous);
        play = findViewById(R.id.pause_play);
        next = findViewById(R.id.next);
        title = findViewById(R.id.song_title);
        btnReturn = findViewById(R.id.back_arrow);

        initMusic(currentTrackIndex, musicPath);

        title.setText(musicTitle);

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pressPrev();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pressNext();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPause();
            }
        });

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.release();
                mediaPlayer = null;
                onBackPressed();
            }
        });

        musicAdapter = new MusicAdapter(playlist, new MusicAdapter.Action() {
            @Override
            public void onItemClicked(Music music) {
                Intent intent = new Intent(PlayerActivity.this, PlayerActivity.class);
                intent.putExtra("title", music.getTitle());
                intent.putExtra("album", music.getAlbum());
                intent.putExtra("artist", music.getArtist());
                intent.putExtra("path", music.getPath());
                intent.putExtra("duration", music.getDuration());
                intent.putExtra("position", music.getPosition());
                startActivity(intent);
            }
        });
        loadMusic();
    }

    private void loadMusic() {
        playlist.clear();
        musicAdapter.notifyDataSetChanged();
        playlist.addAll(Helper.allMusic);
        musicAdapter.notifyDataSetChanged();
    }

    private void initMusic(int position, String path) {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepareAsync();
        } catch (Exception e){
            Toast.makeText(this, "Error, music can't play", Toast.LENGTH_SHORT).show();
        }

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                play.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                mediaPlayer.start();
            }
        });
    }

    private void pressNext() {
        if (currentTrackIndex < playlist.size() - 1) {
            currentTrackIndex++;
        } else {
            currentTrackIndex = 0;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        initMusic(currentTrackIndex, playlist.get(currentTrackIndex).path);
        title.setText(playlist.get(currentTrackIndex).title);
    }

    private void pressPrev() {
        if (currentTrackIndex > 0) {
            currentTrackIndex--;
        } else {
            currentTrackIndex = playlist.size() - 1;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        initMusic(currentTrackIndex, playlist.get(currentTrackIndex).path);
        title.setText(playlist.get(currentTrackIndex).title);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                play.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
            } else {
                mediaPlayer.start();
                play.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
            }
        }
    }
}
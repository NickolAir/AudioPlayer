package com.example.audioplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    MusicAdapter musicAdapter;
    List<Music> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_songs);
        musicAdapter = new MusicAdapter(list);
        recyclerView.setAdapter(musicAdapter);

        loadMusic();

    }

    private void loadMusic() {
        //list.add(new Music());

        musicAdapter.notifyDataSetChanged();
    }
}
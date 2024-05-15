package com.example.audioplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.loader.content.AsyncTaskLoader;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.drm.DrmStore;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    MusicAdapter musicAdapter;
    List<Music> list = new ArrayList<>();

    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        if (isPermission()) {
            loadPlaylist();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 12);
        }

        recyclerView = findViewById(R.id.recycler_songs);
        musicAdapter = new MusicAdapter(list, new MusicAdapter.Action() {
            @Override
            public void onItemClicked(Music music) {
                Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                intent.putExtra("title", music.getTitle());
                intent.putExtra("album", music.getAlbum());
                intent.putExtra("artist", music.getArtist());
                intent.putExtra("path", music.getPath());
                intent.putExtra("duration", music.getDuration());
                intent.putExtra("position", music.getPosition());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(musicAdapter);

        loadMusic();

        sidebar();
    }

    private boolean isPermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void loadPlaylist() {
        Helper.getAllMusic(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadPlaylist();
        }
    }

    private void sidebar() {
        navigation = findViewById(R.id.nav);
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_songs:
                        loadMusic();
                        break;
                    case R.id.nav_fav:
                        loadFav();
                        break;
                    case R.id.nav_create:
                        Intent intentCreate = new Intent(MainActivity.this, PartyServer.class);
                        startActivity(intentCreate);
                        break;
                    case R.id.nav_join:
                        Intent intentJoin = new Intent(MainActivity.this, PartyClient.class);
                        startActivity(intentJoin);
                        break;
                }
                return false;
            }
        });
    }

    private void loadFav() {

    }

    private void loadMusic() {
        list.clear();
        musicAdapter.notifyDataSetChanged();
        list.addAll(Helper.allMusic);
        musicAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
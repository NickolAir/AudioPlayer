package com.example.audioplayer;

public class Music {
    String title, artist, album, path, duration;
    int position;

    public Music(String title, String artist, String album, String path, String duration, int position) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.path = path;
        this.duration = duration;
        this.position = position;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getPath() {
        return path;
    }

    public String getDuration() {
        return duration;
    }

    public int getPosition() {
        return position;
    }
}

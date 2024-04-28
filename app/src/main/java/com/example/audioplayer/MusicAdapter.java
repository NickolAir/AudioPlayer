package com.example.audioplayer;

import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicHolder> {

    public interface Action {
        void onItemClicked(Music music);
    }

    private List<Music> list;
    private Action action;

    public MusicAdapter(List<Music> list, Action action) {
        this.list = list;
        this.action = action;
    }

    @NonNull
    @Override
    public MusicAdapter.MusicHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MusicHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MusicAdapter.MusicHolder holder, int position) {
        Music music = list.get(position);
        holder.title.setText(music.getTitle());
        String duration = music.getDuration() + " Minutes";
        holder.duration.setText(duration);

        Glide.with(holder.itemView.getContext())
                .load(music.getAlbum())
                .error(R.drawable.smoothie)
                .placeholder(R.drawable.smoothie)
                .into(holder.icon);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                action.onItemClicked(music);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MusicHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView title, duration;

        public MusicHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.item_img);
            title = itemView.findViewById(R.id.item_title);
            duration = itemView.findViewById(R.id.item_duration);
        }
    }
}
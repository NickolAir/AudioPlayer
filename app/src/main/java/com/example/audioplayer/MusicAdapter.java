package com.example.audioplayer;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicHolder> {

    List<Music> list;

    public MusicAdapter(List<Music> list) {
        this.list = list;
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
                .error(R.drawable.ic_song)
                .placeholder(R.drawable.ic_song)
                .into(holder.icon);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), PlayerActivity.class);
                intent.putExtra("title", music.getTitle());
                intent.putExtra("album", music.getAlbum());
                intent.putExtra("artist", music.getArtist());
                intent.putExtra("path", music.getPath());
                intent.putExtra("duration", music.getDuration());
                holder.itemView.getContext().startActivity(intent);

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
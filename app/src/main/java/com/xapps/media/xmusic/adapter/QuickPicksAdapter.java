package com.xapps.media.xmusic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.xapps.media.xmusic.R;
import com.xapps.media.xmusic.models.Song;
import java.util.ArrayList;
import java.util.List;

public class QuickPicksAdapter extends RecyclerView.Adapter<QuickPicksAdapter.ViewHolder> {

    public interface OnQuickPickClickListener {
        void onSongClick(Song song, int position, List<Song> allSongs);
    }

    private final List<Song> items = new ArrayList<>();
    private final OnQuickPickClickListener listener;

    public QuickPicksAdapter(List<Song> initialItems, OnQuickPickClickListener listener) {
        if (initialItems != null) this.items.addAll(initialItems);
        this.listener = listener;
    }

    public void updateItems(List<Song> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public List<Song> getItems() {
        return items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_explore_quick_pick, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = items.get(position);
        holder.title.setText(song.title);
        holder.artist.setText(song.artist);

        Glide.with(holder.thumbnail.getContext())
                .load(song.getArtworkUri())
                .placeholder(R.drawable.placeholder_small)
                .error(R.drawable.placeholder_small)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.thumbnail);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSongClick(song, holder.getBindingAdapterPosition(), items);
        });
        holder.moreButton.setOnClickListener(v -> {
            if (listener != null) listener.onSongClick(song, holder.getBindingAdapterPosition(), items);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView thumbnail;
        final TextView title;
        final TextView artist;
        final View moreButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.qp_thumbnail);
            title = itemView.findViewById(R.id.qp_title);
            artist = itemView.findViewById(R.id.qp_artist);
            moreButton = itemView.findViewById(R.id.qp_more);
        }
    }
}

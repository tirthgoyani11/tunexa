package com.xapps.media.xmusic.adapter;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.xapps.media.xmusic.R;
import com.xapps.media.xmusic.models.Song;
import com.xapps.media.xmusic.utils.MaterialColorUtils;
import com.xapps.media.xmusic.utils.XUtils;

import java.util.List;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.QueueViewHolder> {

    public interface OnQueueItemListener {
        void onItemClick(int position);
        void onItemRemove(int position);
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    private final List<Song> songs;
    private int currentPlayingIndex;
    private final OnQueueItemListener listener;

    public QueueAdapter(List<Song> songs, int currentPlayingIndex, OnQueueItemListener listener) {
        this.songs = songs;
        this.currentPlayingIndex = currentPlayingIndex;
        this.listener = listener;
    }

    public void setCurrentPlayingIndex(int index) {
        int old = this.currentPlayingIndex;
        this.currentPlayingIndex = index;
        if (old >= 0 && old < getItemCount()) notifyItemChanged(old);
        if (index >= 0 && index < getItemCount()) notifyItemChanged(index);
    }

    public int getCurrentPlayingIndex() {
        return currentPlayingIndex;
    }

    @NonNull
    @Override
    public QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_queue_song, parent, false);
        return new QueueViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull QueueViewHolder holder, int position) {
        Song song = songs.get(position);
        boolean isPlaying = (position == currentPlayingIndex);

        holder.posText.setText(String.valueOf(position + 1));
        holder.titleText.setText(song.title);
        holder.artistText.setText(song.artist);

        if (isPlaying) {
            holder.posText.setTextColor(MaterialColorUtils.colorPrimary);
            holder.titleText.setTextColor(MaterialColorUtils.colorPrimary);
            holder.titleText.setTypeface(null, Typeface.BOLD);
        } else {
            holder.posText.setTextColor(MaterialColorUtils.colorOutline);
            holder.titleText.setTextColor(MaterialColorUtils.colorOnSurface);
            holder.titleText.setTypeface(null, Typeface.NORMAL);
        }

        Glide.with(holder.itemView.getContext())
                .load(song.getArtworkUri() != null ? song.getArtworkUri() : R.drawable.placeholder_small)
                .transform(new RoundedCorners(XUtils.convertToPx(holder.itemView.getContext(), 8f)))
                .into(holder.thumbView);

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) {
                listener.onItemClick(pos);
            }
        });

        holder.removeBtn.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) {
                listener.onItemRemove(pos);
            }
        });

        holder.dragHandle.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN && listener != null) {
                listener.onStartDrag(holder);
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public static class QueueViewHolder extends RecyclerView.ViewHolder {
        public final TextView posText;
        public final ImageView thumbView;
        public final TextView titleText;
        public final TextView artistText;
        public final ImageView removeBtn;
        public final ImageView dragHandle;

        public QueueViewHolder(@NonNull View itemView) {
            super(itemView);
            posText = itemView.findViewById(R.id.queue_item_pos);
            thumbView = itemView.findViewById(R.id.queue_item_thumb);
            titleText = itemView.findViewById(R.id.queue_item_title);
            artistText = itemView.findViewById(R.id.queue_item_artist);
            removeBtn = itemView.findViewById(R.id.queue_item_remove);
            dragHandle = itemView.findViewById(R.id.queue_item_handle);
        }
    }
}

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
import com.xapps.media.xmusic.models.ExploreCardItem;
import java.util.ArrayList;
import java.util.List;

public class CommunityPlaylistAdapter extends RecyclerView.Adapter<CommunityPlaylistAdapter.ViewHolder> {

    public interface OnPlayClickListener {
        void onPlayClick(ExploreCardItem item);
    }

    private final List<ExploreCardItem> items = new ArrayList<>();
    private final OnPlayClickListener listener;

    public CommunityPlaylistAdapter(List<ExploreCardItem> initialItems, OnPlayClickListener listener) {
        if (initialItems != null) this.items.addAll(initialItems);
        this.listener = listener;
    }

    public void updateItems(List<ExploreCardItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_explore_community_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExploreCardItem item = items.get(position);
        holder.title.setText(item.title);
        holder.subtitle.setText(item.subtitle);
        holder.views.setText("4.8M views");

        Glide.with(holder.image.getContext())
                .load(item.imageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.image);

        View.OnClickListener click = v -> {
            if (listener != null) listener.onPlayClick(item);
        };
        holder.itemView.setOnClickListener(click);
        holder.playButton.setOnClickListener(click);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView title;
        final TextView subtitle;
        final TextView views;
        final View playButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.comm_image);
            title = itemView.findViewById(R.id.comm_title);
            subtitle = itemView.findViewById(R.id.comm_subtitle);
            views = itemView.findViewById(R.id.comm_views);
            playButton = itemView.findViewById(R.id.btn_play_red);
        }
    }
}

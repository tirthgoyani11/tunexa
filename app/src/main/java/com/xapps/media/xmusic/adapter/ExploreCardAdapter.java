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

public class ExploreCardAdapter extends RecyclerView.Adapter<ExploreCardAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ExploreCardItem item);
    }

    private final List<ExploreCardItem> items = new ArrayList<>();
    private final OnItemClickListener listener;
    private final boolean showSubtitle;

    public ExploreCardAdapter(List<ExploreCardItem> initialItems, boolean showSubtitle, OnItemClickListener listener) {
        if (initialItems != null) this.items.addAll(initialItems);
        this.showSubtitle = showSubtitle;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_explore_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExploreCardItem item = items.get(position);
        holder.title.setText(item.title);
        holder.overlayTitle.setText(item.title);
        if (showSubtitle && item.subtitle != null && !item.subtitle.isEmpty()) {
            holder.subtitle.setText(item.subtitle);
            holder.subtitle.setVisibility(View.VISIBLE);
        } else {
            holder.subtitle.setVisibility(View.GONE);
        }

        Glide.with(holder.image.getContext())
                .load(item.imageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView overlayTitle;
        final TextView title;
        final TextView subtitle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.card_image);
            overlayTitle = itemView.findViewById(R.id.card_overlay_title);
            title = itemView.findViewById(R.id.card_title);
            subtitle = itemView.findViewById(R.id.card_subtitle);
        }
    }
}

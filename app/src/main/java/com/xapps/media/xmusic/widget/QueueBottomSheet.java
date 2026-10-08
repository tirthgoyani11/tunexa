package com.xapps.media.xmusic.widget;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.media3.session.MediaController;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.xapps.media.xmusic.activity.RootActivity;
import com.xapps.media.xmusic.adapter.QueueAdapter;
import com.xapps.media.xmusic.data.RuntimeData;
import com.xapps.media.xmusic.databinding.LayoutQueueSheetBinding;
import com.xapps.media.xmusic.models.Song;
import com.xapps.media.xmusic.service.manager.ExoPlayerManager;

public class QueueBottomSheet {

    private final RootActivity activity;
    private BottomSheetDialog dialog;
    private LayoutQueueSheetBinding binding;
    private QueueAdapter adapter;
    private ItemTouchHelper touchHelper;

    public QueueBottomSheet(RootActivity activity) {
        this.activity = activity;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void show() {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;
        if (RuntimeData.songs == null || RuntimeData.songs.isEmpty()) {
            return;
        }

        binding = LayoutQueueSheetBinding.inflate(LayoutInflater.from(activity));
        dialog = new BottomSheetDialog(activity);
        dialog.setContentView(binding.getRoot());

        dialog.setOnShowListener(d -> {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setFitToContents(true);
                behavior.setSkipCollapsed(true);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        int currentIndex = 0;
        MediaController controller = activity.getController();
        if (controller != null) {
            currentIndex = controller.getCurrentMediaItemIndex();
            if (currentIndex < 0 || currentIndex >= RuntimeData.songs.size()) {
                currentIndex = 0;
            }
        }

        updateSubtitle(currentIndex);

        adapter = new QueueAdapter(RuntimeData.songs, currentIndex, new QueueAdapter.OnQueueItemListener() {
            @Override
            public void onItemClick(int position) {
                if (position >= 0 && position < RuntimeData.songs.size()) {
                    activity.getLogicManager().playSong(position);
                    adapter.setCurrentPlayingIndex(position);
                    updateSubtitle(position);
                }
            }

            @Override
            public void onItemRemove(int position) {
                if (position >= 0 && position < RuntimeData.songs.size()) {
                    if (RuntimeData.songs.size() > 1) {
                        RuntimeData.songs.remove(position);
                        if (activity.getController() != null) {
                            try {
                                activity.getController().removeMediaItem(position);
                            } catch (Exception ignored) {}
                        }
                        activity.updateSongsQueue(RuntimeData.songs);
                        adapter.notifyItemRemoved(position);
                        int newCur = activity.getController() != null ? activity.getController().getCurrentMediaItemIndex() : 0;
                        if (newCur >= RuntimeData.songs.size()) newCur = 0;
                        adapter.setCurrentPlayingIndex(newCur);
                        updateSubtitle(newCur);
                    }
                }
            }

            @Override
            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                if (touchHelper != null) {
                    touchHelper.startDrag(viewHolder);
                }
            }
        });

        binding.queueRecycler.setLayoutManager(new LinearLayoutManager(activity));
        binding.queueRecycler.setAdapter(adapter);

        touchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPos = viewHolder.getBindingAdapterPosition();
                int toPos = target.getBindingAdapterPosition();
                if (fromPos < 0 || toPos < 0 || fromPos >= RuntimeData.songs.size() || toPos >= RuntimeData.songs.size()) {
                    return false;
                }
                Song moved = RuntimeData.songs.remove(fromPos);
                RuntimeData.songs.add(toPos, moved);
                adapter.notifyItemMoved(fromPos, toPos);

                if (activity.getController() != null) {
                    try {
                        activity.getController().moveMediaItem(fromPos, toPos);
                    } catch (Exception ignored) {}
                }
                activity.updateSongsQueue(RuntimeData.songs);

                int newCur = activity.getController() != null ? activity.getController().getCurrentMediaItemIndex() : 0;
                if (newCur >= RuntimeData.songs.size()) newCur = 0;
                adapter.setCurrentPlayingIndex(newCur);
                updateSubtitle(newCur);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }
        });
        touchHelper.attachToRecyclerView(binding.queueRecycler);

        binding.queueSheetClear.setOnClickListener(v -> {
            if (RuntimeData.songs != null && RuntimeData.songs.size() > 1) {
                int playingIdx = activity.getController() != null ? activity.getController().getCurrentMediaItemIndex() : 0;
                if (playingIdx < 0 || playingIdx >= RuntimeData.songs.size()) playingIdx = 0;
                Song current = RuntimeData.songs.get(playingIdx);
                RuntimeData.songs.clear();
                RuntimeData.songs.add(current);
                if (activity.getController() != null) {
                    try {
                        activity.getController().setMediaItems(
                                ExoPlayerManager.createMediaItems(RuntimeData.songs), 0, activity.getController().getCurrentPosition());
                    } catch (Exception ignored) {}
                }
                activity.updateSongsQueue(RuntimeData.songs);
                adapter.notifyDataSetChanged();
                adapter.setCurrentPlayingIndex(0);
                updateSubtitle(0);
            }
        });

        if (currentIndex > 0 && currentIndex < RuntimeData.songs.size()) {
            binding.queueRecycler.scrollToPosition(currentIndex);
        }

        dialog.show();
    }

    @SuppressLint("DefaultLocale")
    private void updateSubtitle(int currentIndex) {
        if (binding == null) return;
        int count = RuntimeData.songs != null ? RuntimeData.songs.size() : 0;
        String text = String.format("%d tracks • Now playing #%d", count, currentIndex + 1);
        binding.queueSheetSubtitle.setText(text);
    }
}

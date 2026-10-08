package com.xapps.media.xmusic.fragment;

import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDragHandleView;
import com.google.android.material.search.SearchView;
import com.xapps.media.xmusic.R;
import com.xapps.media.xmusic.activity.RootActivity;
import com.xapps.media.xmusic.activity.manager.UIManager;
import com.xapps.media.xmusic.callback.CallbackInterface;
import com.xapps.media.xmusic.callback.FragmentCallback;
import com.xapps.media.xmusic.common.SongLoadListener;
import com.xapps.media.xmusic.data.RuntimeData;
import com.xapps.media.xmusic.databinding.ActivityRootBinding;
import com.xapps.media.xmusic.databinding.FragmentSearchBinding;
import com.xapps.media.xmusic.databinding.SearchItemMiddleBinding;
import com.xapps.media.xmusic.helper.SongMetadataHelper;
import com.xapps.media.xmusic.helper.SongSearchHelper;
import com.xapps.media.xmusic.models.Song;
import com.xapps.media.xmusic.online.YoutubeMusicRepository;
import com.xapps.media.xmusic.utils.MaterialColorUtils;
import com.xapps.media.xmusic.utils.XUtils;
import com.xapps.media.xmusic.widget.ExpressiveSliderLayout;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchFragment extends BaseFragment implements FragmentCallback {

    public FragmentSearchBinding binding;
    private ActivityRootBinding activity;
    private RootActivity a;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private final Runnable searchRunnable = this::executeSearch;

    private int searchGeneration = 0;

    private boolean searchAll = true;
    private boolean searchOnline = false;
    private boolean searchTitle = false;
    private boolean searchArtist = false;
    private boolean searchAlbum = false;
    private boolean searchAlbumArtist = false;

    private String lastQuery = "";

    private int currentPos = -1;
    private int oldPos = -1;

    private int currentState = UIManager.LAYOUT_STATE_EXPOSE_BNV;

    private String currentSong = "null";
    private String oldSong = "null";

    private int imageSize;
    private long lastClickTime;
    private static final int DEBOUNCE_MS = 200;

    private SearchListAdapter searchAdapter;
    private SearchListAdapter landingAdapter;

    @NonNull
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        binding = FragmentSearchBinding.inflate(inflater, container, false);
        a = (RootActivity) getActivity();
        activity = a.getBinding();

        imageSize = XUtils.convertToPx(getActivity(), 50);

        initializeLogic();
        setupListeners();
        setupInsets();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSongs();
        loadLandingContent();
    }

    private void initializeLogic() {
        CallbackInterface.setSrFragCallback(this);

        binding.searchRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.searchRecycler.setItemAnimator(null);

        binding.recentsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recentsList.setItemAnimator(null);

        activity.bottomNavigation.post(() -> {
            if (getContext() == null || activity == null) return;
            int spacing = XUtils.convertToPx(requireContext(), activity.bottomNavigation.getHeight());
            binding.searchRecycler.addItemDecoration(new BottomSpacingDecoration(spacing));
            binding.recentsList.addItemDecoration(new BottomSpacingDecoration(spacing));
        });

        loadSongs();
        loadLandingContent();
    }

    private void setupInsets() {
        XUtils.setMargins(binding.searchBar, 0, XUtils.getStatusBarHeight(getActivity()), 0, 0);
    }

    private void setupListeners() {
        binding.searchView.setupWithSearchBar(binding.searchBar);

        binding.searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                lastQuery = s.toString();
                debounceSearch();
            }
        });

        binding.chipAll.setOnCheckedChangeListener((v, b) -> {
            if (b) {
                searchAll = true;
                searchOnline = false;
                searchTitle = false;
                searchArtist = false;
                searchAlbum = false;
                searchAlbumArtist = false;
                binding.chipOnline.setChecked(false);
                binding.chipTitle.setChecked(false);
                binding.chipArtist.setChecked(false);
                binding.chipAlbum.setChecked(false);
                binding.chipAlbumArtist.setChecked(false);
            }
            onSearchPreferencesChanged();
        });

        binding.chipOnline.setOnCheckedChangeListener((v, b) -> {
            if (b) {
                searchOnline = true;
                searchAll = false;
                searchTitle = false;
                searchArtist = false;
                searchAlbum = false;
                searchAlbumArtist = false;
                binding.chipAll.setChecked(false);
                binding.chipTitle.setChecked(false);
                binding.chipArtist.setChecked(false);
                binding.chipAlbum.setChecked(false);
                binding.chipAlbumArtist.setChecked(false);
            }
            onSearchPreferencesChanged();
        });

        binding.chipTitle.setOnCheckedChangeListener((v, b) -> {
            searchTitle = b;
            if (b) {
                searchAll = false;
                searchOnline = false;
                binding.chipAll.setChecked(false);
                binding.chipOnline.setChecked(false);
            }
            onSearchPreferencesChanged();
        });

        binding.chipArtist.setOnCheckedChangeListener((v, b) -> {
            searchArtist = b;
            if (b) {
                searchAll = false;
                searchOnline = false;
                binding.chipAll.setChecked(false);
                binding.chipOnline.setChecked(false);
            }
            onSearchPreferencesChanged();
        });

        binding.chipAlbum.setOnCheckedChangeListener((v, b) -> {
            searchAlbum = b;
            if (b) {
                searchAll = false;
                searchOnline = false;
                binding.chipAll.setChecked(false);
                binding.chipOnline.setChecked(false);
            }
            onSearchPreferencesChanged();
        });

        binding.chipAlbumArtist.setOnCheckedChangeListener((v, b) -> {
            searchAlbumArtist = b;
            if (b) {
                searchAll = false;
                searchOnline = false;
                binding.chipAll.setChecked(false);
                binding.chipOnline.setChecked(false);
            }
            onSearchPreferencesChanged();
        });

        binding.searchView.addTransitionListener((searchView, previousState, newState) -> {
            if (newState == SearchView.TransitionState.SHOWING) {
                currentState = UIManager.LAYOUT_STATE_EXPOSE_PLAYER_ONLY;
            } else if (newState == SearchView.TransitionState.HIDING) {
                currentState = UIManager.LAYOUT_STATE_EXPOSE_BNV;
            } else if (newState == SearchView.TransitionState.HIDDEN) {
                currentState = UIManager.LAYOUT_STATE_EXPOSE_BNV;
            }
        });
    }

    private void debounceSearch() {
        searchHandler.removeCallbacks(searchRunnable);
        searchHandler.postDelayed(searchRunnable, 250);
    }

    private void onSearchPreferencesChanged() {
        searchHandler.removeCallbacks(searchRunnable);
        executeSearch();
    }

    private void executeSearch() {
        final int generation = ++searchGeneration;
        final String query = lastQuery == null ? "" : lastQuery.trim();

        if (binding.chipOnline.isChecked()) {
            executor.execute(() -> {
                ArrayList<Song> results;
                try {
                    if (query.isEmpty()) {
                        results = YoutubeMusicRepository.getInstance().getTrendingMusic();
                    } else {
                        YoutubeMusicRepository.SearchPage page =
                                YoutubeMusicRepository.getInstance().search(query, null);
                        results = page.songs;
                    }
                } catch (Exception e) {
                    results = new ArrayList<>();
                }
                final ArrayList<Song> finalResults = results;
                mainHandler.post(() -> {
                    if (!isAdded() || generation != searchGeneration || searchAdapter == null) return;
                    searchAdapter.setData(finalResults);
                    binding.searchRecycler.scrollToPosition(0);
                });
            });
            return;
        }

        boolean titleOnly = binding.chipTitle.isChecked();
        boolean artistOnly = binding.chipArtist.isChecked();
        boolean albumOnly = binding.chipAlbumArtist.isChecked() || binding.chipAlbum.isChecked();
        boolean albumArtistOnly = binding.chipAlbumArtist.isChecked();
        boolean allMode = binding.chipAll.isChecked() || (!titleOnly && !artistOnly && !albumOnly && !albumArtistOnly);

        if (!allMode) {
            executor.execute(() -> {
                ArrayList<Song> localResults = SongSearchHelper.search(
                        query.toLowerCase(Locale.ROOT),
                        titleOnly, artistOnly, albumOnly, albumArtistOnly);
                mainHandler.post(() -> {
                    if (!isAdded() || generation != searchGeneration || searchAdapter == null) return;
                    searchAdapter.setData(localResults);
                    binding.searchRecycler.scrollToPosition(0);
                });
            });
            return;
        }

        if (query.isEmpty()) {
            ArrayList<Song> local = SongSearchHelper.search("", true, true, true, true);
            if (searchAdapter != null) {
                searchAdapter.setData(local);
                binding.searchRecycler.scrollToPosition(0);
            }
            return;
        }

        ArrayList<Song> localMatches = SongSearchHelper.search(
                query.toLowerCase(Locale.ROOT), true, true, true, true);
        if (searchAdapter != null) {
            searchAdapter.setData(localMatches);
            binding.searchRecycler.scrollToPosition(0);
        }

        executor.execute(() -> {
            try {
                YoutubeMusicRepository.SearchPage page =
                        YoutubeMusicRepository.getInstance().search(query, null);
                if (page.songs != null && !page.songs.isEmpty()) {
                    ArrayList<Song> combined = new ArrayList<>(localMatches);
                    for (Song os : page.songs) {
                        boolean duplicate = false;
                        for (Song ls : localMatches) {
                            if (ls.title != null && ls.title.equalsIgnoreCase(os.title)
                                    && ls.artist != null && ls.artist.equalsIgnoreCase(os.artist)) {
                                duplicate = true;
                                break;
                            }
                        }
                        if (!duplicate) {
                            combined.add(os);
                        }
                    }
                    mainHandler.post(() -> {
                        if (!isAdded() || generation != searchGeneration || searchAdapter == null) return;
                        searchAdapter.setData(combined);
                    });
                }
            } catch (Exception ignored) {}
        });
    }

    private void loadLandingContent() {
        executor.execute(() -> {
            ArrayList<Song> trending = null;
            try {
                trending = YoutubeMusicRepository.getInstance().getTrendingMusic();
            } catch (Exception ignored) {}

            final ArrayList<Song> suggestions = (trending != null && !trending.isEmpty())
                    ? trending
                    : new ArrayList<>(RuntimeData.localSongs);

            mainHandler.post(() -> {
                if (!isAdded() || getContext() == null) return;
                if (landingAdapter == null) {
                    landingAdapter = new SearchListAdapter(requireContext(), suggestions);
                    binding.recentsList.setAdapter(landingAdapter);
                } else {
                    landingAdapter.setData(suggestions);
                }
            });
        });
    }

    private void loadSongs() {
        executor.execute(() -> {
            SongMetadataHelper.getAllSongs(getActivity(), new SongLoadListener() {
                @Override
                public void onComplete(ArrayList<Song> list) {
                    if (list != null) {
                        RuntimeData.localSongs = list;
                    }
                    bindInitial();
                }
            });
        });
    }

    private void bindInitial() {
        if (!isAdded() || getContext() == null) return;
        mainHandler.post(() -> {
            ArrayList<Song> initial = SongSearchHelper.search("", true, true, true, true);
            if (searchAdapter == null) {
                searchAdapter = new SearchListAdapter(requireContext(), initial);
                binding.searchRecycler.setAdapter(searchAdapter);
            } else {
                searchAdapter.setData(initial);
            }
        });
    }

    public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.ViewHolder> {

        private static final int TYPE_SINGLE = -1;
        private static final int TYPE_TOP = 0;
        private static final int TYPE_MIDDLE = 1;
        private static final int TYPE_BOTTOM = 2;

        private final ArrayList<Song> data = new ArrayList<>();

        private final int c1 = MaterialColorUtils.colorPrimary;
        private final int c2 = MaterialColorUtils.colorSecondary;
        private final int c3 = MaterialColorUtils.colorOnSurface;
        private final int c4 = MaterialColorUtils.colorOutline;

        SearchListAdapter(Context c, ArrayList<Song> list) {
            setHasStableIds(true);
            data.addAll(list);
        }

        public ArrayList<Song> getData() {
            return data;
        }

        @Override
        public int getItemViewType(int position) {
            int size = getItemCount();

            if (size == 1) {
                return TYPE_SINGLE;
            }

            if (size == 2) {
                return position == 0 ? TYPE_TOP : TYPE_BOTTOM;
            }

            if (position == 0) return TYPE_TOP;
            if (position == size - 1) return TYPE_BOTTOM;
            return TYPE_MIDDLE;
        }

        @Override
        public long getItemId(int position) {
            return data.get(position).path.hashCode();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            int layout;
            if (viewType == TYPE_TOP) layout = R.layout.search_item_top;
            else if (viewType == TYPE_BOTTOM) layout = R.layout.search_item_bottom;
            else if (viewType == TYPE_SINGLE) layout = R.layout.search_item_single;
            else layout = R.layout.search_item_middle;

            return new ViewHolder(inflater.inflate(layout, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Song song = data.get(position);
            SearchItemMiddleBinding b = holder.binding;

            boolean active = song.path.equals(currentSong);

            if (a.getController() != null && !a.getController().isPlaying()) {
                b.vumeterView.pause();
            } else {
                b.vumeterView.resume();
            }

            b.item.setChecked(active);
            b.vumeterFrame.setVisibility(active ? View.VISIBLE : View.INVISIBLE);
            b.SongTitle.setTextColor(active ? c1 : c3);
            b.SongArtist.setTextColor(active ? c2 : c4);

            b.SongTitle.setText(song.title);
            b.SongArtist.setText(song.artist);

            Uri cover = song.getArtworkUri();

            Glide.with(b.songCover)
                    .load(cover)
                    .centerCrop()
                    .error(R.drawable.placeholder_small)
                    .fallback(R.drawable.placeholder_small)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .override(imageSize, imageSize)
                    .into(b.songCover);

            b.item.setOnClickListener(v -> {
                int pos = holder.getBindingAdapterPosition();
                currentPos = pos;
                if (pos == RecyclerView.NO_POSITION) return;
                if (a.getController() == null) return;
                if (activity.miniPlayer.getState() == ExpressiveSliderLayout.STATE_SETTLING
                        || activity.miniPlayer.getState() == ExpressiveSliderLayout.STATE_DRAGGING) return;
                long now = System.currentTimeMillis();
                if (now - lastClickTime < DEBOUNCE_MS) return;
                lastClickTime = now;

                String path = song.path;
                int realIndex = -1;
                for (int i = 0; i < RuntimeData.songs.size(); i++) {
                    if (RuntimeData.songs.get(i).path.equals(path)) {
                        realIndex = i;
                        break;
                    }
                }

                if (realIndex == -1 || RuntimeData.songs != data) {
                    RuntimeData.songs = new ArrayList<>(data);
                    a.updateSongsQueue(RuntimeData.songs);
                    realIndex = pos;
                }

                a.setSong(realIndex);
                updateActiveItem(realIndex);
                if (binding.searchView.isShowing()) {
                    binding.searchView.hide();
                }
            });

            b.optionsIcon.setOnClickListener(v -> {
                BottomSheetDragHandleView drag = new BottomSheetDragHandleView(getActivity());
                LinearLayout bsl = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.options_bottom_sheet, null);
                bsl.addView(drag, 0);
                BottomSheetDialog bsd = new BottomSheetDialog(getActivity());
                bsd.setContentView(bsl, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                bsd.show();
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        void setData(ArrayList<Song> newData) {
            DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new SongDiff(data, newData));
            data.clear();
            data.addAll(newData);
            diff.dispatchUpdatesTo(this);
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final SearchItemMiddleBinding binding;

            ViewHolder(View v) {
                super(v);
                binding = SearchItemMiddleBinding.bind(v);
            }
        }
    }

    static class SongDiff extends DiffUtil.Callback {

        private final ArrayList<Song> oldL;
        private final ArrayList<Song> newL;

        SongDiff(ArrayList<Song> o, ArrayList<Song> n) {
            oldL = o;
            newL = n;
        }

        @Override
        public int getOldListSize() {
            return oldL.size();
        }

        @Override
        public int getNewListSize() {
            return newL.size();
        }

        @Override
        public boolean areItemsTheSame(int o, int n) {
            return oldL.get(o).path.equals(newL.get(n).path);
        }

        @Override
        public boolean areContentsTheSame(int o, int n) {
            return oldL.get(o).equals(newL.get(n));
        }
    }

    public static class BottomSpacingDecoration extends RecyclerView.ItemDecoration {
        private final int bottomSpacing;

        BottomSpacingDecoration(int bottomSpacing) {
            this.bottomSpacing = bottomSpacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int pos = parent.getChildAdapterPosition(view);
            if (pos == RecyclerView.NO_POSITION) return;
            if (pos == state.getItemCount() - 1) {
                outRect.bottom = bottomSpacing;
            }
        }
    }

    @Override
    public void updateActiveItem(int pos) {
        currentPos = pos;
        oldSong = currentSong;
        currentSong = (pos >= 0 && pos < RuntimeData.songs.size()) ? RuntimeData.songs.get(pos).path : "null";
        if (searchAdapter != null) searchAdapter.notifyDataSetChanged();
        if (landingAdapter != null) landingAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateVumeter(boolean b) {
        if (binding == null) return;
        updateVumeterInRecyclerView(binding.searchRecycler, b);
        updateVumeterInRecyclerView(binding.recentsList, b);
    }

    private void updateVumeterInRecyclerView(RecyclerView rv, boolean isPlaying) {
        if (rv == null) return;
        for (int i = 0; i < rv.getChildCount(); i++) {
            RecyclerView.ViewHolder vh = rv.getChildViewHolder(rv.getChildAt(i));
            if (vh instanceof SearchListAdapter.ViewHolder) {
                SearchListAdapter.ViewHolder svh = (SearchListAdapter.ViewHolder) vh;
                if (svh.binding.item.isChecked()) {
                    if (isPlaying) svh.binding.vumeterView.resume();
                    else svh.binding.vumeterView.pause();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        searchHandler.removeCallbacksAndMessages(null);
        CallbackInterface.clearSrFragCallback(this);
        if (binding != null) {
            binding.recentsList.setAdapter(null);
            binding.searchRecycler.setAdapter(null);
        }
        searchAdapter = null;
        landingAdapter = null;
        binding = null;
        activity = null;
        a = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }

    @Override
    public SearchView.TransitionState getSearchViewState() {
        return binding == null ? SearchView.TransitionState.HIDDEN : binding.searchView.getCurrentTransitionState();
    }

    @Override
    public void hideSearchView() {
        if (binding != null) binding.searchView.hide();
    }

    @Override
    public void freeze(boolean b) {
        if (binding != null) binding.blockingOverlay.setClickable(b);
    }

    @Override
    public int getLayoutState() {
        return currentState;
    }

    public void focusSearch() {
        if (binding != null) {
            binding.searchView.show();
        }
    }

    public void performSearch(String query) {
        if (binding != null) {
            binding.searchView.show();
            if (query != null && !query.isEmpty()) {
                binding.searchView.setText(query);
            }
        }
    }

}

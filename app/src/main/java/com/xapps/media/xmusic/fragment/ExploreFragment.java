package com.xapps.media.xmusic.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.xapps.media.xmusic.R;
import com.xapps.media.xmusic.activity.RootActivity;
import com.xapps.media.xmusic.adapter.CommunityPlaylistAdapter;
import com.xapps.media.xmusic.adapter.ExploreCardAdapter;
import com.xapps.media.xmusic.adapter.QuickPicksAdapter;
import com.xapps.media.xmusic.data.RuntimeData;
import com.xapps.media.xmusic.databinding.FragmentExploreBinding;
import com.xapps.media.xmusic.models.ExploreCardItem;
import com.xapps.media.xmusic.models.Song;
import com.xapps.media.xmusic.online.YoutubeMusicRepository;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExploreFragment extends Fragment {

    private FragmentExploreBinding binding;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ExploreCardAdapter dancingAdapter;
    private ExploreCardAdapter featuredAdapter;
    private ExploreCardAdapter romanceAdapter;
    private CommunityPlaylistAdapter communityAdapter;
    private QuickPicksAdapter quickPicksAdapter;
    private ExploreCardAdapter nostalgicAdapter;
    private ExploreCardAdapter newReleasesAdapter;
    private ExploreCardAdapter morningsAdapter;

    private TextView selectedPill = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupGreeting();
        setupSearchBar();
        setupFilterPills();
        setupCategoryCards();
        setupCarousels();
        loadLiveContent();
    }

    private void setupGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String greeting;
        int iconRes = R.drawable.ic_wb_sunny;

        if (hour >= 5 && hour < 12) {
            greeting = "Good Morning";
        } else if (hour >= 12 && hour < 17) {
            greeting = "Good Afternoon";
        } else if (hour >= 17 && hour < 21) {
            greeting = "Good Evening";
        } else {
            greeting = "Good Night";
            iconRes = R.drawable.bedtime_24px;
        }

        binding.greetingText.setText(greeting);
        binding.greetingIcon.setImageResource(iconRes);
    }

    private void setupSearchBar() {
        View.OnClickListener openSearch = v -> {
            if (getActivity() instanceof RootActivity) {
                ((RootActivity) getActivity()).showSearchScreen("");
            }
        };

        binding.searchBarCard.setOnClickListener(openSearch);
        binding.searchHint.setOnClickListener(openSearch);
        binding.searchIcon.setOnClickListener(openSearch);

        binding.profileIcon.setOnClickListener(v -> {
            if (getActivity() instanceof RootActivity) {
                ((RootActivity) getActivity()).getBinding().bottomNavigation.setSelectedItemId(R.id.menuSettingsFragment);
            }
        });
    }

    private void setupFilterPills() {
        TextView[] pills = new TextView[]{
                binding.pillPodcasts, binding.pillFeelGood, binding.pillRomance,
                binding.pillEnergize, binding.pillWorkout, binding.pillParty,
                binding.pillFocus, binding.pillRelax
        };

        for (TextView pill : pills) {
            pill.setOnClickListener(v -> {
                if (selectedPill == pill) {
                    pill.setBackgroundResource(R.drawable.bg_filter_pill);
                    selectedPill = null;
                } else {
                    if (selectedPill != null) {
                        selectedPill.setBackgroundResource(R.drawable.bg_filter_pill);
                    }
                    pill.setBackgroundResource(R.drawable.bg_filter_pill_selected);
                    selectedPill = pill;
                    String query = pill.getText().toString();
                    playOrSearchCategory(query + " songs hits");
                }
            });
        }
    }

    private void setupCategoryCards() {
        binding.cardNewReleases.setOnClickListener(v -> playOrSearchCategory("New Releases Bollywood Punjabi"));
        binding.cardCharts.setOnClickListener(v -> playOrSearchCategory("Top 50 Charts Global India"));
        binding.cardMoods.setOnClickListener(v -> playOrSearchCategory("Top Moods Genres Music"));
        binding.cardPodcasts.setOnClickListener(v -> playOrSearchCategory("Best Podcasts Episodes"));
    }

    private void setupCarousels() {
        // 1. Dancing on your own
        List<ExploreCardItem> dancingList = new ArrayList<>();
        dancingList.add(new ExploreCardItem("d1", "Bollywood Fire", "Party Mix",
                "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=500&auto=format&fit=crop&q=80",
                "Bollywood Fire Dance"));
        dancingList.add(new ExploreCardItem("d2", "Punjabi Party", "Bhangra Hits",
                "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop&q=80",
                "Punjabi Party Karan Aujla Diljit"));
        dancingList.add(new ExploreCardItem("d3", "Bollywood Club", "Club Hits",
                "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=500&auto=format&fit=crop&q=80",
                "Bollywood Club Dance Hits"));
        dancingList.add(new ExploreCardItem("d4", "EDM Desi Energy", "EDM Mix",
                "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&auto=format&fit=crop&q=80",
                "Desi EDM Dance"));

        dancingAdapter = new ExploreCardAdapter(dancingList, false, this::onCardClicked);
        binding.recyclerDancing.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerDancing.setAdapter(dancingAdapter);

        // 2. Featured playlists for you
        List<ExploreCardItem> featuredList = new ArrayList<>();
        featuredList.add(new ExploreCardItem("f1", "Top Weekly Videos Tamil", "Tamil Hits",
                "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&auto=format&fit=crop&q=80",
                "Top Weekly Videos Tamil"));
        featuredList.add(new ExploreCardItem("f2", "Top Weekly Videos Haryanvi", "Haryanvi Hits",
                "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=500&auto=format&fit=crop&q=80",
                "Top Weekly Videos Haryanvi"));
        featuredList.add(new ExploreCardItem("f3", "Top Weekly Videos Hindi", "Bollywood Top",
                "https://images.unsplash.com/photo-1487180144351-b8472da7d491?w=500&auto=format&fit=crop&q=80",
                "Top Weekly Videos Hindi"));
        featuredList.add(new ExploreCardItem("f4", "Top Weekly International", "Global Top",
                "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=500&auto=format&fit=crop&q=80",
                "Top Weekly Videos International"));

        featuredAdapter = new ExploreCardAdapter(featuredList, false, this::onCardClicked);
        binding.recyclerFeatured.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerFeatured.setAdapter(featuredAdapter);

        // 3. Romance Right Now
        List<ExploreCardItem> romanceList = new ArrayList<>();
        romanceList.add(new ExploreCardItem("r1", "Bollywood Romantic Moments", "Love Hits",
                "https://images.unsplash.com/photo-1518895949257-7621c3c786d7?w=500&auto=format&fit=crop&q=80",
                "Bollywood Romantic Moments"));
        romanceList.add(new ExploreCardItem("r2", "00s Bollywood Romance", "00s Melodies",
                "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&auto=format&fit=crop&q=80",
                "00s Bollywood Romance"));
        romanceList.add(new ExploreCardItem("r3", "10s Bollywood Hits", "Love Ballads",
                "https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=500&auto=format&fit=crop&q=80",
                "10s Bollywood Hits Romance"));
        romanceList.add(new ExploreCardItem("r4", "Arijit Singh Romance", "Soulful Love",
                "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=500&auto=format&fit=crop&q=80",
                "Arijit Singh Romantic Songs"));

        romanceAdapter = new ExploreCardAdapter(romanceList, false, this::onCardClicked);
        binding.recyclerRomance.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerRomance.setAdapter(romanceAdapter);

        // 4. Trending Playlists
        List<ExploreCardItem> communityList = new ArrayList<>();
        communityList.add(new ExploreCardItem("c1", "Evening songs", "Tunexa Curator",
                "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500&auto=format&fit=crop&q=80",
                "Evening songs relaxing hindi"));
        communityList.add(new ExploreCardItem("c2", "Late Night Lo-Fi", "Lofi Curator",
                "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=500&auto=format&fit=crop&q=80",
                "Late Night Lo-Fi Bollywood"));
        communityList.add(new ExploreCardItem("c3", "Chill Monsoon Chai", "Acoustic Curator",
                "https://images.unsplash.com/photo-1515934751635-c81c6bc9a2d8?w=500&auto=format&fit=crop&q=80",
                "Chill Monsoon Chai Songs"));

        communityAdapter = new CommunityPlaylistAdapter(communityList, this::onCardClicked);
        binding.recyclerCommunity.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerCommunity.setAdapter(communityAdapter);

        // 5. Quick picks
        List<Song> quickList = new ArrayList<>();
        quickList.add(Song.youtube("https://www.youtube.com/watch?v=0kF3uO_7D74", "Casa Tupka Anthemo (feat. ...)", "Yo Yo Honey Singh", 210000L,
                "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=800&auto=format&fit=crop&q=80"));
        quickList.add(Song.youtube("https://www.youtube.com/watch?v=wXhTHyIgQ_U", "Parvati", "Sadhu Tiwari", 240000L,
                "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800&auto=format&fit=crop&q=80"));
        quickList.add(Song.youtube("https://www.youtube.com/watch?v=k4yXQkG2s1E", "Ashke", "Karan Aujla & Mxrci", 195000L,
                "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=800&auto=format&fit=crop&q=80"));
        quickList.add(Song.youtube("https://www.youtube.com/watch?v=V1Pl8CzNzCw", "Chalray Chalray Waal", "Kasif Kasif", 225000L,
                "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=800&auto=format&fit=crop&q=80"));

        quickPicksAdapter = new QuickPicksAdapter(quickList, (song, pos, all) -> playSongs(all, pos));
        binding.recyclerQuickPicks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerQuickPicks.setAdapter(quickPicksAdapter);

        binding.btnPlayAll.setOnClickListener(v -> {
            List<Song> songs = quickPicksAdapter.getItems();
            if (!songs.isEmpty()) playSongs(songs, 0);
        });

        // 6. Brb, Being Nostalgic!
        List<ExploreCardItem> nostalgicList = new ArrayList<>();
        nostalgicList.add(new ExploreCardItem("n1", "00s Chill: Tamil", "Tamil Nostalgia",
                "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=500&auto=format&fit=crop&q=80",
                "00s Chill Tamil"));
        nostalgicList.add(new ExploreCardItem("n2", "90s Bollywood Sad Songs", "Heartbreak Melodies",
                "https://images.unsplash.com/photo-1499415479124-43c32433a620?w=500&auto=format&fit=crop&q=80",
                "90s Bollywood Sad Songs"));
        nostalgicList.add(new ExploreCardItem("n3", "Chai, Baatein & Nostalgia", "Retro Golden Hits",
                "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?w=500&auto=format&fit=crop&q=80",
                "Retro Golden Old Hindi Hits"));
        nostalgicList.add(new ExploreCardItem("n4", "Evergreen Kishore Kumar", "Classics",
                "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=500&auto=format&fit=crop&q=80",
                "Kishore Kumar Evergreen Classics"));

        nostalgicAdapter = new ExploreCardAdapter(nostalgicList, false, this::onCardClicked);
        binding.recyclerNostalgic.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerNostalgic.setAdapter(nostalgicAdapter);

        // 7. New releases
        List<ExploreCardItem> newReleasesList = new ArrayList<>();
        newReleasesList.add(new ExploreCardItem("nr1", "AUJLA SZN 1", "Karan Aujla",
                "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&auto=format&fit=crop&q=80",
                "Karan Aujla new songs"));
        newReleasesList.add(new ExploreCardItem("nr2", "Ghostface Killah", "Sidhu Moose Wala & Mxrci",
                "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop&q=80",
                "Sidhu Moose Wala Ghostface Killah"));
        newReleasesList.add(new ExploreCardItem("nr3", "Jailer 2 Theme", "Anirudh Ravichander",
                "https://images.unsplash.com/photo-1518895949257-7621c3c786d7?w=500&auto=format&fit=crop&q=80",
                "Jailer 2 Anirudh new songs"));
        newReleasesList.add(new ExploreCardItem("nr4", "Barbaad", "Jubin Nautiyal",
                "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=500&auto=format&fit=crop&q=80",
                "Barbaad Jubin Nautiyal"));

        newReleasesAdapter = new ExploreCardAdapter(newReleasesList, true, this::onCardClicked);
        binding.recyclerNewReleases.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerNewReleases.setAdapter(newReleasesAdapter);

        // 8. Easy Mornings
        List<ExploreCardItem> morningsList = new ArrayList<>();
        morningsList.add(new ExploreCardItem("m1", "Punjabi Hip Hop Hits", "Morning Vibe",
                "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=500&auto=format&fit=crop&q=80",
                "Punjabi Hip Hop Hits"));
        morningsList.add(new ExploreCardItem("m2", "Pump-Up Pop", "Energy Hits",
                "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&auto=format&fit=crop&q=80",
                "Pump Up Pop Energy Songs"));
        morningsList.add(new ExploreCardItem("m3", "Easy Morning Acoustic", "Acoustic Guitar",
                "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&auto=format&fit=crop&q=80",
                "Easy Morning Acoustic Songs"));
        morningsList.add(new ExploreCardItem("m4", "Peaceful Sunrise", "Relaxing Morning",
                "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500&auto=format&fit=crop&q=80",
                "Peaceful Sunrise Chill"));

        morningsAdapter = new ExploreCardAdapter(morningsList, false, this::onCardClicked);
        binding.recyclerMornings.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerMornings.setAdapter(morningsAdapter);
    }

    private void onCardClicked(ExploreCardItem item) {
        if (item.directSong != null) {
            List<Song> singleList = new ArrayList<>();
            singleList.add(item.directSong);
            playSongs(singleList, 0);
            return;
        }
        playOrSearchCategory(item.searchQuery != null ? item.searchQuery : item.title);
    }

    private void playOrSearchCategory(String query) {
        Toast.makeText(getContext(), "Loading " + query + "...", Toast.LENGTH_SHORT).show();
        executor.execute(() -> {
            try {
                ArrayList<Song> songs = YoutubeMusicRepository.getInstance().getCategoryMusic(query);
                if (songs != null && !songs.isEmpty()) {
                    mainHandler.post(() -> playSongs(songs, 0));
                }
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Unable to fetch playlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void playSongs(List<Song> songs, int position) {
        if (songs == null || songs.isEmpty() || !(getActivity() instanceof RootActivity)) return;
        RootActivity activity = (RootActivity) getActivity();
        RuntimeData.songs = new ArrayList<>(songs);
        activity.updateSongsQueue(RuntimeData.songs);
        activity.setSong(position);
    }

    private void loadLiveContent() {
        executor.execute(() -> {
            try {
                // Fetch real Trending Songs for Quick Picks
                ArrayList<Song> trending = YoutubeMusicRepository.getInstance().getTrendingMusic();
                if (trending != null && !trending.isEmpty()) {
                    List<Song> topFour = new ArrayList<>();
                    for (int i = 0; i < Math.min(6, trending.size()); i++) {
                        topFour.add(trending.get(i));
                    }
                    mainHandler.post(() -> {
                        if (quickPicksAdapter != null && isAdded()) {
                            quickPicksAdapter.updateItems(topFour);
                        }
                    });
                }
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onDestroyView() {
        executor.shutdownNow();
        binding = null;
        super.onDestroyView();
    }
}

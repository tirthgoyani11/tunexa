package com.xapps.media.xmusic.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.xapps.media.xmusic.R;
import com.xapps.media.xmusic.BuildConfig;
import com.xapps.media.xmusic.activity.RootActivity;
import com.xapps.media.xmusic.databinding.FragmentAboutBinding;
import com.xapps.media.xmusic.utils.XUtils;

import java.util.Random;

public class AboutFragment extends SubFragment {
    private FragmentAboutBinding binding;
    private RootActivity activity;

    private static final String[] ABOUT_QUOTES = {
            "This app exists because why not :)",
            "Built one bug at a time",
            "Works on my device at least",
            "I'm NEVER rewriting this in compose",
            "If you’re reading this, the app didn’t crash",
            "WHERE IS SALADDDD!!!",
            "Technically, Nick owns 15% of this app, thanks to all his help :D",
            "Akane's annoying but nice guy, I hope he doesn't go bald",
            "Alex Gimme 500$ :P",
            "Use this till redead makes Accord 3...",
            "Another day, another workaround.",
            "You weren’t supposed to be here this often.",
            "Yes, this is intentional.",
            "'It’s stable' Yeah, no",
            "This screen does nothing productive.",
            "An About screen that knows it’s an About screen.",
            "This app has opinions.",
            "Thanks for using the app. Seriously.",
            "Curiosity killed the cat",
            "HELP IM BEING ABUSED (/jk)",
            "Fun fact, fully built on a mobile IDE so far :>\nUPDATE: not anymore I got anew PC :DD"
    };



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAboutBinding.inflate(inflater, container, false);
        activity = (RootActivity) getActivity();
        init();
        return binding.getRoot();
    }


    private void init() {
        setupUI();
        setupListeners();
    }

    private void setupUI() {
        binding.container.setPadding(binding.container.getPaddingLeft(), binding.container.getPaddingTop(), binding.container.getPaddingRight(), XUtils.getNavigationBarHeight(requireContext())*2);

        int i = new Random().nextInt(ABOUT_QUOTES.length);
        binding.randomNote.setText(ABOUT_QUOTES[i]);

        binding.appbar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            int totalScrollRange = appBarLayout.getTotalScrollRange();
            float progress = Math.abs(verticalOffset) / (float) totalScrollRange;
            progress = Math.min(1f, Math.max(0f, progress));
            binding.toolbarText.setAlpha(progress);

            binding.collapsingtoolbar.setScrimVisibleHeightTrigger(binding.collapsingtoolbar.getHeight());
            binding.collapsingtoolbar.setScrimAnimationDuration(0);
        });

        binding.toolbar.setNavigationOnClickListener(v -> {
            assert getActivity() != null;
            getActivity().getOnBackPressedDispatcher().onBackPressed();
            activity.getUIManager().hideComponents(activity.getUIManager().playerHidden, false, activity.getUIManager().tabsHidden, "AboutFragment");
        });

        binding.versionText.setText(BuildConfig.VERSION_NAME);
        binding.buildText.setText(BuildConfig.BUILD_TYPE);
    }

    private void setupListeners() {
        binding.thirdItem.setOnClickListener(v -> activity.showInfoDialog("Build Flavors", R.drawable.ic_info_outline, "Tunexa build flavors:\n\n- Debug: Logging and testing\n- Release: Optimized and fast", "Got it", binding.coordinator));
        binding.secondItem.setOnClickListener(v -> activity.showInfoDialog("Release types", R.drawable.ic_info_outline, "Tunexa release channels:\n\n- Alpha: Experimental\n- Stable: Production", "Got it", binding.coordinator));
        binding.fourthItem.setOnClickListener(v -> openFragment(new UsedLibsFragment()));
    }
    private void openLink(String link) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(link));
        startActivity(intent);
    }

    private void openFragment(Fragment f) {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.settings_frag, f)
                .addToBackStack(null)
                .commit();
    }
}

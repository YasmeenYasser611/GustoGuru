package com.example.gustoguru.features.search_intro.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.example.gustoguru.R;
import com.example.gustoguru.features.main.view.MainActivity;
import com.example.gustoguru.features.search.view.SearchFragment;
import com.example.gustoguru.features.search_intro.presenter.SearchLoadingPresenter;

import javax.annotation.Nullable;

public class SearchLoadingFragment extends Fragment implements SearchLoadingView {

    private SearchLoadingPresenter presenter;
    private LottieAnimationView animationView;
    private boolean hasNavigated = false;
    private boolean shouldSkipAnimation = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            shouldSkipAnimation = getArguments().getBoolean("skip_animation", false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_loading, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (shouldSkipAnimation) {
            navigateToSearch();
            return;
        }

        animationView = view.findViewById(R.id.search_loading_animation);
        try {
            animationView.setAnimation("search.json");
            animationView.setRepeatCount(0);
            animationView.setSpeed(2.5f);
            animationView.loop(false);

            presenter = new SearchLoadingPresenter(this);
            presenter.initialize();

            animationView.addAnimatorListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!hasNavigated && presenter != null) {
                        presenter.onAnimationComplete();
                    }
                }
            });

            animationView.playAnimation();

        } catch (Exception e) {
            showError("Animation setup failed: " + e.getMessage());
            navigateToSearch();
        }
    }



    @Override
    public void navigateToSearch() {
        if (hasNavigated || !isAdded() || isDetached() || getActivity() == null) return;
        hasNavigated = true;

        new Handler(Looper.getMainLooper()).post(() -> {
            if (getActivity() != null && !getActivity().isFinishing()) {
                ((MainActivity) getActivity()).replaceFragment(new SearchFragment(), false);
            }
        });
    }

    @Override
    public LottieAnimationView getAnimationView() {
        return animationView;
    }

    @Override
    public void showError(String message) {
        Log.e("SearchLoading", message);
        if (getActivity() != null) getActivity().onBackPressed();
    }

    @Override
    public void onDestroyView() {
        if (animationView != null) {
            animationView.cancelAnimation();
            animationView.removeAllAnimatorListeners();
        }
        if (presenter != null) {
            presenter.onDestroy();
            presenter.detachView();
        }
        super.onDestroyView();
    }
    @Override
    public void onResume() {
        super.onResume();
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Cancel the animation if it's playing
                if (animationView != null && animationView.isAnimating()) {
                    animationView.cancelAnimation();
                }

                // Immediately navigate back
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });
    }
}

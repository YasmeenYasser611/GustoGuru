package com.example.gustoguru.features.weekly_planner_intro.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.example.gustoguru.R;
import com.example.gustoguru.features.main.view.MainActivity;
import com.example.gustoguru.features.weekly_planner.view.PlannedFragment;
import com.example.gustoguru.features.weekly_planner_intro.presenter.WeeklyLoadingPresenter;

import javax.annotation.Nullable;

public class WeeklyLoadingFragment extends Fragment implements WeeklyLoadingView {

    private WeeklyLoadingPresenter presenter;
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
        return inflater.inflate(R.layout.fragment_weekly_loading, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (shouldSkipAnimation) {
            navigateToWeeklyPlanner();
            return;
        }

        animationView = view.findViewById(R.id.weekly_loading_animation);
        try {
            // Set your weekly planner animation JSON file
            animationView.setAnimation("weekly_planner.json");
            animationView.setRepeatCount(0);
            animationView.setSpeed(2.5f);
            animationView.loop(false);

            presenter = new WeeklyLoadingPresenter(this);
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
            navigateToWeeklyPlanner();
        }
    }

    @Override
    public void navigateToWeeklyPlanner() {
        if (hasNavigated || !isAdded() || isDetached() || getActivity() == null) {
            return;
        }
        hasNavigated = true;

        new Handler(Looper.getMainLooper()).post(() -> {
            if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                ((MainActivity) getActivity()).replaceFragment(new PlannedFragment(), false);
            }
        });
    }

    @Override
    public LottieAnimationView getAnimationView() {
        return animationView;
    }

    @Override
    public void showError(String message) {
        Log.e("WeeklyLoading", message);
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
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
}
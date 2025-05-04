package com.example.gustoguru.features.favorites_intro.presenter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Handler;
import android.os.Looper;

import com.airbnb.lottie.LottieAnimationView;
import com.example.gustoguru.features.favorites_intro.view.FavLoadingView;
import com.example.gustoguru.features.weekly_planner_intro.view.WeeklyLoadingView;

public class FavLoadingPresenter {
    private static final long MIN_LOADING_DURATION = 500;
    private static final long MAX_LOADING_DURATION = 1000;
    private long startTime;
    private FavLoadingView view;
    private Handler handler = new Handler(Looper.getMainLooper());

    public FavLoadingPresenter(FavLoadingView view) {
        this.view = view;
    }

    public void initialize() {
        this.startTime = System.currentTimeMillis();
        setupAnimationListener();
    }

    private void setupAnimationListener() {
        LottieAnimationView animationView = view.getAnimationView();
        if (animationView != null) {
            animationView.removeAllAnimatorListeners();

            animationView.addAnimatorListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animationView.removeAnimatorListener(this);
                    handleAnimationCompletion();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    animationView.removeAnimatorListener(this);
                    if (view != null) {
                        view.navigateToFavorites();
                    }
                }
            });
        } else {
            if (view != null) {
                view.navigateToFavorites();
            }
        }
    }

    private void handleAnimationCompletion() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        long remainingTime = MIN_LOADING_DURATION - elapsedTime;

        if (remainingTime > 0) {
            handler.postDelayed(() -> {
                if (view != null) {
                    view.navigateToFavorites();
                }
            }, Math.min(remainingTime, MAX_LOADING_DURATION - elapsedTime));
        } else {
            if (view != null) {
                view.navigateToFavorites();
            }
        }
    }

    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        view = null;
    }

    public void onAnimationComplete() {
        if (view != null) {
            view.navigateToFavorites();
        }
    }

    public void detachView() {
        view = null;
    }
}
package com.example.gustoguru.features.profileIntro.presenter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Handler;
import android.os.Looper;

import com.airbnb.lottie.LottieAnimationView;
import com.example.gustoguru.features.profileIntro.view.ProfileLoadingFragment;
import com.example.gustoguru.features.profileIntro.view.ProfileLoadingView;
public class ProfileLoadingPresenter {
    private static final long MIN_LOADING_DURATION = 500;
    private static final long MAX_LOADING_DURATION = 1000;
    private long startTime;
    private ProfileLoadingView view;
    private Handler handler = new Handler(Looper.getMainLooper());

    public ProfileLoadingPresenter(ProfileLoadingView view) {
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
                        view.navigateToProfile();
                    }
                }
            });
        } else {
            if (view != null) {
                view.navigateToProfile();
            }
        }
    }

    private void handleAnimationCompletion() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        long remainingTime = MIN_LOADING_DURATION - elapsedTime;

        if (remainingTime > 0) {
            handler.postDelayed(() -> {
                if (view != null) {
                    view.navigateToProfile();
                }
            }, Math.min(remainingTime, MAX_LOADING_DURATION - elapsedTime));
        } else {
            if (view != null) {
                view.navigateToProfile();
            }
        }
    }

    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        view = null;
    }
    public void onAnimationComplete() {
        // This gets called when animation finishes
        view.navigateToProfile();
    }

    public void detachView() {
        view=null;
    }
}
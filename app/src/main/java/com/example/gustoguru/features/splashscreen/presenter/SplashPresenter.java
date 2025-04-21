package com.example.gustoguru.features.splashscreen.presenter;




import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Handler;

import com.airbnb.lottie.LottieAnimationView;
import com.example.gustoguru.features.splashscreen.view.Splashview;

public class SplashPresenter  {
    private static final long MIN_SPLASH_DURATION = 2000;
    private long startTime;
    private Splashview view;
    private Handler handler = new Handler();

    public SplashPresenter(Splashview view) {
        this.view = view;
    }


    public void initialize() {
        this.startTime = System.currentTimeMillis();
        setupAnimationListener();
    }

    private void setupAnimationListener() 
    {
        LottieAnimationView animationView = view.getAnimationView();
        if (animationView != null)
        {
            animationView.addAnimatorListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    handleAnimationCompletion();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    view.navigateToLogin();
                }
            });
        }
        else
        {

            view.navigateToLogin();
        }
    }

    private void handleAnimationCompletion()
    {
        long elapsedTime = System.currentTimeMillis() - startTime;
        long remainingTime = MIN_SPLASH_DURATION - elapsedTime;

        if (remainingTime > 0) {
            handler.postDelayed(() -> view.navigateToLogin(), remainingTime);
        } else {
            view.navigateToLogin();
        }
    }


    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        view = null;
    }
}
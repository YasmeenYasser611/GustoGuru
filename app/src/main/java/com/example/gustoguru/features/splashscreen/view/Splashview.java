package com.example.gustoguru.features.splashscreen.view;

import com.airbnb.lottie.LottieAnimationView;

public interface Splashview
{
    void navigateToLogin();
    void navigateToMain();
    void showError(String message);
    LottieAnimationView getAnimationView();
}

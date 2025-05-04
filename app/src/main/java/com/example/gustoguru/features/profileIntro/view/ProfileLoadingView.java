package com.example.gustoguru.features.profileIntro.view;

import com.airbnb.lottie.LottieAnimationView;

public interface ProfileLoadingView {
    LottieAnimationView getAnimationView();
    void navigateToProfile();
    void showError(String message);
}

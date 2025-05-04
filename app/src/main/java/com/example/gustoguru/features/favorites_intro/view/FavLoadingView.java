package com.example.gustoguru.features.favorites_intro.view;

import com.airbnb.lottie.LottieAnimationView;

public interface FavLoadingView {
    void navigateToFavorites();
    LottieAnimationView getAnimationView();
    void showError(String message);
}
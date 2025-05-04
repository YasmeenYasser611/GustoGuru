package com.example.gustoguru.features.search_intro.view;

import com.airbnb.lottie.LottieAnimationView;

public interface SearchLoadingView {
    void navigateToSearch();
    LottieAnimationView getAnimationView();
    void showError(String message);
}

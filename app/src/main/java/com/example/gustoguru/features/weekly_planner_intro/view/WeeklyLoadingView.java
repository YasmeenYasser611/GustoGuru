package com.example.gustoguru.features.weekly_planner_intro.view;

import com.airbnb.lottie.LottieAnimationView;

public interface WeeklyLoadingView {
    void navigateToWeeklyPlanner();
    LottieAnimationView getAnimationView();
    void showError(String message);
}
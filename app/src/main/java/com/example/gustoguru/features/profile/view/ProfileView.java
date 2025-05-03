package com.example.gustoguru.features.profile.view;

import android.content.Context;

public interface ProfileView {
    void showUserProfile(String name, String email);
    void updateNameDisplay(String newName);
    void showLoading();
    void hideLoading();
    void showError(String message);
    void navigateToLogin();

}

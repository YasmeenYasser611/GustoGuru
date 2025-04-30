package com.example.gustoguru.features.profile.view;

public interface ProfileView {
    void showUserProfile(String name, String email);
    void updateNameDisplay(String newName);
    void showLoading();
    void hideLoading();
    void showError(String message);
    void navigateToLogin();
}

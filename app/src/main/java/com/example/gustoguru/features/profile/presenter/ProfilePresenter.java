package com.example.gustoguru.features.profile.presenter;

import com.example.gustoguru.features.profile.view.ProfileView;
import com.example.gustoguru.model.sessionmanager.SessionManager;
import com.example.gustoguru.model.repository.MealRepository;

public class ProfilePresenter  {
    private ProfileView view;
    private final MealRepository repository;
    private final SessionManager sessionManager;

    public ProfilePresenter(ProfileView view, MealRepository repository,
                            SessionManager sessionManager) {
        this.view = view;
        this.repository = repository;
        this.sessionManager = sessionManager;
    }


    public void loadUserProfile() {
        view.showLoading();
        repository.getUserProfile(new MealRepository.OnProfileDataCallback() {
            @Override
            public void onSuccess(String name, String email) {
                if (!name.isEmpty() && !name.equals(sessionManager.getUserName())) {
                    sessionManager.updateUserName(name);
                }
                view.hideLoading();
                view.showUserProfile(name, email);
            }

            @Override
            public void onFailure(Exception e) {
                view.hideLoading();
                view.showError("Failed to load profile: " + e.getMessage());
            }
        });
    }


    public void updateUserName(String newName) {
        if (newName.isEmpty()) {
            view.showError("Name cannot be empty");
            return;
        }

        view.showLoading();
        repository.updateUserName(newName, new MealRepository.OnUpdateCallback() {
            @Override
            public void onSuccess() {
                sessionManager.updateUserName(newName);
                view.hideLoading();
                view.updateNameDisplay(newName);
            }

            @Override
            public void onFailure(Exception e) {
                view.hideLoading();
                view.showError("Failed to update name: " + e.getMessage());
            }
        });
    }


    public void logout() {
        repository.logout();
        sessionManager.logoutUser();
        view.navigateToLogin();
    }


    public void detachView() {
        this.view = null;
    }
    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

}
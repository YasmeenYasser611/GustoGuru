package com.example.gustoguru.features.navigation.view;

public interface NavigationCommunicator {
    void navigateToSearch();
    void navigateToHome();
    void navigateToPlannedMeals();
    void navigateToFavorites();
    void navigateToProfile();
    void navigateToLogin();
    void showLoginRequiredDialog(String message);
    void navigateToMealDetail(String mealId);


}
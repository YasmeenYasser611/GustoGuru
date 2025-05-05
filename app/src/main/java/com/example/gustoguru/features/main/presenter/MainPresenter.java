package com.example.gustoguru.features.main.presenter;

import androidx.fragment.app.Fragment;

import com.example.gustoguru.R;
import com.example.gustoguru.features.favorites.view.FavoritesFragment;
import com.example.gustoguru.features.home.view.HomeFragment;
import com.example.gustoguru.features.main.view.Mainview;
import com.example.gustoguru.features.meal.view.MealDetailFragment;
import com.example.gustoguru.features.profile.view.ProfileFragment;
import com.example.gustoguru.features.search.view.SearchFragment;
import com.example.gustoguru.model.sessionmanager.SessionManager;
import com.example.gustoguru.features.weekly_planner.view.PlannedFragment;

public class MainPresenter  {
    private final Mainview view;
    private final SessionManager sessionManager;

    public MainPresenter(Mainview view, SessionManager sessionManager) {
        this.view = view;
        this.sessionManager = sessionManager;
    }


    public boolean isUserLoggedIn() {
        return sessionManager.isLoggedIn();
    }


    public void handleNavigation(int menuItemId) {
        if (menuItemId == R.id.nav_home) {
            view.replaceFragment(new HomeFragment(), false);
        }
        else if (menuItemId == R.id.nav_search) {
            view.replaceFragment(new SearchFragment(), true);
        }
        else if (menuItemId == R.id.nav_planner) {
            handleProtectedNavigation(new PlannedFragment(), "Please login to view planned meals");
        }
        else if (menuItemId == R.id.nav_fav) {
            handleProtectedNavigation(new FavoritesFragment(), "Please login to view favorites");
        }
        else if (menuItemId == R.id.nav_profile) {
            handleProtectedNavigation(new ProfileFragment(), "Please login to view profile");
        }
    }

    private void handleProtectedNavigation(Fragment fragment, String message) {
        if (isUserLoggedIn()) {
            view.replaceFragment(fragment, true);
        } else {
            view.showAlertDialog("Login Required", message);
        }
    }


    public void navigateToMealDetail(String mealId) {
        view.replaceFragment(MealDetailFragment.newInstance(mealId), true);
    }


    public void checkBackStack() {
        // Logic would be implemented here if needed
    }
}
package com.example.gustoguru.features.navigation.presenter;

import android.content.Context;

import com.example.gustoguru.R;
import com.example.gustoguru.features.navigation.view.NavigationCommunicator;
import com.example.gustoguru.model.sessionmanager.SessionManager;
public class NavigationPresenter {
    private final NavigationCommunicator communicator;
    private final SessionManager sessionManager;

    public NavigationPresenter(NavigationCommunicator communicator, Context context) {
        this.communicator = communicator;
        this.sessionManager = new SessionManager(context);
    }

    public boolean handleNavigationItemSelected(int itemId) {
        if (itemId == R.id.nav_home) {
            communicator.navigateToHome();
            return true;
        }
        else if (itemId == R.id.nav_search) {
            communicator.navigateToSearch();
            return true;
        }
        else if (itemId == R.id.nav_fav) {
            if (!sessionManager.isLoggedIn()) {
                communicator.showLoginRequiredDialog("Please login to view favorites");
                return false;
            }
            communicator.navigateToFavorites();
            return true;
        }
        else if (itemId == R.id.nav_planner) {
            if (!sessionManager.isLoggedIn()) {
                communicator.showLoginRequiredDialog("Please login to view planner");
                return false;
            }
            communicator.navigateToPlannedMeals();
            return true;
        }
        else if (itemId == R.id.nav_profile) {
            if (!sessionManager.isLoggedIn()) {
                communicator.showLoginRequiredDialog("Please login to view your profile");
                return false;
            }
            communicator.navigateToProfile();
            return true;
        }
        return false;
    }
}
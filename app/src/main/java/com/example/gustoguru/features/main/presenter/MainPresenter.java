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
    private Mainview view;
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
            view.navigateToSearch();
        }
        else if (menuItemId == R.id.nav_planner) {

            if (isUserLoggedIn())
            {
                view.navigateToPlanner();
            } else {
                view.showAlertDialog("Login Required", "Please login to view planned meals");
            }
        }
        else if (menuItemId == R.id.nav_fav)
        {
            if (isUserLoggedIn())
            {
                view.navigateToFav();
            } else {
                view.showAlertDialog("Login Required", "Please login to view favorites");
            }
        }
        else if (menuItemId == R.id.nav_profile) {
            if (isUserLoggedIn())
            {
            view.navigateToProfile();
            } else {
                view.showAlertDialog("Login Required", "Please login to view profile");
            }
        }
    }




    public void navigateToMealDetail(String mealId) {
        view.replaceFragment(MealDetailFragment.newInstance(mealId), true);
    }


    public void checkBackStack() {
        // Logic would be implemented here if needed
    }
    public void onDestroy() {

        view = null;
    }
}
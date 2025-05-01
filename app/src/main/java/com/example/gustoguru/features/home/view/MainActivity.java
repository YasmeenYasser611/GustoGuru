package com.example.gustoguru.features.home.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.gustoguru.R;
//import com.example.gustoguru.features.authentication.login.view.LoginActivity;
import com.example.gustoguru.features.authentication.login.view.LoginActivity;
import com.example.gustoguru.features.favorites.view.FavoritesActivity;
import com.example.gustoguru.features.meal.view.MealDetailActivity;
import com.example.gustoguru.features.profile.view.ProfileActivity;
import com.example.gustoguru.features.search.view.SearchActivity;
import com.example.gustoguru.features.sessionmanager.SessionManager;
import com.example.gustoguru.features.weekly_planner.view.PlannedActivity;

public class MainActivity extends AppCompatActivity implements NavigationCommunicator, HomeCommunicator{
    private static final String TAG = "MainActivity";
    private FragmentManager fragmentManager;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Activity created");

        sessionManager = new SessionManager(this);
        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            // Add Navigation Fragment (static - always visible)
            fragmentManager.beginTransaction()
                    .add(R.id.bottom_nav_container, new NavigationFragment(), "NAV_FRAGMENT")
                    .commit();

            // Start with Home Fragment
            replaceFragment(new HomeFragment(), false);
        }
    }

    @Override
    public void navigateToSearch() {
        // Temporary activity launch
        startActivity(new Intent(this, SearchActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void navigateToHome() {
        replaceFragment(new HomeFragment(), false);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void navigateToPlannedMeals() {
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, PlannedActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    @Override
    public void navigateToFavorites() {
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, FavoritesActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    @Override
    public void showLoginRequiredDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Login Required")
                .setMessage(message)
                .setPositiveButton("Login", (dialog, which) -> navigateToLogin())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void navigateToMealDetail(String mealId) {
        Intent intent = new Intent(this, MealDetailActivity.class);
        intent.putExtra("MEAL_ID", mealId);
        startActivity(intent);
    }

    @Override
    public void navigateToProfile() {
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else {
            navigateToLogin();
        }
    }



    @Override
    public void navigateToLogin() {
        // Temporary activity launch
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }




    private void replaceFragment(Fragment fragment, boolean addToBackStack) {
        try {
            // Check if fragment container exists
            if (findViewById(R.id.fragment_container) == null) {
                Log.e(TAG, "Fragment container not found!");
                return;
            }

            Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
            if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
                return; // Don't replace if same fragment is already shown
            }

            FragmentTransaction transaction = fragmentManager.beginTransaction();

            // Use animations only if fragment container exists
            if (findViewById(R.id.fragment_container) != null) {
                transaction.setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                );
            }

            transaction.replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName());

            if (addToBackStack) {
                transaction.addToBackStack(fragment.getClass().getSimpleName());
            }

            transaction.commitAllowingStateLoss(); // Safer than commit()

        } catch (Exception e) {
            Log.e(TAG, "Fragment transaction failed", e);
        }
    }
}
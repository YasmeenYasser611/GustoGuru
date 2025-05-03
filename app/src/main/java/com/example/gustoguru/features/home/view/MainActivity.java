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
import com.example.gustoguru.features.NetworkStatus.view.NetworkStatusFragment;
import com.example.gustoguru.features.authentication.login.view.LoginActivity;
import com.example.gustoguru.features.favorites.view.FavoritesFragment;
import com.example.gustoguru.features.meal.view.MealDetailFragment;
import com.example.gustoguru.features.profile.view.ProfileFragment;
import com.example.gustoguru.features.search.view.SearchFragment;
import com.example.gustoguru.features.sessionmanager.SessionManager;
import com.example.gustoguru.features.weekly_planner.view.PlannedFragment;

public class MainActivity extends AppCompatActivity implements NavigationCommunicator, HomeCommunicator{
    private static final String TAG = "MainActivity";
    private FragmentManager fragmentManager;
    private SessionManager sessionManager;

    private NavigationFragment navigationFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Activity created");

        sessionManager = new SessionManager(this);
        fragmentManager = getSupportFragmentManager();

        // In your MainActivity's onCreate() after setContentView()
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.network_status_container, new NetworkStatusFragment())
                .commit();

        if (savedInstanceState == null) {
            // Add Navigation Fragment
            navigationFragment = new NavigationFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.bottom_nav_container, navigationFragment, "NAV_FRAGMENT")
                    .commit();

            // Start with Home Fragment
            replaceFragment(new HomeFragment(), false);
        } else {
            // Restore reference to existing navigation fragment
            navigationFragment = (NavigationFragment) fragmentManager.findFragmentByTag("NAV_FRAGMENT");
        }
        setupBackStackListener();
    }

    @Override
    public void navigateToSearch() {
        replaceFragment(new SearchFragment(), true);
    }

    @Override
    public void navigateToHome() {
        replaceFragment(new HomeFragment(), false);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void navigateToPlannedMeals() {
        if (sessionManager.isLoggedIn()) {
            replaceFragment(new PlannedFragment(), true);
        } else {
            showLoginRequiredDialog("Please login to view planned meals");
        }
    }

    @Override
    public void navigateToFavorites() {
        if (sessionManager.isLoggedIn()) {
            replaceFragment(new FavoritesFragment(), true);
        } else {
            showLoginRequiredDialog("Please login to view favorites");
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
    public void navigateToMealDetail(String mealId) {
        // Use fragment instead of activity
        MealDetailFragment fragment = MealDetailFragment.newInstance(mealId);
        replaceFragment(fragment, true);

        // Optional: Add custom transition animation
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,  // enter
                        R.anim.fade_out,        // exit
                        R.anim.fade_in,         // popEnter
                        R.anim.slide_out_right   // popExit
                )
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void navigateToProfile() {
        if (sessionManager.isLoggedIn()) {
            replaceFragment(new ProfileFragment(), true);
        } else {
//            navigateToLogin();
            showLoginRequiredDialog("Please login to view favorites");
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

    private void setupBackStackListener() {
        fragmentManager.addOnBackStackChangedListener(() -> {
            updateBottomNavigation();
        });
    }

    private void updateBottomNavigation() {
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (navigationFragment != null && currentFragment != null) {
            navigationFragment.updateSelectedItem(getNavigationItemId(currentFragment));
        }
    }

    private int getNavigationItemId(Fragment fragment) {
        if (fragment instanceof HomeFragment) {
            return R.id.nav_home;
        } else if (fragment instanceof SearchFragment) {
            return R.id.nav_search;
        } else if (fragment instanceof PlannedFragment) {
            return R.id.nav_planner;
        } else if (fragment instanceof FavoritesFragment) {
            return R.id.nav_fav;
        } else if (fragment instanceof ProfileFragment) {
            return R.id.nav_profile;
        }
        return -1; // No matching item
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
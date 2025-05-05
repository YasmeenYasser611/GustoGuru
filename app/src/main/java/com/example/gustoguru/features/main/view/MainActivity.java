package com.example.gustoguru.features.main.view;

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
import com.example.gustoguru.features.home.view.HomeCommunicator;
import com.example.gustoguru.features.home.view.HomeFragment;
import com.example.gustoguru.features.main.presenter.MainPresenter;
import com.example.gustoguru.features.navigation.view.NavigationCommunicator;
import com.example.gustoguru.features.navigation.view.NavigationFragment;
import com.example.gustoguru.features.profile.view.ProfileFragment;
import com.example.gustoguru.features.search.view.SearchFragment;
import com.example.gustoguru.model.sessionmanager.SessionManager;
import com.example.gustoguru.features.weekly_planner.view.PlannedFragment;
public class MainActivity extends AppCompatActivity implements
        Mainview,
        NavigationCommunicator,
        HomeCommunicator {

    private static final String TAG = "MainActivity";
    private FragmentManager fragmentManager;
    private MainPresenter presenter;
    private NavigationFragment navigationFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Activity created");

        SessionManager sessionManager = new SessionManager(this);
        presenter = new MainPresenter(this, sessionManager);
        fragmentManager = getSupportFragmentManager();

        // Setup network status fragment
        fragmentManager.beginTransaction()
                .replace(R.id.network_status_container, new NetworkStatusFragment())
                .commit();

        if (savedInstanceState == null) {
            navigationFragment = new NavigationFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.bottom_nav_container, navigationFragment, "NAV_FRAGMENT")
                    .commit();

            replaceFragment(new HomeFragment(), false);
        } else {
            navigationFragment = (NavigationFragment) fragmentManager.findFragmentByTag("NAV_FRAGMENT");
        }

        setupBackStackListener();
    }

    // Implement MainContract.View methods
    @Override
    public void replaceFragment(Fragment fragment, boolean addToBackStack) {
        try {
            if (findViewById(R.id.fragment_container) == null) {
                Log.e(TAG, "Fragment container not found!");
                return;
            }

            Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
            if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
                return;
            }

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            setupFragmentAnimations(transaction);

            transaction.replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName());

            if (addToBackStack) {
                transaction.addToBackStack(fragment.getClass().getSimpleName());
            }

            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            Log.e(TAG, "Fragment transaction failed", e);
        }
    }

    @Override
    public void setupFragmentAnimations(FragmentTransaction transaction) {
        transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );
    }

    @Override
    public void showLoginScreen() {
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Login", (dialog, which) -> showLoginScreen())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void updateBottomNavigation(int itemId) {
        if (navigationFragment != null) {
            navigationFragment.updateSelectedItem(itemId);
        }
    }

    // Implement NavigationCommunicator methods
    @Override
    public void navigateToSearch() {
        presenter.handleNavigation(R.id.nav_search);
    }

    @Override
    public void navigateToHome() {
        presenter.handleNavigation(R.id.nav_home);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void navigateToPlannedMeals() {
        presenter.handleNavigation(R.id.nav_planner);
    }

    @Override
    public void navigateToFavorites() {
        presenter.handleNavigation(R.id.nav_fav);
    }

    @Override
    public void navigateToProfile() {
        presenter.handleNavigation(R.id.nav_profile);
    }

    @Override
    public void navigateToLogin() {

    }

    // Implement HomeCommunicator method
    @Override
    public void navigateToMealDetail(String mealId) {
        presenter.navigateToMealDetail(mealId);
    }

    @Override
    public void showLoginRequiredDialog(String message) {
        showAlertDialog("Login Required", message);
    }

    // Other existing methods
    private void setupBackStackListener() {
        fragmentManager.addOnBackStackChangedListener(() -> {
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
            if (currentFragment != null) {
                int itemId = getNavigationItemId(currentFragment);
                updateBottomNavigation(itemId);
            }
        });
    }

    private int getNavigationItemId(Fragment fragment) {
        if (fragment instanceof HomeFragment) return R.id.nav_home;
        if (fragment instanceof SearchFragment) return R.id.nav_search;
        if (fragment instanceof PlannedFragment) return R.id.nav_planner;
        if (fragment instanceof FavoritesFragment) return R.id.nav_fav;
        if (fragment instanceof ProfileFragment) return R.id.nav_profile;
        return -1;
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
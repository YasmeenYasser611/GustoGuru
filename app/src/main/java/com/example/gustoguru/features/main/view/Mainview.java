package com.example.gustoguru.features.main.view;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public interface Mainview {
    void replaceFragment(Fragment fragment, boolean addToBackStack);
    void showLoginScreen();
    void showAlertDialog(String title, String message);
    void updateBottomNavigation(int itemId);
    void setupFragmentAnimations(FragmentTransaction transaction);
    void navigateToProfile();
    void navigateToPlanner();
    void navigateToFav();
    void navigateToSearch();
}

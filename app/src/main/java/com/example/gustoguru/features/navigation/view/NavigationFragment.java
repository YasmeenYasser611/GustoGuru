package com.example.gustoguru.features.navigation.view;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gustoguru.R;
import com.example.gustoguru.features.sessionmanager.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationFragment extends Fragment
{
    private NavigationCommunicator communicator;
    private BottomNavigationView bottomNav;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof NavigationCommunicator) {
            communicator = (NavigationCommunicator) context;
        } else {
            throw new RuntimeException(context + " must implement NavigationCommunicator");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomNav = view.findViewById(R.id.bottomNav);
        setupNavigation();
    }

    private void setupNavigation() {
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            SessionManager sessionManager = new SessionManager(requireActivity());

            if (id == R.id.nav_home) {
                communicator.navigateToHome();
                return true;
            }
            else if (id == R.id.nav_search) {
                communicator.navigateToSearch();
                return true;
            }
            else if (id == R.id.nav_fav) {
                if (!sessionManager.isLoggedIn()) {
                    communicator.showLoginRequiredDialog("Please login to view favorites");
                    return false;
                }
                communicator.navigateToFavorites();
                return true;
            }
            else if (id == R.id.nav_planner) {
                if (!sessionManager.isLoggedIn()) {
                    communicator.showLoginRequiredDialog("Please login to view planner");
                    return false;
                }
                communicator.navigateToPlannedMeals();
                return true;
            }
            else if (id == R.id.nav_profile) {
                communicator.navigateToProfile();
                return true;
            }
            return false;
        });
    }

    public void updateSelectedItem(int itemId) {
        if (bottomNav != null && itemId != -1) {
            bottomNav.setSelectedItemId(itemId);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        communicator = null;
    }
}
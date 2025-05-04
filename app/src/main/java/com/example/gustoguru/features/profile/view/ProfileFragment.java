package com.example.gustoguru.features.profile.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gustoguru.R;
import com.example.gustoguru.features.authentication.login.view.LoginActivity;
import com.example.gustoguru.features.navigation.view.NavigationCommunicator;
import com.example.gustoguru.features.profile.presenter.ProfilePresenter;
import com.example.gustoguru.model.sessionmanager.SessionManager;
import com.example.gustoguru.model.local.AppDatabase;
import com.example.gustoguru.model.remote.firebase.FirebaseClient;
import com.example.gustoguru.model.remote.retrofit.client.MealClient;
import com.example.gustoguru.model.repository.MealRepository;
public class ProfileFragment extends Fragment implements ProfileView {
    private ProfilePresenter presenter;
    private TextView userNameText;
    private TextView userEmailText;
    private AlertDialog editNameDialog;
    private Button favoritesButton;
    private Button plannedMealsButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MealRepository repository = MealRepository.getInstance(
                AppDatabase.getInstance(requireContext()).favoriteMealDao(),
                AppDatabase.getInstance(requireContext()).plannedMealDao(),
                MealClient.getInstance(requireContext()),
                FirebaseClient.getInstance()
        );
        SessionManager sessionManager = new SessionManager(requireContext());

        presenter = new ProfilePresenter(this, repository, sessionManager);
        initViews(view);
        presenter.loadUserProfile();
        favoritesButton = view.findViewById(R.id.favorites_button);
        plannedMealsButton = view.findViewById(R.id.planned_meals_button);

        // Set click listeners
        favoritesButton.setOnClickListener(v -> {
            if (presenter.isLoggedIn()) {
                ((NavigationCommunicator)requireActivity()).navigateToFavorites();
            } else {
                ((NavigationCommunicator)requireActivity()).showLoginRequiredDialog("Please login to view favorites");
            }
        });

        plannedMealsButton.setOnClickListener(v -> {
            if (presenter.isLoggedIn()) {
                ((NavigationCommunicator)requireActivity()).navigateToPlannedMeals();
            } else {
                ((NavigationCommunicator)requireActivity()).showLoginRequiredDialog("Please login to view planned meals");

            }
        });
    }

    private void initViews(View view) {
        userNameText = view.findViewById(R.id.user_name);
        userEmailText = view.findViewById(R.id.user_email);

        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
        view.findViewById(R.id.edit_name_button).setOnClickListener(v -> {
            String currentName = userNameText.getText().toString();
            showEditNameDialog(currentName);
        });

        view.findViewById(R.id.logout_button).setOnClickListener(v -> presenter.logout());
    }

    private void showEditNameDialog(String currentName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_name, null);
        builder.setView(dialogView);

        EditText nameEditText = dialogView.findViewById(R.id.name_edit_text);
        Button saveButton = dialogView.findViewById(R.id.save_name_button);

        nameEditText.setText(currentName);
        editNameDialog = builder.create();
        editNameDialog.show();

        saveButton.setOnClickListener(v -> {
            String newName = nameEditText.getText().toString().trim();
            if (!newName.isEmpty()) {
                presenter.updateUserName(newName);
                editNameDialog.dismiss();
            } else {
                nameEditText.setError("Name cannot be empty");
            }
        });
    }

    @Override
    public void showUserProfile(String name, String email) {
        requireActivity().runOnUiThread(() -> {
            userNameText.setText(name);
            userEmailText.setText(email);
        });
    }

    @Override
    public void updateNameDisplay(String newName) {
        requireActivity().runOnUiThread(() -> {
            userNameText.setText(newName);
            Toast.makeText(requireContext(), "Name updated successfully", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void showLoading() {
        requireActivity().runOnUiThread(() -> {
            View view = getView();
            if (view != null) {
                view.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void hideLoading() {
        requireActivity().runOnUiThread(() -> {
            View view = getView();
            if (view != null) {
                view.findViewById(R.id.progress_bar).setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void showError(String message) {
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void navigateToLogin() {
        requireActivity().runOnUiThread(() -> {
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (editNameDialog != null && editNameDialog.isShowing()) {
            editNameDialog.dismiss();
        }
        presenter.detachView();
    }



}
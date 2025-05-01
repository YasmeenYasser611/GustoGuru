package com.example.gustoguru.features.profile.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gustoguru.R;
import com.example.gustoguru.features.authentication.login.view.LoginActivity;
import com.example.gustoguru.features.profile.presenter.ProfilePresenter;
import com.example.gustoguru.features.sessionmanager.SessionManager;
import com.example.gustoguru.model.local.AppDatabase;
import com.example.gustoguru.model.remote.firebase.FirebaseClient;
import com.example.gustoguru.model.remote.retrofit.client.MealClient;
import com.example.gustoguru.model.repository.MealRepository;

public class ProfileActivity extends AppCompatActivity implements ProfileView {
    private ProfilePresenter presenter;
    private TextView userNameText;
    private TextView userEmailText;
    private AlertDialog editNameDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_screen);

        // Initialize dependencies
        MealRepository repository = MealRepository.getInstance(
                AppDatabase.getInstance(this).favoriteMealDao(),
                AppDatabase.getInstance(this).plannedMealDao(),
                MealClient.getInstance(),
                FirebaseClient.getInstance()
        );
        SessionManager sessionManager = new SessionManager(this);

        presenter = new ProfilePresenter(this, repository, sessionManager);

        initViews();
        presenter.loadUserProfile();
    }

    private void initViews() {
        userNameText = findViewById(R.id.user_name);
        userEmailText = findViewById(R.id.user_email);

        // Back button
        findViewById(R.id.back_button).setOnClickListener(v -> onBackPressed());

        // Edit name button
        findViewById(R.id.edit_name_button).setOnClickListener(v -> {
            String currentName = userNameText.getText().toString();
            showEditNameDialog(currentName);
        });

        // Logout button
        findViewById(R.id.logout_button).setOnClickListener(v -> logoutUser());
    }

    private void showEditNameDialog(String currentName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_name, null);
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

    private void logoutUser() {
        presenter.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void showUserProfile(String name, String email) {
        userNameText.setText(name);
        userEmailText.setText(email);
    }

    @Override
    public void updateNameDisplay(String newName) {
        userNameText.setText(newName);
        Toast.makeText(this, "Name updated successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoading() {
        // Show progress bar or loading indicator
        findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        // Hide progress bar or loading indicator
        findViewById(R.id.progress_bar).setVisibility(View.GONE);
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (editNameDialog != null && editNameDialog.isShowing()) {
            editNameDialog.dismiss();
        }
    }
}
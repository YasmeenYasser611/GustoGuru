package com.example.gustoguru.features.authentication.registeration.view;



import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gustoguru.R;
import com.example.gustoguru.features.authentication.login.view.LoginActivity;
import com.example.gustoguru.features.authentication.registeration.presenter.RegistrationPresenter;
import com.example.gustoguru.model.local.AppDatabase;
import com.example.gustoguru.model.remote.firebase.FirebaseClient;
import com.example.gustoguru.model.remote.retrofit.client.MealClient;
import com.example.gustoguru.model.repository.MealRepository;
import com.facebook.FacebookSdk;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseUser;


public class RegistrationActivity extends AppCompatActivity implements RegistrationView
{
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button joinButton;
    private Button loginButton;
    private RegistrationPresenter presenter;
    private MealRepository mealRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        initViews();
        initDependencies();
        setupClickListeners();
    }

    private void initViews()
    {
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        joinButton = findViewById(R.id.join);
        loginButton = findViewById(R.id.login_button);
    }

    private void initDependencies()
    {
        mealRepository = MealRepository.getInstance(AppDatabase.getInstance(this).favoriteMealDao(), AppDatabase.getInstance(this).plannedMealDao(), MealClient.getInstance(), FirebaseClient.getInstance());
        presenter = new RegistrationPresenter(this, mealRepository);
    }

    private void setupClickListeners()
    {
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.registerUser(
                        emailEditText.getText().toString().trim(),
                        passwordEditText.getText().toString().trim()
                );
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLogin();
            }
        });
    }

    private void navigateToLogin()
    {
        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void showLoading() {
        joinButton.setEnabled(false);
    }

    @Override
    public void hideLoading() {
        joinButton.setEnabled(true);
    }

    @Override
    public void onRegistrationSuccess() {
        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
        navigateToLogin();
    }

    @Override
    public void onRegistrationFailure(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showEmailError(String message) {
        emailEditText.setError(message);
    }

    @Override
    public void showPasswordError(String message) {
        passwordEditText.setError(message);
    }
}


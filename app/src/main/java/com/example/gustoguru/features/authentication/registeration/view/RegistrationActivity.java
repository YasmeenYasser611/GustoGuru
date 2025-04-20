package com.example.gustoguru.features.authentication.registeration.view;



import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseUser;

public class RegistrationActivity extends AppCompatActivity implements RegistrationView, OnRegisterClickListener {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button joinButton;
    Button loginButton;
    private RegistrationPresenter presenter;

    private MealRepository mealRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize views
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        joinButton = findViewById(R.id.join);


        mealRepository = MealRepository.getInstance(
                AppDatabase.getInstance(this).favoriteMealDao(),
                AppDatabase.getInstance(this).plannedMealDao(),
                MealClient.getInstance(),
                FirebaseClient.getInstance()
        );


        // Initialize presenter
        presenter = new RegistrationPresenter(this, mealRepository);

        // Set up button click listeners
        joinButton.setOnClickListener(v ->
                onRegisterClick(emailEditText.getText().toString().trim(), passwordEditText.getText().toString().trim())
        );

        loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });


    }




    @Override
    public void onRegisterClick(String email, String password) {
        presenter.registerUser(email, password);
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

        // Navigate to LoginActivity
        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        //finish(); // Close RegistrationActivity
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



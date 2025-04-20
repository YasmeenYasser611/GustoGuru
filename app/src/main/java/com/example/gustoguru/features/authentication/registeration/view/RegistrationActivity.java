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
//import com.facebook.CallbackManager;
//import com.facebook.FacebookCallback;
//import com.facebook.FacebookException;
//import com.facebook.login.LoginManager;
//import com.facebook.login.LoginResult;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class RegistrationActivity extends AppCompatActivity implements RegistrationView, OnRegisterClickListener {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button joinButton;
    private Button fbRegisterButton; // Add Facebook login button

    private RegistrationPresenter presenter;
//    private CallbackManager callbackManager;
    private MealRepository mealRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize views
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        joinButton = findViewById(R.id.join);
        fbRegisterButton = findViewById(R.id.fb_register); // Make sure this ID exists in your layout

        // Initialize repository
        mealRepository = MealRepository.getInstance(
                AppDatabase.getInstance(this).favoriteMealDao(),
                AppDatabase.getInstance(this).plannedMealDao(),
                MealClient.getInstance(),
                FirebaseClient.getInstance()
        );

        // Initialize presenter
        presenter = new RegistrationPresenter(this, mealRepository);

        // Initialize Facebook callback manager
//        callbackManager = CallbackManager.Factory.create();

        // Set up button click listeners
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRegisterClick(emailEditText.getText().toString().trim(),
                        passwordEditText.getText().toString().trim());
            }
        });

        // Facebook login button click listener
//        fbRegisterButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                loginWithFacebook();
//            }
//        });
    }

//    private void loginWithFacebook() {
//        LoginManager.getInstance().registerCallback(
//                callbackManager,
//                new FacebookCallback<LoginResult>() {
//                    @Override
//                    public void onSuccess(LoginResult loginResult) {
//                        handleFacebookAccessToken(loginResult.getAccessToken().getToken());
//                    }
//
//                    @Override
//                    public void onCancel() {
//                        Toast.makeText(RegistrationActivity.this, "Facebook login cancelled", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onError(FacebookException error) {
//                        Toast.makeText(RegistrationActivity.this, "Facebook login error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile")
//        );
//    }

//    private void handleFacebookAccessToken(String token) {
//        presenter.registerWithFacebook(token, new FirebaseClient.OnAuthCallback() {
//            @Override
//            public void onSuccess(FirebaseUser user) {
//                onRegistrationSuccess();
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                onRegistrationFailure(e.getMessage());
//            }
//        });
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        callbackManager.onActivityResult(requestCode, resultCode, data);
//    }
//
//
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
        finish(); // Close RegistrationActivity
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

package com.example.gustoguru.features.authentication.login.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gustoguru.R;
import com.example.gustoguru.features.authentication.login.presenter.LoginPresenter;
import com.example.gustoguru.features.authentication.registeration.view.RegistrationActivity;
import com.example.gustoguru.model.local.AppDatabase;
import com.example.gustoguru.model.remote.firebase.FirebaseClient;
import com.example.gustoguru.model.remote.retrofit.client.MealClient;
import com.example.gustoguru.model.repository.MealRepository;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements LoginView, OnLoginClickListener {
    private static final int RC_GOOGLE_SIGN_IN = 9001;

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;

    private Button googleRegisterButton;

    private LoginPresenter presenter;
    private MealRepository mealRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        mealRepository = MealRepository.getInstance(
                AppDatabase.getInstance(this).favoriteMealDao(),
                AppDatabase.getInstance(this).plannedMealDao(),
                MealClient.getInstance(),
                FirebaseClient.getInstance()
        );


        presenter = new LoginPresenter(this, MealRepository.getInstance(AppDatabase.getInstance(this).favoriteMealDao(), AppDatabase.getInstance(this).plannedMealDao(), MealClient.getInstance(), FirebaseClient.getInstance()));

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLoginClick(
                        emailEditText.getText().toString().trim(),
                        passwordEditText.getText().toString().trim()
                );
            }
        });

        TextView registerTextView = findViewById(R.id.tv_register);
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });

        googleRegisterButton = findViewById(R.id.google_register);

        googleRegisterButton.setOnClickListener(v -> {
            Intent signInIntent = mealRepository.getGoogleSignInIntent(
                    this,
                    getString(R.string.default_web_client_id)
            );
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            if (data == null) {
                Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
                return;
            }

            mealRepository.handleGoogleSignInResult(data, new FirebaseClient.OnAuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, "Signed in with Google", Toast.LENGTH_SHORT).show();
                        onLoginSuccess();
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    runOnUiThread(() -> {
                        String message = e.getMessage();
                        if (message == null || message.isEmpty()) {
                            message = "Sign in failed";
                        }
                        Toast.makeText(LoginActivity.this, message,
                                Toast.LENGTH_LONG).show();
                    });
                }
            });
        }
    }

    @Override
    public void onLoginClick(String email, String password) {
        presenter.loginUser(email, password);
    }

    @Override
    public void showLoading() {
        loginButton.setEnabled(false);
    }

    @Override
    public void hideLoading() {
        loginButton.setEnabled(true);
    }

    @Override
    public void onLoginSuccess() {
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
        // Start  home activity here
    }

    @Override
    public void onLoginFailure(String errorMessage) {
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

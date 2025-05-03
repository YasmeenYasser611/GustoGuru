
package com.example.gustoguru.features.authentication.login.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gustoguru.R;
import com.example.gustoguru.features.authentication.login.presenter.LoginPresenter;
import com.example.gustoguru.features.authentication.registeration.view.RegistrationActivity;
import com.example.gustoguru.features.main.view.MainActivity;
import com.example.gustoguru.model.local.AppDatabase;
import com.example.gustoguru.model.remote.firebase.FirebaseClient;
import com.example.gustoguru.model.remote.retrofit.client.MealClient;
import com.example.gustoguru.model.repository.MealRepository;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;


public class LoginActivity extends AppCompatActivity implements LoginView
{
    private static final int RC_GOOGLE_SIGN_IN = 9001;

    private EditText emailEditText;
    private EditText passwordEditText;
    private LoginPresenter presenter;
    private MealRepository mealRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Facebook SDK FIRST
        FacebookSdk.sdkInitialize(getApplicationContext());
        FacebookSdk.setAutoInitEnabled(true);
        FacebookSdk.fullyInitialize();
        AppEventsLogger.activateApp(this.getApplication());

        initViews();
        initDependencies(); // Remove the duplicate call
        setupClickListeners();
        setupSkipButton();

        presenter.checkExistingSession();
    }

    private void initViews()
    {
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
    }
    private void setupSkipButton() {
        findViewById(R.id.tv_skip).setOnClickListener(v -> {
            // Mark user as guest
            navigateToHome();
        });
    }

    private void initDependencies() {
        mealRepository = MealRepository.getInstance(AppDatabase.getInstance(this).favoriteMealDao(), AppDatabase.getInstance(this).plannedMealDao(), MealClient.getInstance(this), FirebaseClient.getInstance());
        presenter = new LoginPresenter(this, mealRepository , this);
        presenter.initFacebookLogin();
    }

    private void setupClickListeners() {
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.loginUser(
                        emailEditText.getText().toString().trim(),
                        passwordEditText.getText().toString().trim()
                );
            }
        });

        findViewById(R.id.tv_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToRegistration();
            }
        });

        findViewById(R.id.google_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.handleGoogleSignInClick();
            }
        });

        findViewById(R.id.fb_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.performFacebookLogin();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN)
        {
            if (data == null) {
                onLoginFailure("Sign in cancelled");
                return;
            }
            presenter.handleGoogleSignIn(data);
            return;
        }

        presenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void startGoogleSignIn(Intent signInIntent)
    {
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    @Override
    public void navigateToRegistration() {
        startActivity(new Intent(this, RegistrationActivity.class));
    }


    public Context getActivityContext() {
        return this;
    }

    @Override
    public void showLoading() {
        findViewById(R.id.login).setEnabled(false);
    }

    @Override
    public void hideLoading() {
        findViewById(R.id.login).setEnabled(true);
    }

    @Override
    public void onLoginSuccess() {
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
        navigateToHome();
    }
    private void navigateToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
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

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getGoogleClientId() {
        return getString(R.string.default_web_client_id);
    }

    @Override
    public void onBackPressed() {
        // Navigate to HomeFragment in MainActivity when back button is pressed
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


}
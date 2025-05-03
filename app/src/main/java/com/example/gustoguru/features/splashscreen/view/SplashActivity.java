package com.example.gustoguru.features.splashscreen.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.gustoguru.R;
import com.example.gustoguru.features.authentication.login.view.LoginActivity;

import com.example.gustoguru.features.main.view.MainActivity;
import com.example.gustoguru.model.sessionmanager.SessionManager;
import com.example.gustoguru.features.splashscreen.presenter.SplashPresenter;
import com.google.firebase.FirebaseApp;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity implements Splashview {
    private SplashPresenter presenter;
    private LottieAnimationView animationView;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        FirebaseApp.initializeApp(this);

        animationView = findViewById(R.id.splashAnimation);
        sessionManager = new SessionManager(this);

        // Initialize presenter
        presenter = new SplashPresenter(this, sessionManager);
        presenter.initialize();
    }

    @Override
    public LottieAnimationView getAnimationView() {
        return animationView;
    }

    @Override
    public void navigateToLogin() {
        try {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } catch (Exception e) {
            showError("Failed to start LoginActivity");
        }
    }

    @Override
    public void navigateToMain() {
        try {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } catch (Exception e) {
            showError("Failed to start MainActivity");
        }
    }

    @Override
    public void showError(String message) {
        Log.e("SplashActivity", message);
        // Fallback navigation - try to go to MainActivity
        try {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } catch (Exception e) {
            // If all else fails, just finish
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}
package com.example.gustoguru.features.splashscreen.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.gustoguru.R;
import com.example.gustoguru.features.authentication.login.view.LoginActivity;

import com.example.gustoguru.features.splashscreen.presenter.SplashPresenter;
import com.google.firebase.FirebaseApp;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity implements Splashview{
    private SplashPresenter presenter;
    private LottieAnimationView animationView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        FirebaseApp.initializeApp(this);

        animationView = findViewById(R.id.splashAnimation);

        // Initialize presenter
        presenter = new SplashPresenter(this);
        presenter.initialize();
    }

    @Override
    public LottieAnimationView getAnimationView()
    {
        return animationView;
    }

    @Override
    public void navigateToLogin()
    {
        try {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } catch (Exception e) {
            showError("Failed to start LoginActivity");
        }
    }

    @Override
    public void showError(String message)
    {
        Log.e("SplashActivity", message);
        // Fallback navigation
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        presenter.onDestroy();
    }
}
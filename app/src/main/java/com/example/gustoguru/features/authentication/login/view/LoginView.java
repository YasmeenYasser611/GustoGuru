package com.example.gustoguru.features.authentication.login.view;




import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.StringRes;

public interface LoginView {
    void showLoading();
    void hideLoading();
    void onLoginSuccess();
    void onLoginFailure(String errorMessage);
    void showEmailError(String message);
    void showPasswordError(String message);
    Activity getActivity();
    String getGoogleClientId();
    void startGoogleSignIn(Intent signInIntent);
    void navigateToRegistration();
}
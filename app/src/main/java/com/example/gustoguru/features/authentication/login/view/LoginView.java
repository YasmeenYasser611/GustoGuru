package com.example.gustoguru.features.authentication.login.view;




public interface LoginView {
    void showLoading();
    void hideLoading();
    void onLoginSuccess();
    void onLoginFailure(String errorMessage);
    void showEmailError(String message);
    void showPasswordError(String message);
}

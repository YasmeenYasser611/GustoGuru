package com.example.gustoguru.features.authentication.registeration.view;






public interface RegistrationView {
    void showLoading();
    void hideLoading();
    void onRegistrationSuccess();
    void onRegistrationFailure(String errorMessage);
    void showEmailError(String message);
    void showPasswordError(String message);
}

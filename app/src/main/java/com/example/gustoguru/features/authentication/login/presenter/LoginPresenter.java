package com.example.gustoguru.features.authentication.login.presenter;


import com.example.gustoguru.features.authentication.login.view.LoginView;
import com.example.gustoguru.model.remote.firebase.FirebaseClient;
import com.example.gustoguru.model.repository.MealRepository;
import com.google.firebase.auth.FirebaseUser;

public class LoginPresenter {
    private final LoginView view;
    private final MealRepository repository;

    public LoginPresenter(LoginView view, MealRepository repository) {
        this.view = view;
        this.repository = repository;
    }

    public void loginUser(String email, String password) {
        if (!validateCredentials(email, password)) {
            return;
        }

        view.showLoading();


        repository.login(email, password, new FirebaseClient.OnAuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                view.hideLoading();
                view.onLoginSuccess();
            }

            @Override
            public void onFailure(Exception e) {
                view.hideLoading();
                view.onLoginFailure(parseFirebaseError(e));
            }
        });
    }

    private boolean validateCredentials(String email, String password) {
        boolean isValid = true;

        if (email.isEmpty()) {
            view.showEmailError("Email cannot be empty");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.showEmailError("Invalid email format");
            isValid = false;
        }

        if (password.isEmpty()) {
            view.showPasswordError("Password cannot be empty");
            isValid = false;
        }

        return isValid;
    }

    private String parseFirebaseError(Exception exception) {
        String errorMessage = exception.getMessage();
        if (errorMessage == null) return "Login failed";

        if (errorMessage.contains("invalid login credentials")) {
            return "Invalid email or password";
        } else if (errorMessage.contains("network error")) {
            return "Network error - please check your connection";
        } else if (errorMessage.contains("too many requests")) {
            return "Too many attempts - try again later";
        }
        return "Login failed - please try again";
    }
}
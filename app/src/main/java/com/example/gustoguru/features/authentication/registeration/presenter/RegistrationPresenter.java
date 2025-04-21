package com.example.gustoguru.features.authentication.registeration.presenter;

import android.content.Intent;

import com.example.gustoguru.features.authentication.registeration.view.RegistrationView;
import com.example.gustoguru.model.remote.firebase.FirebaseClient;
import com.example.gustoguru.model.repository.MealRepository;
import com.google.firebase.auth.FirebaseUser;


public class RegistrationPresenter
{
    private final RegistrationView view;
    private final MealRepository repository;

    public RegistrationPresenter(RegistrationView view, MealRepository repository)
    {
        this.view = view;
        this.repository = repository;
    }


    public void registerUser(String email, String password)
    {
        if (!validateCredentials(email, password)) {
            return;
        }

        view.showLoading();

        repository.register(email, password, new FirebaseClient.OnAuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                view.hideLoading();
                view.onRegistrationSuccess();
            }

            @Override
            public void onFailure(Exception e) {
                view.hideLoading();
                view.onRegistrationFailure(parseFirebaseError(e));
            }
        });
    }



    private boolean validateCredentials(String email, String password)
    {
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
        } else if (password.length() < 6) {
            view.showPasswordError("Password must be at least 6 characters");
            isValid = false;
        }

        return isValid;
    }

    private String parseFirebaseError(Exception exception)
    {
        String errorMessage = exception.getMessage();
        if (errorMessage == null) return "Registration failed";

        if (errorMessage.contains("email address is already in use")) {
            return "Email already registered";
        } else if (errorMessage.contains("network error")) {
            return "Network error - please check your connection";
        } else if (errorMessage.contains("invalid email")) {
            return "Invalid email format";
        } else if (errorMessage.contains("password is invalid")) {
            return "Password must be at least 6 characters";
        }
        return "Registration failed - please try again";
    }
}
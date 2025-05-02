package com.example.gustoguru.features.authentication.login.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.gustoguru.R;
import com.example.gustoguru.features.authentication.login.view.LoginView;
import com.example.gustoguru.features.sessionmanager.SessionManager;
import com.example.gustoguru.model.remote.firebase.FirebaseClient;
import com.example.gustoguru.model.repository.MealRepository;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;


public class LoginPresenter
{
    private SessionManager sessionManager;
    private final LoginView view;
    private final MealRepository repository;
    private CallbackManager facebookCallbackManager;

    public LoginPresenter(LoginView view, MealRepository repository , Context context)
    {
        this.view = view;
        this.repository = repository;
        this.sessionManager = new SessionManager(context);

    }


    public void loginUser(String email, String password)
    {
        if (!validateCredentials(email, password))
        {
            return;
        }

        view.showLoading();
        repository.login(email, password, new FirebaseClient.OnAuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                sessionManager.createLoginSession(
                        user.getUid(),
                        user.getEmail(),
                        user.getDisplayName() != null ? user.getDisplayName() : ""
                );
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


    public void handleGoogleSignIn(Intent data)
    {
        view.showLoading();
        repository.handleGoogleSignInResult(data, new FirebaseClient.OnAuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Save user session
                sessionManager.createLoginSession(
                        user.getUid(),
                        user.getEmail(),
                        user.getDisplayName() != null ? user.getDisplayName() : ""
                );
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


    public void handleGoogleSignInClick()
    {
        try {
            Intent signInIntent = repository.getGoogleSignInIntent(view.getActivity(), view.getGoogleClientId());
            if (signInIntent != null)
            {
                view.startGoogleSignIn(signInIntent);
            }
            else
            {
                view.onLoginFailure("Failed to initialize Google Sign-In");
            }
        }
        catch (Exception e)
        {
            view.onLoginFailure("Error: " + e.getMessage());

        }
    }


    public void initFacebookLogin()
    {
        facebookCallbackManager = repository.getFacebookCallbackManager();
        repository.registerFacebookCallback(new FirebaseClient.OnAuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Save user session
                sessionManager.createLoginSession(
                        user.getUid(),
                        user.getEmail(),
                        user.getDisplayName() != null ? user.getDisplayName() : ""
                );
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


    public void performFacebookLogin()
    {
        view.showLoading();
        if (AccessToken.getCurrentAccessToken() != null)
        {
            LoginManager.getInstance().logOut();
        }
        LoginManager.getInstance().setLoginBehavior(LoginBehavior.WEB_ONLY);

        Activity activity = view.getActivity();
        if (activity != null && !activity.isFinishing())
        {
            LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("email", "public_profile"));
        }
        else
        {
            view.onLoginFailure("Activity not available");
        }
    }


    public boolean onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (facebookCallbackManager != null) {
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
            return true;
        }
        return false;
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
        }

        return isValid;
    }

    private String parseFirebaseError(Exception exception) {
        String errorMessage = exception.getMessage();
        if (errorMessage == null) return "Login failed";

        if (exception instanceof FirebaseAuthUserCollisionException) {
            return "This email is already registered with another sign-in method. " +
                    "Please sign in using that method first to link accounts.";
        } else if (errorMessage.contains("invalid login credentials")) {
            return "Invalid email or password";
        } else if (errorMessage.contains("network error")) {
            return "Network error - please check your connection";
        } else if (errorMessage.contains("too many requests")) {
            return "Too many attempts - try again later";
        }
        return "Login failed - please try again";
    }
    public void checkExistingSession() {
        if (sessionManager.isLoggedIn()) {
            view.onLoginSuccess();
        }
    }

}
 package com.example.gustoguru.model.remote.firebase;
//
//import com.facebook.AccessToken;
//import com.facebook.CallbackManager;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseClient {
    private static FirebaseClient instance;
//    private CallbackManager facebookCallbackManager;
    private final FirebaseAuth firebaseAuth;

    public interface OnAuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception e);
    }

    private FirebaseClient() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseClient getInstance() {
        if (instance == null) {
            instance = new FirebaseClient();
        }
        return instance;
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public void login(String email, String password, OnAuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(firebaseAuth.getCurrentUser());
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void register(String email, String password, OnAuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(firebaseAuth.getCurrentUser());
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void logout() {
        firebaseAuth.signOut();
    }

//    public void initFacebookLogin() {
//        facebookCallbackManager = CallbackManager.Factory.create();
//    }
//
//    public CallbackManager getFacebookCallbackManager() {
//        return facebookCallbackManager;
//    }
//
//    public void handleFacebookAccessToken(String token, OnAuthCallback callback) {
//        AuthCredential credential = FacebookAuthProvider.getCredential(token);
//        firebaseAuth.signInWithCredential(credential)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        callback.onSuccess(firebaseAuth.getCurrentUser());
//                    } else {
//                        callback.onFailure(task.getException());
//                    }
//                });
//    }
}
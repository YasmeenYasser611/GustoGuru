 package com.example.gustoguru.model.remote.firebase;
//
//import com.facebook.AccessToken;
//import com.facebook.CallbackManager;


 import android.content.Context;
 import android.content.Intent;

 import com.google.android.gms.auth.api.signin.GoogleSignIn;
 import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
 import com.google.android.gms.auth.api.signin.GoogleSignInClient;
 import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
 import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
 import com.google.android.gms.common.api.ApiException;
 import com.google.android.gms.tasks.Task;
 import com.google.firebase.auth.AuthCredential;
 import com.google.firebase.auth.FirebaseAuth;
 import com.google.firebase.auth.FirebaseUser;
 import com.google.firebase.auth.GoogleAuthProvider;

 import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;

 public class FirebaseClient {
     private static FirebaseClient instance;
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

     public GoogleSignInClient getGoogleSignInClient(Context context, String webClientId) {
         GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                 .requestIdToken(webClientId)
                 .requestEmail()
                 .build();
         return GoogleSignIn.getClient(context, gso);
     }

     public Intent getGoogleSignInIntent(Context context, String webClientId) {
         GoogleSignInClient client = getGoogleSignInClient(context, webClientId);
         // Clear any existing account
         client.signOut();
         return client.getSignInIntent();
     }


     public void handleGoogleSignInResult(Intent data, OnAuthCallback callback) {
         try {
             GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data)
                     .getResult(ApiException.class);
             if (account == null || account.getIdToken() == null) {
                 callback.onFailure(new Exception("Google account information missing"));
                 return;
             }
             firebaseAuthWithGoogle(account.getIdToken(), callback);
         } catch (ApiException e) {
             String errorMessage = parseGoogleError(e.getStatusCode());
             callback.onFailure(new Exception(errorMessage));
         }
     }

     private String parseGoogleError(int statusCode) {
         switch (statusCode) {
             case GoogleSignInStatusCodes.SIGN_IN_CANCELLED:
                 return "Sign in cancelled";
             case GoogleSignInStatusCodes.SIGN_IN_FAILED:
                 return "Sign in failed - please try again";
             case GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS:
                 return "Sign in already in progress";
             case 10: // Common error code
                 return "Developer error - check configuration";
             default:
                 return "Sign in error (" + statusCode + ") - please try again";
         }
     }
     private void firebaseAuthWithGoogle(String idToken, OnAuthCallback callback) {
         AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
         firebaseAuth.signInWithCredential(credential)
                 .addOnCompleteListener(task -> {
                     if (task.isSuccessful()) {
                         callback.onSuccess(firebaseAuth.getCurrentUser());
                     } else {
                         callback.onFailure(task.getException());
                     }
                 });
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



}
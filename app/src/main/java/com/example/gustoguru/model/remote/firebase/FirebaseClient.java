 package com.example.gustoguru.model.remote.firebase;
 import com.facebook.AccessToken;
 import com.facebook.CallbackManager;
 import com.facebook.FacebookCallback;
 import com.facebook.FacebookException;
 import com.facebook.login.LoginManager;
 import com.facebook.login.LoginResult;
 import com.google.firebase.auth.FacebookAuthProvider;


 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;

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

 public class FirebaseClient
 {
     private static FirebaseClient instance;
     private final FirebaseAuth firebaseAuth;

     private CallbackManager facebookCallbackManager;

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
         client.signOut(); // Clear any existing session
         return client.getSignInIntent();
     }

     public void handleGoogleSignInResult(Intent data, OnAuthCallback callback) {
         try {
             Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
             GoogleSignInAccount account = task.getResult(ApiException.class);
             firebaseAuthWithGoogle(account.getIdToken(), callback);
         } catch (ApiException e) {
             callback.onFailure(new Exception("Google sign-in failed: " + e.getStatusCode()));
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
     public Intent getGoogleSignInIntent(Activity activity, String clientId) {
         GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                 .requestIdToken(clientId)
                 .requestEmail()
                 .build();

         GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(activity, gso);
         return googleSignInClient.getSignInIntent();
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


     public CallbackManager getFacebookCallbackManager() {
         if (facebookCallbackManager == null) {
             facebookCallbackManager = CallbackManager.Factory.create();
         }
         return facebookCallbackManager;
     }

     public void handleFacebookAccessToken(AccessToken token, OnAuthCallback callback) {
         Log.d("FB_DEBUG", "Handling Facebook token: " + token.getToken());

         AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
         firebaseAuth.signInWithCredential(credential)
                 .addOnCompleteListener(task -> {
                     if (task.isSuccessful()) {
                         Log.d("FB_DEBUG", "Facebook auth successful");
                         callback.onSuccess(firebaseAuth.getCurrentUser());
                     } else {
                         Log.e("FB_DEBUG", "Facebook auth failed", task.getException());
                         callback.onFailure(task.getException());
                     }
                 });
     }
     // In FirebaseClient.java
     public void registerFacebookCallback(OnAuthCallback callback) {
         LoginManager.getInstance().registerCallback(
                 getFacebookCallbackManager(),
                 new FacebookCallback<LoginResult>() {
                     @Override
                     public void onSuccess(LoginResult loginResult) {
                         handleFacebookAccessToken(loginResult.getAccessToken(), callback);
                     }

                     @Override
                     public void onCancel() {
                         callback.onFailure(new Exception("Facebook login cancelled"));
                     }

                     @Override
                     public void onError(FacebookException error) {
                         callback.onFailure(error);
                     }
                 });
     }


}
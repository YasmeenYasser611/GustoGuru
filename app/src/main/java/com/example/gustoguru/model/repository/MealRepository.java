package com.example.gustoguru.model.repository;

import com.example.gustoguru.model.remote.firebase.FirebaseSyncHelper;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gustoguru.model.local.FavoriteMealDao;
import com.example.gustoguru.model.local.PlannedMealDao;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.remote.firebase.FirebaseClient;
import com.example.gustoguru.model.remote.retrofit.client.MealClient;
import com.example.gustoguru.model.remote.retrofit.callback.AreaCallback;
import com.example.gustoguru.model.remote.retrofit.callback.CategoryCallback;
import com.example.gustoguru.model.remote.retrofit.callback.FilteredMealCallback;
import com.example.gustoguru.model.remote.retrofit.callback.IngredientCallback;
import com.example.gustoguru.model.remote.retrofit.callback.MealCallback;
//import com.facebook.CallbackManager;
//import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MealRepository {

    private final FavoriteMealDao favoriteMealDao;
    private final PlannedMealDao plannedMealDao;
    private final MealClient mealClient;

    private final FirebaseClient firebaseClient;

    private static MealRepository instance;
    private FirebaseSyncHelper syncHelper;



    public static synchronized MealRepository getInstance(FavoriteMealDao favoriteMealDao, PlannedMealDao plannedMealDao, MealClient mealClient, FirebaseClient firebaseClient)
    {
        if (instance == null)
        {
            instance = new MealRepository(favoriteMealDao, plannedMealDao, mealClient, firebaseClient);
        }
        return instance;
    }

    private MealRepository(FavoriteMealDao favoriteMealDao, PlannedMealDao plannedMealDao, MealClient mealClient, FirebaseClient firebaseClient)
    {
        this.favoriteMealDao = favoriteMealDao;
        this.plannedMealDao = plannedMealDao;
        this.mealClient = mealClient;
        this.firebaseClient = firebaseClient;
        FirebaseUser user = firebaseClient.getCurrentUser();
        if (user != null) {
            syncHelper = new FirebaseSyncHelper(user.getUid());
        }
    }


    // Remote operations
    public void getRandomMeal(MealCallback callback) {
        mealClient.getRandomMeal(callback);
    }

    public void searchMealsByName(String query, MealCallback callback) {
        mealClient.searchMealsByName(query, callback);
    }

    public void getMealById(String mealId, MealCallback callback) {
        mealClient.getMealById(mealId, callback);
    }

    public void filterByCategory(String category, FilteredMealCallback callback) {
        mealClient.filterByCategory(category, callback);
    }

    public void getAllCategories(CategoryCallback callback) {
        mealClient.getAllCategories(callback);
    }

    public void getAllAreas(AreaCallback callback) {
        mealClient.getAllAreas(callback);
    }

    public void getAllIngredients(IngredientCallback callback) {
        mealClient.getAllIngredients(callback);
    }
    // In MealRepository.java
    public void filterByIngredient(String ingredient, FilteredMealCallback callback) {
        mealClient.filterByIngredient(ingredient, callback);
    }

    public void filterByArea(String area, FilteredMealCallback callback) {
        mealClient.filterByArea(area, callback);
    }

    // Local operations
    public LiveData<List<Meal>> getAllFavorites() {
        return favoriteMealDao.getAllFavorites();
    }
    public LiveData<List<Meal>> getUserFavorites() {
        FirebaseUser user = firebaseClient.getCurrentUser();
        if (user != null) {
            return favoriteMealDao.getUserFavorites(user.getUid());
        }
        return new MutableLiveData<>(Collections.emptyList());
    }

    public void addFavorite(Meal meal) {
        if (meal == null || meal.getIdMeal() == null) return;

        FirebaseUser user = firebaseClient.getCurrentUser();
        if (user == null) return;

        new Thread(() -> {
            meal.setFavorite(true);
            meal.setUserId(user.getUid());
            meal.setLastSyncTimestamp(System.currentTimeMillis());

            if (favoriteMealDao.mealExists(meal.getIdMeal(), user.getUid()) > 0) {
                favoriteMealDao.updateFavoriteStatus(meal.getIdMeal(), true, user.getUid());
            } else {
                favoriteMealDao.insertFavorite(meal);
            }

            if (syncHelper != null) {
                syncHelper.syncFavoriteToFirebase(meal);
            }
        }).start();
    }

    public void removeFavorite(Meal meal) {
        if (meal == null || meal.getIdMeal() == null) return;

        FirebaseUser user = firebaseClient.getCurrentUser();
        if (user == null) return;

        new Thread(() -> {
            meal.setFavorite(false);
            if (favoriteMealDao.mealExists(meal.getIdMeal(), user.getUid()) > 0) {
                favoriteMealDao.updateFavoriteStatus(meal.getIdMeal(), false, user.getUid());
            }

            if (syncHelper != null) {
                syncHelper.removeFavoriteFromFirebase(meal.getIdMeal());
            }
        }).start();
    }

    public void removePlannedMeal(Meal meal) {
        new Thread(() -> {
            plannedMealDao.deletePlannedMeal(meal.getIdMeal() ,"");
        }).start();
    }


    public LiveData<List<Meal>> getUserPlannedMeals() {
        FirebaseUser user = firebaseClient.getCurrentUser();
        if (user != null) {
            return plannedMealDao.getUserPlannedMeals(user.getUid());
        }
        return new MutableLiveData<>(Collections.emptyList());
    }

    public void addPlannedMeal(Meal meal, String date) {
        FirebaseUser user = firebaseClient.getCurrentUser();
        if (user == null) return;

        new Thread(() -> {
            meal.setPlannedDate(date);
            meal.setUserId(user.getUid());
            meal.setLastSyncTimestamp(System.currentTimeMillis());

            plannedMealDao.insertPlannedMeal(meal);

            if (syncHelper != null) {
                syncHelper.syncPlannedMealToFirebase(meal);
            }
        }).start();
    }

//    public void removePlannedMeal(Meal meal) {
//        FirebaseUser user = firebaseClient.getCurrentUser();
//        if (user == null) return;
//
//        new Thread(() -> {
//            plannedMealDao.deletePlannedMeal(meal.getIdMeal(), meal.getPlannedDate(), user.getUid());
//
//            if (syncHelper != null) {
//                syncHelper.removePlannedMealFromFirebase(meal.getIdMeal(), meal.getPlannedDate());
//            }
//        }).start();
//    }

    //FireBase Functions
    public void login(String email, String password, FirebaseClient.OnAuthCallback callback)
    {
        firebaseClient.login(email, password, new FirebaseClient.OnAuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Initialize sync after successful login
                initializeSync(user.getUid());
            }

            @Override
            public void onFailure(Exception e) {
                // Handle error
            }
        });
    }

    public void register(String email, String password, FirebaseClient.OnAuthCallback callback)
    {
        firebaseClient.register(email, password, callback);
    }

    public void logout()
    {
        firebaseClient.logout();
    }

    public FirebaseUser getCurrentUser()
    {
        return firebaseClient.getCurrentUser();
    }



    // for Google Sign-In


    public Intent getGoogleSignInIntent(Context context, String webClientId) {
        return firebaseClient.getGoogleSignInIntent(context, webClientId);
    }

    public void handleGoogleSignInResult(Intent data, FirebaseClient.OnAuthCallback callback) {
        firebaseClient.handleGoogleSignInResult(data, callback);
    }



    //facebook

    public CallbackManager getFacebookCallbackManager() {
        return firebaseClient.getFacebookCallbackManager();
    }


    public void registerFacebookCallback(FirebaseClient.OnAuthCallback callback) {
        firebaseClient.registerFacebookCallback(callback);
    }

    // Add to MealRepository.java
    public void getUserProfile(OnProfileDataCallback callback) {
        firebaseClient.getUserProfile(new FirebaseClient.OnAuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                callback.onSuccess(
                        user.getDisplayName() != null ? user.getDisplayName() : "",
                        user.getEmail() != null ? user.getEmail() : ""
                );
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void updateUserName(String newName, OnUpdateCallback callback) {
        firebaseClient.updateUserName(newName, new FirebaseClient.OnUpdateCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void registerWithName(String email, String password, String name, FirebaseClient.OnAuthCallback callback) {
        firebaseClient.register(email, password, name, callback);
    }

    // Add to MealRepository.java
    public interface OnProfileDataCallback {
        void onSuccess(String name, String email);
        void onFailure(Exception e);
    }

    public interface OnUpdateCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
    public void initializeSync(String userId) {
        this.syncHelper = new FirebaseSyncHelper(userId);

        // Sync favorites
        syncHelper.downloadUserFavorites(new FirebaseSyncHelper.FirebaseCallback<List<Meal>>() {
            @Override
            public void onSuccess(List<Meal> meals) {
                new Thread(() -> {
                    for (Meal meal : meals) {
                        if (favoriteMealDao.mealExists(meal.getIdMeal(), userId) == 0) {
                            meal.setUserId(userId);
                            favoriteMealDao.insertFavorite(meal);
                        }
                    }
                }).start();
            }
            @Override
            public void onFailure(Exception e) {
                Log.e("SYNC", "Failed to download favorites", e);
            }
        });

        // Sync planned meals
        syncHelper.downloadUserPlannedMeals(new FirebaseSyncHelper.FirebaseCallback<List<Meal>>() {
            @Override
            public void onSuccess(List<Meal> meals) {
                new Thread(() -> {
                    for (Meal meal : meals) {
                        if (plannedMealDao.mealExists(meal.getIdMeal(), meal.getPlannedDate(), userId) == 0) {
                            meal.setUserId(userId);
                            plannedMealDao.insertPlannedMeal(meal);
                        }
                    }
                }).start();
            }
            @Override
            public void onFailure(Exception e) {
                Log.e("SYNC", "Failed to download planned meals", e);
            }
        });
    }



}
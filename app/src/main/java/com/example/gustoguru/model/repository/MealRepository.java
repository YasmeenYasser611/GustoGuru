package com.example.gustoguru.model.repository;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;

import androidx.lifecycle.LiveData;

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
import java.util.List;

public class MealRepository {

    private final FavoriteMealDao favoriteMealDao;
    private final PlannedMealDao plannedMealDao;
    private final MealClient mealClient;

    private final FirebaseClient firebaseClient;

    private static MealRepository instance;



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

    // Local operations
    public LiveData<List<Meal>> getAllFavorites() {
        return favoriteMealDao.getAllFavorites();
    }

    public void addFavorite(Meal meal) {
        new Thread(() -> {
            meal.setFavorite(true);
            favoriteMealDao.insertFavorite(meal);
        }).start();
    }

    public void removeFavorite(Meal meal) {
        if (meal == null || meal.getIdMeal() == null) return;

        new Thread(() -> {
            // First check if the meal exists in the database
            if (favoriteMealDao.isFavorite(meal.getIdMeal())) {
                favoriteMealDao.deleteFavorite(meal);
            }
        }).start();
    }

    public LiveData<List<Meal>> getAllPlannedMeals() {
        return plannedMealDao.getAllPlannedMeals();
    }

    public void addPlannedMeal(Meal meal, String date)
    {
        new Thread(() -> {
            meal.setPlannedDate(date);
            plannedMealDao.insertPlannedMeal(meal);
        }).start();
    }

    //FireBase Functions
    public void login(String email, String password, FirebaseClient.OnAuthCallback callback)
    {
        firebaseClient.login(email, password, callback);
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



}
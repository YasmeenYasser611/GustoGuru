package com.example.gustoguru.model.repository;

import androidx.lifecycle.LiveData;

import com.example.gustoguru.model.local.FavoriteMealDao;
import com.example.gustoguru.model.local.PlannedMealDao;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.remote.client.MealClient;
import com.example.gustoguru.model.remote.callback.AreaCallback;
import com.example.gustoguru.model.remote.callback.CategoryCallback;
import com.example.gustoguru.model.remote.callback.FilteredMealCallback;
import com.example.gustoguru.model.remote.callback.IngredientCallback;
import com.example.gustoguru.model.remote.callback.MealCallback;

import java.util.List;

public class MealRepository {
    private final FavoriteMealDao favoriteMealDao;
    private final PlannedMealDao plannedMealDao;
    private final MealClient mealClient;

    private static MealRepository instance;

    public static synchronized MealRepository getInstance(
            FavoriteMealDao favoriteMealDao,
            PlannedMealDao plannedMealDao,
            MealClient mealClient) {
        if (instance == null) {
            instance = new MealRepository(favoriteMealDao, plannedMealDao, mealClient);
        }
        return instance;
    }

    private MealRepository(FavoriteMealDao favoriteMealDao,
                           PlannedMealDao plannedMealDao,
                           MealClient mealClient) {
        this.favoriteMealDao = favoriteMealDao;
        this.plannedMealDao = plannedMealDao;
        this.mealClient = mealClient;
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

    public void addPlannedMeal(Meal meal, String date) {
        new Thread(() -> {
            meal.setPlannedDate(date);
            plannedMealDao.insertPlannedMeal(meal);
        }).start();
    }
}
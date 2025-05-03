package com.example.gustoguru.features.home.presenter;


import android.content.Context;

import com.example.gustoguru.features.home.view.HomeView;
import com.example.gustoguru.model.sessionmanager.SessionManager;
import com.example.gustoguru.model.pojo.Area;
import com.example.gustoguru.model.pojo.Category;
import com.example.gustoguru.model.pojo.FilteredMeal;
import com.example.gustoguru.model.pojo.Ingredient;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.remote.retrofit.callback.AreaCallback;
import com.example.gustoguru.model.remote.retrofit.callback.CategoryCallback;
import com.example.gustoguru.model.remote.retrofit.callback.FilteredMealCallback;
import com.example.gustoguru.model.remote.retrofit.callback.IngredientCallback;
import com.example.gustoguru.model.remote.retrofit.callback.MealCallback;
import com.example.gustoguru.model.repository.MealRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class HomePresenter {

    private HomeView view;
    private MealRepository repository;
    private SessionManager sessionManager;

    public HomePresenter(HomeView view, MealRepository repository, Context context) {
        this.view = view;
        this.repository = repository;
        this.sessionManager = new SessionManager(context);
    }


    public void getRandomMeal() {
        Meal savedMeal = sessionManager.getMealOfTheDay();
        if (savedMeal != null) {
            view.showMealOfTheDay(savedMeal);
            return;
        }
        fetchNewRandomMeal();
    }

    private void fetchNewRandomMeal() {
        repository.getRandomMeal(new MealCallback() {
            @Override
            public void onSuccess(List<Meal> meals) {
                if (meals != null && !meals.isEmpty()) {
                    Meal todaysMeal = meals.get(0);
                    view.showMealOfTheDay(todaysMeal);
                    sessionManager.saveMealOfTheDay(todaysMeal);
                } else {
                    view.showError("No meal found");
                }
            }

            @Override
            public void onFailure(String message) {
                view.showError(message);
            }
        });
    }


    public void getAllCategories() {
        repository.getAllCategories(new CategoryCallback() {
            @Override
            public void onSuccess(List<Category> categories) {
                if (categories != null && !categories.isEmpty()) {
                    view.showCategories(categories);
                } else {
                    view.showError("No categories found");
                }
            }

            @Override
            public void onFailure(String message) {
                view.showError(message);
            }
        });
    }


    public void searchByCategory(String category) {
        repository.filterByCategory(category, new FilteredMealCallback() {
            @Override
            public void onSuccess(List<FilteredMeal> meals) {
                view.showMealsByCategory(meals);
            }

            @Override
            public void onFailure(String message) {
                view.showError(message);
            }
        });
    }


    public void onDestroy() {
        view = null;
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    public void getAllAreas() {
        repository.getAllAreas(new AreaCallback() {
            @Override
            public void onSuccess(List<Area> areas) {
                view.showAreas(areas);
            }

            @Override
            public void onFailure(String message) {
                view.showError(message);
            }
        });
    }

    public void searchByArea(String area) {
        repository.filterByArea(area, new FilteredMealCallback() {
            @Override
            public void onSuccess(List<FilteredMeal> meals) {
                view.showMealsByArea(meals);
            }

            @Override
            public void onFailure(String message) {
                view.showError(message);
            }
        });
    }

    // Ingredients Section
    public void getPopularIngredients() {
        repository.getAllIngredients(new IngredientCallback() {
            @Override
            public void onSuccess(List<Ingredient> ingredients) {
                // Get top 12 most common ingredients
                List<Ingredient> popular = ingredients.subList(0, Math.min(500, ingredients.size()));
                view.showIngredients(popular);
            }

            @Override
            public void onFailure(String message) {
                view.showError("Failed to load ingredients: " + message);
            }
        });
    }

    public void searchByIngredient(String ingredient) {
        repository.filterByIngredient(ingredient, new FilteredMealCallback() {
            @Override
            public void onSuccess(List<FilteredMeal> meals) {
                view.showMealsByIngredient(meals);
            }

            @Override
            public void onFailure(String message) {
                view.showError("No meals found with this ingredient: " + message);
            }
        });
    }


}
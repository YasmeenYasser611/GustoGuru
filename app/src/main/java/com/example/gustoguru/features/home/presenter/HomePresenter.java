package com.example.gustoguru.features.home.presenter;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.gustoguru.features.home.view.HomeView;
import com.example.gustoguru.model.pojo.Category;
import com.example.gustoguru.model.pojo.FilteredMeal;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.remote.retrofit.callback.CategoryCallback;
import com.example.gustoguru.model.remote.retrofit.callback.FilteredMealCallback;
import com.example.gustoguru.model.remote.retrofit.callback.MealCallback;
import com.example.gustoguru.model.repository.MealRepository;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class HomePresenter {
    private static final String PREF_MEAL_OF_THE_DAY = "meal_of_the_day";
    private static final String PREF_MEAL_DATE = "meal_date";

    private HomeView view;
    private MealRepository repository;
    private SharedPreferences sharedPreferences;

    public HomePresenter(HomeView view, MealRepository repository, Context context) {
        this.view = view;
        this.repository = repository;
        this.sharedPreferences = context.getSharedPreferences("MealPreferences", Context.MODE_PRIVATE);
    }


    public void getRandomMeal() {
        String savedDate = sharedPreferences.getString(PREF_MEAL_DATE, "");
        String today = getCurrentDate();

        if (savedDate.equals(today)) {
            String mealJson = sharedPreferences.getString(PREF_MEAL_OF_THE_DAY, null);
            if (mealJson != null) {
                try {
                    Meal savedMeal = new Gson().fromJson(mealJson, Meal.class);
                    view.showMealOfTheDay(savedMeal);
                    return;
                } catch (Exception e) {
                    // Proceed to fetch new meal if parsing fails
                }
            }
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

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(PREF_MEAL_OF_THE_DAY, new Gson().toJson(todaysMeal));
                    editor.putString(PREF_MEAL_DATE, getCurrentDate());
                    editor.apply();
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
                view.showSearchResults(meals);
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
}
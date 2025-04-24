package com.example.gustoguru.features.meal.presenter;

import com.example.gustoguru.features.meal.view.MealDetailView;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.remote.retrofit.callback.MealCallback;
import com.example.gustoguru.model.repository.MealRepository;

import java.util.List;

public class MealDetailPresenter {
    private final MealDetailView view;
    private final MealRepository repository;

    public MealDetailPresenter(MealDetailView view, MealRepository repository) {
        this.view = view;
        this.repository = repository;
    }

    public void getMealDetails(String mealId) {
        repository.getMealById(mealId, new MealCallback() {
            @Override
            public void onSuccess(List<Meal> meals) {
                if (meals != null && !meals.isEmpty()) {
                    Meal meal = meals.get(0);
                    view.showMealDetails(meal);
                    view.showIngredients(meal.getIngredientMeasureMap());
                } else {
                    view.showError("Meal details not found");
                }
            }

            @Override
            public void onFailure(String message) {
                view.showError(message);
            }
        });
    }
}
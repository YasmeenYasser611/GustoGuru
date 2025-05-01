package com.example.gustoguru.features.favorites.presenter;

import androidx.lifecycle.Observer;

import com.example.gustoguru.features.favorites.view.FavoritesView;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.repository.MealRepository;

import java.util.ArrayList;
import java.util.List;

public class FavoritesPresenter {
    private FavoritesView view;
    private final MealRepository repository;

    private Meal lastRemovedMeal;
    private int lastRemovedPosition = -1;
    private List<Meal> currentMeals = new ArrayList<>();

    public FavoritesPresenter(FavoritesView view, MealRepository repository) {
        this.view = view;
        this.repository = repository;
    }

    public void loadFavorites() {
        view.showLoading();
        repository.getAllFavorites().observeForever(new Observer<List<Meal>>() {
            @Override
            public void onChanged(List<Meal> meals) {
                view.hideLoading();
                currentMeals = meals;
                if (meals == null || meals.isEmpty()) {
                    view.showError("No favorites found");
                } else {
                    view.showFavorites(meals);
                }
            }
        });
    }

    public void toggleFavorite(Meal meal, int position) {
        if (meal.isFavorite()) {
            lastRemovedMeal = meal;
            lastRemovedPosition = position;
            repository.removeFavorite(meal);
            view.removeMealAt(position);
            view.showUndoSnackbar(meal);
        } else {
            repository.addFavorite(meal);
        }
    }

    public void undoLastRemoval() {
        if (lastRemovedMeal != null && lastRemovedPosition != -1) {

            int safePosition = Math.min(lastRemovedPosition, currentMeals.size());
            repository.addFavorite(lastRemovedMeal);
            view.insertMealAt(lastRemovedMeal, safePosition);
            lastRemovedMeal = null;
            lastRemovedPosition = -1;
        }
    }


}

package com.example.gustoguru.features.favorites.presenter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.gustoguru.features.favorites.view.FavoritesView;
import com.example.gustoguru.features.profile.view.ProfileView;
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
    private LiveData<List<Meal>> favoritesLiveData;
    private Observer<List<Meal>> favoritesObserver;

    public FavoritesPresenter(FavoritesView view, MealRepository repository)
    {
        this.view = view;
        this.repository = repository;
        this.favoritesObserver = new Observer<List<Meal>>() {
            @Override
            public void onChanged(List<Meal> meals) {
                if (view != null && view.isActive()) {  // Add this check
                    view.hideLoading();
                    if (meals == null || meals.isEmpty()) {
                        view.showError("No favorites found");
                    } else {
                        view.showFavorites(meals);
                    }
                }
            }
        };
    }

public void loadFavorites() {
    if (view != null) {
        view.showLoading();
    }
    favoritesLiveData = repository.getUserFavorites();
    favoritesLiveData.observeForever(favoritesObserver);
}

    public void detachView() {
        if (favoritesLiveData != null && favoritesObserver != null) {
            favoritesLiveData.removeObserver(favoritesObserver);
        }
        view = null;
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


    public void attachView(FavoritesView view) {
        this.view = view;
    }


}

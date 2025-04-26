package com.example.gustoguru.features.favorites.presenter;

import androidx.lifecycle.Observer;

import com.example.gustoguru.features.favorites.view.FavoritesView;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.repository.MealRepository;

import java.util.List;

public class FavoritesPresenter {
    private final FavoritesView view;
    private final MealRepository repository;

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
                if (meals == null || meals.isEmpty()) {
                    view.showError("No favorites found");
                } else {
                    view.showFavorites(meals);
                }
            }
        });
    }

    public void toggleFavorite(Meal meal) {
        if (meal.isFavorite()) {
            repository.removeFavorite(meal);
        } else {
            repository.addFavorite(meal);
        }
    }
}

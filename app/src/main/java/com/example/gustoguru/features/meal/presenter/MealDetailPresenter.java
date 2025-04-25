package com.example.gustoguru.features.meal.presenter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.gustoguru.features.meal.view.MealDetailView;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.remote.retrofit.callback.MealCallback;
import com.example.gustoguru.model.repository.MealRepository;

import java.util.List;

public class MealDetailPresenter {
    private final MealDetailView view;
    private final MealRepository repository;
    private Meal currentMeal;
    private boolean isFavorite = false;

    public MealDetailPresenter(MealDetailView view, MealRepository repository) {
        this.view = view;
        this.repository = repository;
    }

    public void getMealDetails(String mealId) {
        view.showLoading();
        repository.getMealById(mealId, new MealCallback() {
            @Override
            public void onSuccess(List<Meal> meals) {
                if (meals != null && !meals.isEmpty()) {
                    currentMeal = meals.get(0);
                    view.showMealDetails(currentMeal);
                    view.showIngredients(currentMeal.getIngredientMeasureMap());
                    view.showYoutubeVideo(currentMeal.getStrYoutube());
                    checkFavoriteStatus();
                } else {
                    view.showError("Meal details not found");
                }
                view.hideLoading();
            }

            @Override
            public void onFailure(String message) {
                view.showError(message);
                view.hideLoading();
            }
        });
    }

    public void toggleFavorite() {
        if (currentMeal == null) return;

        if (isFavorite) {
            repository.removeFavorite(currentMeal);
            isFavorite = false;
        } else {
            repository.addFavorite(currentMeal);
            isFavorite = true;
        }
        view.showFavoriteStatus(isFavorite);
    }

    private void checkFavoriteStatus() {
        if (currentMeal == null || currentMeal.getIdMeal() == null) return;

        LiveData<List<Meal>> favoritesLiveData = repository.getAllFavorites();
        favoritesLiveData.observeForever(new Observer<List<Meal>>() {
            @Override
            public void onChanged(List<Meal> meals) {
                isFavorite = false;
                if (meals != null) {
                    for (Meal meal : meals) {
                        if (meal.getIdMeal() != null && meal.getIdMeal().equals(currentMeal.getIdMeal())) {
                            isFavorite = true;
                            break;
                        }
                    }
                }
                view.showFavoriteStatus(isFavorite);
                favoritesLiveData.removeObserver(this);
            }
        });
    }
}
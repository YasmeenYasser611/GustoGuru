package com.example.gustoguru.features.meal.presenter;

import com.example.gustoguru.features.meal.view.MealDetailView;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.remote.retrofit.callback.MealCallback;
import com.example.gustoguru.model.repository.MealRepository;

import java.util.List;

public class MealDetailPresenter {
    private final MealDetailView view;
    private final MealRepository repository;
    private Meal currentMeal;

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

//        boolean newFavoriteStatus = !currentMeal.isFavorite();
//        currentMeal.setFavorite(newFavoriteStatus);
//
//        repository.toggleFavorite(currentMeal, new FavoriteCallback() {
//            @Override
//            public void onSuccess() {
//                view.showFavoriteStatus(newFavoriteStatus);
//            }
//
//            @Override
//            public void onFailure(String error) {
//                view.showError("Failed to update favorite: " + error);
//                // Revert the UI state if the operation failed
//                view.showFavoriteStatus(!newFavoriteStatus);
//            }
//        });
    }

    private void checkFavoriteStatus() {
//        repository.isFavorite(currentMeal.getIdMeal(), new FavoriteCallback() {
//            @Override
//            public void onSuccess() {
//                view.showFavoriteStatus(currentMeal.isFavorite());
//            }
//
//            @Override
//            public void onFailure(String error) {
//                view.showError("Failed to check favorite status: " + error);
//            }
//        });
    }
}
package com.example.gustoguru.features.favorites.view;

import com.example.gustoguru.model.pojo.Meal;

import java.util.List;

public interface FavoritesView {
    void showFavorites(List<Meal> meals);
    void showError(String message);
    void showLoading();
    void hideLoading();
}
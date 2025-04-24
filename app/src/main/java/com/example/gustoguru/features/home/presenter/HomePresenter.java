package com.example.gustoguru.features.home.presenter;


import com.example.gustoguru.features.home.view.HomeView;
import com.example.gustoguru.model.pojo.Category;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.remote.retrofit.callback.CategoryCallback;
import com.example.gustoguru.model.remote.retrofit.callback.MealCallback;
import com.example.gustoguru.model.repository.MealRepository;

import java.util.List;

public class HomePresenter {
    private final HomeView view;
    private final MealRepository repository;


    public HomePresenter(HomeView view, MealRepository repository) {
        this.view = view;
        this.repository = repository;

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


}
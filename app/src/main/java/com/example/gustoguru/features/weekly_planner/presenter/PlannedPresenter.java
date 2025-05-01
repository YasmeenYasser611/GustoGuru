package com.example.gustoguru.features.weekly_planner.presenter;

import androidx.lifecycle.Observer;

import com.example.gustoguru.features.weekly_planner.view.PlannedView;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.repository.MealRepository;

import java.util.ArrayList;
import java.util.List;

public class PlannedPresenter {
    private PlannedView view;
    private final MealRepository repository;

    private Meal lastRemovedMeal;
    private int lastRemovedPosition = -1;
    private List<Meal> currentMeals = new ArrayList<>();

    public PlannedPresenter(PlannedView view, MealRepository repository) {
        this.view = view;
        this.repository = repository;
    }

    public void loadPlannedMeals() {
        view.showLoading();
        repository.getAllPlannedMeals().observeForever(new Observer<List<Meal>>() {
            @Override
            public void onChanged(List<Meal> meals) {
                view.hideLoading();
                currentMeals = meals;
                if (meals == null || meals.isEmpty()) {
                    view.showError("No planned meals found");
                } else {
                    view.showPlannedMeals(meals);
                }
            }
        });
    }

    public void removePlannedMeal(Meal meal, int position) {
        lastRemovedMeal = meal;
        lastRemovedPosition = position;
        repository.addPlannedMeal(meal , "");
        view.removeMealAt(position);
        view.showUndoSnackbar(meal);
    }

    public void undoLastRemoval() {
        if (lastRemovedMeal != null && lastRemovedPosition != -1) {
            int safePosition = Math.min(lastRemovedPosition, currentMeals.size());
            repository.addPlannedMeal(lastRemovedMeal, lastRemovedMeal.getPlannedDate());
            view.insertMealAt(lastRemovedMeal, safePosition);
            lastRemovedMeal = null;
            lastRemovedPosition = -1;
        }
    }
    public void attachView(PlannedView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;

    }
}
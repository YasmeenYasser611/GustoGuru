package com.example.gustoguru.features.weekly_planner.presenter;

import androidx.lifecycle.LiveData;
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

    private LiveData<List<Meal>> plannedMealsLiveData;
    private Observer<List<Meal>> plannedMealsObserver;

    public PlannedPresenter(PlannedView view, MealRepository repository) {
        this.view = view;
        this.repository = repository;
        this.plannedMealsObserver = new Observer<List<Meal>>() {
            @Override
            public void onChanged(List<Meal> meals) {
                if (view != null) {  // Add null check
                    view.hideLoading();
                    if (meals == null || meals.isEmpty()) {
                        view.showEmptyView();
                    } else {
                        view.showPlannedMeals(meals);
                    }
                }
            }
        };
    }



    public void loadPlannedMeals() {
        if (view != null) {  // Add null check
            view.showLoading();
        }
        plannedMealsLiveData = repository.getUserPlannedMeals();
        plannedMealsLiveData.observeForever(plannedMealsObserver);
    }

    public void detachView() {
        if (plannedMealsLiveData != null && plannedMealsObserver != null) {
            plannedMealsLiveData.removeObserver(plannedMealsObserver);
        }
        view = null;  // Clear reference to prevent leaks
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


}
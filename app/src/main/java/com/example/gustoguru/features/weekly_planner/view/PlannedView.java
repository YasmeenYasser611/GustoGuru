package com.example.gustoguru.features.weekly_planner.view;

import com.example.gustoguru.model.pojo.Meal;

import java.util.Calendar;
import java.util.List;

public interface PlannedView
{
    void showPlannedMeals(List<Meal> meals);
    void markDatesWithMeals(List<Calendar> dates);
    void showError(String message);
    void showLoading();
    void hideLoading();
    void showUndoSnackbar(Meal meal);
    void showEmptyView();
    void removeMealAt(int position);
    void insertMealAt(Meal meal, int position);
}
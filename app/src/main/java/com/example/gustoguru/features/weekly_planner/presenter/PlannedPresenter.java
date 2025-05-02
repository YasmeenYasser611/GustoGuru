package com.example.gustoguru.features.weekly_planner.presenter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.gustoguru.features.weekly_planner.view.PlannedView;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.repository.MealRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class PlannedPresenter {
    private PlannedView view;
    private final MealRepository repository;
    private final Map<String, List<Meal>> mealsByDate = new HashMap<>();

    private Meal lastRemovedMeal;
    private int lastRemovedPosition = -1;
    private String currentSelectedDate;


    private LiveData<List<Meal>> plannedMealsLiveData;
    private Observer<List<Meal>> plannedMealsObserver;


    public PlannedPresenter(PlannedView view, MealRepository repository) {
        this.view = view;
        this.repository = repository;
        initObserver();
    }

    private void initObserver() {
        plannedMealsObserver = new Observer<List<Meal>>() {
            @Override
            public void onChanged(List<Meal> allMeals) {
                if (view == null) return;

                // Group meals by date
                mealsByDate.clear();
                for (Meal meal : allMeals) {
                    if (meal.getPlannedDate() != null) {
                        mealsByDate.computeIfAbsent(meal.getPlannedDate(), k -> new ArrayList<>())
                                .add(meal);
                    }
                }

                // Update calendar markers
                List<Calendar> datesWithMeals = mealsByDate.keySet().stream()
                        .map(dateStr -> {
                            Calendar cal = Calendar.getInstance();
                            try {
                                cal.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            return cal;
                        })
                        .collect(Collectors.toList());

                if (view != null) {
                    view.markDatesWithMeals(datesWithMeals);
                }

                // Show meals for selected date
                if (currentSelectedDate != null) {
                    List<Meal> mealsForDate = mealsByDate.get(currentSelectedDate);
                    if (view != null) {
                        if (mealsForDate == null || mealsForDate.isEmpty()) {
                            view.showEmptyView();
                        } else {
                            view.showPlannedMeals(mealsForDate);
                        }
                    }
                }
            }
        };
    }

    public void loadPlannedMealsForDate(String date) {
        this.currentSelectedDate = date;
        if (view != null) {
//            view.showLoading();
        }

        if (plannedMealsLiveData != null) {
            plannedMealsLiveData.removeObserver(plannedMealsObserver);
        }

        plannedMealsLiveData = repository.getUserPlannedMeals();
        plannedMealsLiveData.observeForever(plannedMealsObserver);
    }

    public void detachView() {
        if (plannedMealsLiveData != null && plannedMealsObserver != null) {
            plannedMealsLiveData.removeObserver(plannedMealsObserver);
        }
        view = null;
    }

    public void removePlannedMeal(Meal meal, int position) {
        lastRemovedMeal = meal;
        lastRemovedPosition = position;
        // CORRECTED: Actually remove the meal instead of adding empty date
        repository.removePlannedMeal(meal);
        view.removeMealAt(position);
        view.showUndoSnackbar(meal);
    }

    public void undoLastRemoval() {
        if (lastRemovedMeal != null && lastRemovedPosition != -1) {
            // Restore with original planned date
            repository.addPlannedMeal(lastRemovedMeal, lastRemovedMeal.getPlannedDate());
            view.insertMealAt(lastRemovedMeal, lastRemovedPosition);

            // Refresh data to ensure consistency
            loadPlannedMealsForDate(currentSelectedDate);

            lastRemovedMeal = null;
            lastRemovedPosition = -1;
        }
    }

}
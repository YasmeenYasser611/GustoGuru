package com.example.gustoguru.features.meal.presenter;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.gustoguru.features.meal.view.calender.CalendarManager;
import com.example.gustoguru.features.meal.view.MealDetailView;
import com.example.gustoguru.features.sessionmanager.SessionManager;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.remote.retrofit.callback.MealCallback;
import com.example.gustoguru.model.repository.MealRepository;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MealDetailPresenter {
    private MealDetailView view;
    private final MealRepository repository;
    private Meal currentMeal;
    private boolean isFavorite = false;
    private final CalendarManager calendarManager;
    private final SessionManager sessionManager;

    public MealDetailPresenter(MealDetailView view, MealRepository repository, Context context) {
        this.view = view;
        this.repository = repository;
        this.sessionManager = new SessionManager(context);
        this.calendarManager = new CalendarManager(context);
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
        if (!sessionManager.isLoggedIn()) {
            view.showLoginRequired("Please register or login to add favorites");
            return;
        }

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



    public void handleDateSelected(Calendar selectedDate)
    {
        if (!sessionManager.isLoggedIn()) {
            view.showLoginRequired("Please register or login to plan meals");
            return;
        }

        if (currentMeal == null) {
            view.showError("No meal selected");
            return;
        }

        // Format date for our planner
        String formattedDate = String.format(Locale.getDefault(),
                "%04d-%02d-%02d",
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH) + 1,
                selectedDate.get(Calendar.DAY_OF_MONTH));


        repository.addPlannedMeal(currentMeal, formattedDate);
        view.showPlannerSuccess(formattedDate);


        calendarManager.addMealToCalendar(currentMeal, selectedDate, new CalendarManager.CalendarOperationCallback()
        {
            @Override
            public void onSuccess() {
                view.showCalendarSuccess();
            }

            @Override
            public void onFailure(String message) {
                view.showError(message);
            }

            @Override
            public void onPermissionRequired(int requestCode) {
                view.requestCalendarPermission(requestCode);
            }
        });
    }
    public void onDestroy() {
        view = null;
    }

}
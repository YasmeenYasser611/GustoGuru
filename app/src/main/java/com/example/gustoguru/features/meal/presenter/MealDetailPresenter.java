package com.example.gustoguru.features.meal.presenter;

import static com.example.gustoguru.features.meal.view.calender.CalendarManager.REQUEST_CALENDAR_PERMISSION;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.gustoguru.features.meal.view.MealDetailFragment;
import com.example.gustoguru.features.meal.view.calender.CalendarManager;
import com.example.gustoguru.features.meal.view.MealDetailView;
import com.example.gustoguru.features.sessionmanager.SessionManager;
import com.example.gustoguru.model.network.NetworkUtil;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.remote.retrofit.callback.MealCallback;
import com.example.gustoguru.model.repository.MealRepository;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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




    public void handleDateSelected(Calendar selectedDate) {
        if (!sessionManager.isLoggedIn()) {
            view.showLoginRequired("Please register or login to plan meals");
            return;
        }

        if (currentMeal == null) {
            view.showError("No meal selected");
            return;
        }

        String formattedDate = formatDateForPlanner(selectedDate);
        repository.addPlannedMeal(currentMeal, formattedDate);
        view.showPlannerSuccess(formattedDate);

        addToCalendar(selectedDate);
    }

    private String formatDateForPlanner(Calendar date) {
        return String.format(Locale.getDefault(),
                "%04d-%02d-%02d",
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH));
    }

    private void addToCalendar(Calendar selectedDate) {
        calendarManager.addMealToCalendar(currentMeal, selectedDate,
                new CalendarManager.CalendarOperationCallback() {
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

    public void checkCalendarPermission() {
        if (hasCalendarPermission()) {
            showDatePicker();
        } else {
            view.requestCalendarPermission(REQUEST_CALENDAR_PERMISSION);
        }
    }

    private boolean hasCalendarPermission() {
        return ContextCompat.checkSelfPermission(
                ((MealDetailFragment)view).requireContext(),
                Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void showDatePicker() {
        Calendar today = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_YEAR, 6);
        view.showDatePicker(today, maxDate);
    }

    public void handleYoutubeVideo(String videoUrl) {
        if (videoUrl == null || videoUrl.isEmpty()) {
            view.showYoutubeVideo(null);
            return;
        }

        if (!NetworkUtil.isNetworkAvailable(((MealDetailFragment)view).requireContext())) {
            view.showYoutubeVideo(""); // Empty string indicates offline mode
            return;
        }

        String videoId = extractYouTubeId(videoUrl);
        view.showYoutubeVideo(videoId != null ? videoUrl : null);
    }

    private String extractYouTubeId(String url) {
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\\?video_id=)([^#\\&\\?\\n]*)";
        Matcher matcher = Pattern.compile(pattern).matcher(url);
        return matcher.find() ? matcher.group() : null;
    }

    public void onDestroy() {
        view = null;
    }

}
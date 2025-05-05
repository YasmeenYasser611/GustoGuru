package com.example.gustoguru.features.weekly_planner.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.lottie.LottieAnimationView;
import com.applandeo.materialcalendarview.CalendarView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.EventDay;
import com.example.gustoguru.R;
import com.example.gustoguru.features.navigation.view.NavigationCommunicator;
import com.example.gustoguru.features.weekly_planner.presenter.PlannedPresenter;
import com.example.gustoguru.model.local.AppDatabase;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.remote.firebase.FirebaseClient;
import com.example.gustoguru.model.remote.retrofit.client.MealClient;
import com.example.gustoguru.model.repository.MealRepository;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
public class PlannedFragment extends Fragment implements PlannedView {
    private PlannedPresenter presenter;
    private CalendarView calendarView;
    private RecyclerView rvDailyMeals;
    private TextView tvSelectedDate, tvEmpty;
    private ProgressBar progressBar;
    private DailyMealsAdapter dailyMealsAdapter;

    // Animation views
    private LottieAnimationView animationView;
    private TextView welcomeText;
    private TextView loadingMessage;
    private ViewGroup animationContainer;
    private boolean shouldSkipAnimation = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            shouldSkipAnimation = getArguments().getBoolean("skip_animation", false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_planned, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize animation views
        animationContainer = view.findViewById(R.id.animation_container);
        animationView = view.findViewById(R.id.weekly_loading_animation);
        welcomeText = view.findViewById(R.id.welcome_text);
        loadingMessage = view.findViewById(R.id.loading_message);

        // Initialize content views
        calendarView = view.findViewById(R.id.calendarView);
        rvDailyMeals = view.findViewById(R.id.rv_daily_meals);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        tvEmpty = view.findViewById(R.id.tv_empty);
        progressBar = view.findViewById(R.id.progressBar);

        // Setup RecyclerView
        dailyMealsAdapter = new DailyMealsAdapter(new ArrayList<>(), this::onMealClick, this::onRemoveClick);
        rvDailyMeals.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvDailyMeals.setAdapter(dailyMealsAdapter);

        // Initialize presenter
        presenter = new PlannedPresenter(
                this,
                MealRepository.getInstance(
                        AppDatabase.getInstance(requireContext()).favoriteMealDao(),
                        AppDatabase.getInstance(requireContext()).plannedMealDao(),
                        MealClient.getInstance(requireContext()),
                        FirebaseClient.getInstance()
                )
        );

        if (shouldSkipAnimation) {
            showContentViews();
            initializePlanner();
        } else {
            startLoadingAnimation();
        }
    }

    private void startLoadingAnimation() {
        try {
            animationView.setAnimation("weekly_planner.json");
            animationView.setRepeatCount(0);
            animationView.setSpeed(2.5f);
            animationView.loop(false);

            animationView.addAnimatorListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    showContentViews();
                    initializePlanner();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    showContentViews();
                    initializePlanner();
                }
            });

            animationView.playAnimation();
        } catch (Exception e) {
            Log.e("PlannedFragment", "Animation setup failed", e);
            showContentViews();
            initializePlanner();
        }
    }

    private void showContentViews() {
        // Get reference to content view
        View contentView = getView().findViewById(R.id.content_view);

        // Hide animation views
        animationContainer.setVisibility(View.GONE);

        // Show content views
        contentView.setVisibility(View.VISIBLE);
    }
    private void initializePlanner() {
        // Set current date
        Calendar today = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayStr = sdf.format(today.getTime());
        updateDateHeader(today);
        presenter.loadPlannedMealsForDate(todayStr);

        // Calendar date selection listener
        calendarView.setOnDayClickListener(eventDay -> {
            Calendar clickedDate = eventDay.getCalendar();
            String dateStr = sdf.format(clickedDate.getTime());
            updateDateHeader(clickedDate);
            presenter.loadPlannedMealsForDate(dateStr);
        });
    }

    private void updateDateHeader(Calendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        tvSelectedDate.setText(sdf.format(date.getTime()));
    }

    private void onMealClick(Meal meal) {
        if (getActivity() instanceof NavigationCommunicator) {
            ((NavigationCommunicator) getActivity()).navigateToMealDetail(meal.getIdMeal());
        }
    }

    private void onRemoveClick(Meal meal, int position) {
        presenter.removePlannedMeal(meal, position);
    }

    @Override
    public void showPlannedMeals(List<Meal> meals) {
        requireActivity().runOnUiThread(() -> {
            dailyMealsAdapter.updateMeals(meals);
            tvEmpty.setVisibility(meals.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void markDatesWithMeals(List<Calendar> dates) {
        requireActivity().runOnUiThread(() -> {
            List<EventDay> eventDays = new ArrayList<>();
            for (Calendar date : dates) {
                eventDays.add(new EventDay(date, R.drawable.ic_meal_event));
            }
            calendarView.setEvents(eventDays);
        });
    }

    @Override
    public void showError(String message) {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void showLoading() {
        requireActivity().runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
    }

    @Override
    public void hideLoading() {
        requireActivity().runOnUiThread(() -> progressBar.setVisibility(View.GONE));
    }

    @Override
    public void showEmptyView() {
        requireActivity().runOnUiThread(() -> {
            tvEmpty.setVisibility(View.VISIBLE);
            dailyMealsAdapter.updateMeals(new ArrayList<>());
        });
    }

    @Override
    public void removeMealAt(int position) {
        requireActivity().runOnUiThread(() -> {
            dailyMealsAdapter.removeAt(position);
            if (dailyMealsAdapter.getItemCount() == 0) {
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void insertMealAt(Meal meal, int position) {
        requireActivity().runOnUiThread(() -> {
            dailyMealsAdapter.insertAt(meal, position);
            rvDailyMeals.scrollToPosition(position);
            tvEmpty.setVisibility(View.GONE);
        });
    }

    @Override
    public void showUndoSnackbar(Meal meal) {
        requireActivity().runOnUiThread(() -> {
            Snackbar snackbar = Snackbar.make(
                    requireView(),
                    "Meal removed from planned meals",
                    Snackbar.LENGTH_LONG
            );

            snackbar.setAction("UNDO", v -> {
                if (isAdded() && !requireActivity().isFinishing()) {
                    presenter.undoLastRemoval();
                }
            });

            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    if (event != DISMISS_EVENT_ACTION && isAdded() && !requireActivity().isFinishing()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        presenter.loadPlannedMealsForDate(sdf.format(Calendar.getInstance().getTime()));
                    }
                }
            });

            snackbar.show();
        });
    }

    @Override
    public void onDestroyView() {
        if (animationView != null) {
            animationView.cancelAnimation();
            animationView.removeAllAnimatorListeners();
        }
        if (presenter != null) {
            presenter.detachView();
        }
        super.onDestroyView();
    }
}
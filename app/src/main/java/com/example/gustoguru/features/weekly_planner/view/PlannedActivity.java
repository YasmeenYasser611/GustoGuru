package com.example.gustoguru.features.weekly_planner.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gustoguru.R;
import com.example.gustoguru.features.meal.view.MealAdapter;
import com.example.gustoguru.features.meal.view.MealDetailActivity;
import com.example.gustoguru.features.weekly_planner.presenter.PlannedPresenter;
import com.example.gustoguru.model.local.AppDatabase;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.remote.firebase.FirebaseClient;
import com.example.gustoguru.model.remote.retrofit.client.MealClient;
import com.example.gustoguru.model.repository.MealRepository;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class PlannedActivity extends AppCompatActivity implements PlannedView {
    private PlannedPresenter presenter;
    private RecyclerView recyclerView;
    private MealAdapter adapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planned);

        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.rv_planned);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MealAdapter(this, new ArrayList<>(), this::onMealClick, null); // No favorite click here
        recyclerView.setAdapter(adapter);

        presenter = new PlannedPresenter(
                this,
                MealRepository.getInstance(
                        AppDatabase.getInstance(this).favoriteMealDao(),
                        AppDatabase.getInstance(this).plannedMealDao(),
                        MealClient.getInstance(),
                        FirebaseClient.getInstance()
                )
        );

        presenter.loadPlannedMeals();
    }

    private void onMealClick(Meal meal) {
        Intent intent = new Intent(this, MealDetailActivity.class);
        intent.putExtra("MEAL_ID", meal.getIdMeal());
        startActivity(intent);
    }

    @Override
    public void showPlannedMeals(List<Meal> meals) {
        adapter.updateMeals(meals);
    }



    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showUndoSnackbar(Meal meal) {
        Snackbar snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                "Meal removed from planned meals",
                Snackbar.LENGTH_LONG
        );

        snackbar.setAction("UNDO", v -> {
            if (!isFinishing() && !isDestroyed()) {
                presenter.undoLastRemoval();
            }
        });

        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                if (event != DISMISS_EVENT_ACTION && !isFinishing() && !isDestroyed()) {
                    presenter.loadPlannedMeals();
                }
            }
        });

        snackbar.show();
    }

    @Override
    public void removeMealAt(int position) {
        adapter.removeAt(position);
        if (adapter.getItemCount() == 0) {
            showError("No planned meals found");
        }
    }

    @Override
    public void insertMealAt(Meal meal, int position) {
        adapter.insertAt(meal, position);
        recyclerView.scrollToPosition(position);
    }
}

package com.example.gustoguru.features.search.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gustoguru.R;
import com.example.gustoguru.features.meal.view.MealAdapter;
import com.example.gustoguru.features.meal.view.MealDetailActivity;
import com.example.gustoguru.features.search.presenter.SearchPresenter;
import com.example.gustoguru.model.local.AppDatabase;
import com.example.gustoguru.model.pojo.Area;
import com.example.gustoguru.model.pojo.Category;
import com.example.gustoguru.model.pojo.FilteredMeal;
import com.example.gustoguru.model.pojo.Ingredient;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.remote.firebase.FirebaseClient;
import com.example.gustoguru.model.remote.retrofit.client.MealClient;
import com.example.gustoguru.model.repository.MealRepository;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements SearchView {
    private SearchPresenter presenter;
    private SearchAdapter adapter;
    private ChipGroup filterGroup;
    private RecyclerView searchResultsRecyclerView;
    private ProgressBar progressBar;
    private TextInputEditText searchEditText;
    private MaterialAutoCompleteTextView searchMethodSpinner;
    private ArrayAdapter<String> searchMethodAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize views
        searchEditText = findViewById(R.id.searchEditText);
        searchMethodSpinner = findViewById(R.id.searchMethodSpinner);
        filterGroup = findViewById(R.id.filterGroup);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        // Initialize presenter
        AppDatabase db = AppDatabase.getInstance(this);
        presenter = new SearchPresenter(
                this,
                MealRepository.getInstance(
                        db.favoriteMealDao(),
                        db.plannedMealDao(),
                        MealClient.getInstance(),
                        FirebaseClient.getInstance()
                )
        );

        setupSearchMethodSpinner();
        setupSearchBar();
        setupRecyclerView();
    }

    private void setupSearchMethodSpinner() {
        String[] searchMethods = new String[]{"Name", "Category", "Ingredient", "Area"};

        // Create adapter with custom layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.dropdown_menu_item,
                searchMethods
        );

        // Get reference to the TextInputLayout
        TextInputLayout searchMethodContainer = findViewById(R.id.searchMethodContainer);

        // Configure the dropdown
        MaterialAutoCompleteTextView autoCompleteTextView =
                (MaterialAutoCompleteTextView) searchMethodContainer.getEditText();

        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String method = adapter.getItem(position);
            filterGroup.setVisibility(method.equals("Name") ? View.GONE : View.VISIBLE);

            // Clear previous results
            this.adapter.updateMeals(new ArrayList<>());

            switch (method) {
                case "Category":
                    presenter.loadCategories();
                    break;
                case "Ingredient":
                    presenter.loadIngredients();
                    break;
                case "Area":
                    presenter.loadAreas();
                    break;
                case "Name":
                    searchEditText.requestFocus();
                    break;
            }
        });

        // Set default selection
        autoCompleteTextView.setText(adapter.getItem(0), false);
    }

    private void setupSearchBar() {
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void performSearch() {
        String query = searchEditText.getText().toString().trim();
        String method = searchMethodSpinner.getText().toString();

        if (method.equals("Name")) {
            if (query.isEmpty()) {
                showError("Please enter a search term");
                return;
            }
            presenter.searchByName(query);
        }
        // Other methods are handled by chip selection
    }

    private void setupRecyclerView() {
        adapter = new SearchAdapter(this, new ArrayList<>(), meal -> {
            Intent intent = new Intent(this, MealDetailActivity.class);
            intent.putExtra("MEAL_ID", meal.getIdMeal());
            startActivity(intent);
        });
        searchResultsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        searchResultsRecyclerView.setAdapter(adapter);
    }

    @Override
    public void showSearchResults(List<FilteredMeal> meals) {
        adapter.updateMeals(meals);
    }

    @Override
    public void showCategories(List<Category> categories) {
        filterGroup.removeAllViews();
        for (Category category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category.getStrCategory());
            chip.setOnClickListener(v -> {
                searchEditText.setText(""); // Clear text search
                presenter.searchByCategory(category.getStrCategory());
            });
            filterGroup.addView(chip);
        }
    }

    @Override
    public void showIngredients(List<Ingredient> ingredients) {
        filterGroup.removeAllViews();
        for (Ingredient ingredient : ingredients) {
            Chip chip = new Chip(this);
            chip.setText(ingredient.getStrIngredient());
            chip.setOnClickListener(v -> {
                searchEditText.setText(""); // Clear text search
                presenter.searchByIngredient(ingredient.getStrIngredient());
            });
            filterGroup.addView(chip);
        }
    }

    @Override
    public void showAreas(List<Area> areas) {
        filterGroup.removeAllViews();
        for (Area area : areas) {
            Chip chip = new Chip(this);
            chip.setText(area.getStrArea());
            chip.setOnClickListener(v -> {
                searchEditText.setText(""); // Clear text search
                presenter.searchByArea(area.getStrArea());
            });
            filterGroup.addView(chip);
        }
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
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
package com.example.gustoguru.features.search.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
public class SearchActivity extends AppCompatActivity implements SearchView {
    private SearchPresenter presenter;
    private SearchAdapter adapter;
    private RecyclerView searchResultsRecyclerView;
    private ProgressBar progressBar;
    private TextInputEditText searchEditText;
    private LinearLayout searchContainer;
    private ImageButton btnCloseSearch;
    private ImageView searchIcon;
    private boolean isSearchExpanded = false;
    private RecyclerView suggestionsRecyclerView;
    private SuggestionAdapter suggestionAdapter;
    private MaterialAutoCompleteTextView searchMethodSpinner;
    private TextView suggestionsTitle;
    private ViewGroup filterGroup;
    private List<Category> allCategories = new ArrayList<>();
    private List<Ingredient> allIngredients = new ArrayList<>();
    private List<Area> allAreas = new ArrayList<>();
    private Animation loadingFadeIn;  // Reuses your search_expand.xml
    private Animation loadingFadeOut; // Reuses your search_collapse.xml

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        loadingFadeIn = AnimationUtils.loadAnimation(this, R.anim.search_collapse);
        loadingFadeOut = AnimationUtils.loadAnimation(this, R.anim.search_expand);

        // Initialize views
        initViews();

        // Setup components
        setupRecyclerView();
        setupSearchBarAnimations();
        setupSearchFunctionality();
        setupSearchMethodSpinner();

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

        // Load initial data for suggestions
        presenter.loadCategories();
        presenter.loadIngredients();
        presenter.loadAreas();
    }
    @Override
    public void showLoading() {
        runOnUiThread(() -> {
            progressBar.startAnimation(loadingFadeIn);
            progressBar.setVisibility(View.VISIBLE);
            searchResultsRecyclerView.startAnimation(loadingFadeOut);
        });
    }

    @Override
    public void hideLoading() {
        runOnUiThread(() -> {
            progressBar.startAnimation(loadingFadeOut);
            progressBar.setVisibility(View.GONE);
            searchResultsRecyclerView.startAnimation(loadingFadeIn);
        });
    }

    private void initViews() {
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        searchEditText = findViewById(R.id.searchEditText);
        searchContainer = findViewById(R.id.searchContainer);
        btnCloseSearch = findViewById(R.id.btnCloseSearch);
        searchIcon = findViewById(R.id.searchIcon);
        suggestionsRecyclerView = findViewById(R.id.suggestionsRecyclerView);
        searchMethodSpinner = findViewById(R.id.searchMethodSpinner);

//        filterGroup = findViewById(R.id.filterGroup);
    }



    private void setupRecyclerView() {
        adapter = new SearchAdapter(this, new ArrayList<>(), meal -> {
            Intent intent = new Intent(this, MealDetailActivity.class);
            intent.putExtra("MEAL_ID", meal.getIdMeal());
            startActivity(intent);
        });
        searchResultsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        searchResultsRecyclerView.setAdapter(adapter);

        suggestionAdapter = new SuggestionAdapter(suggestion -> {
            searchEditText.setText("");
            String method = searchMethodSpinner.getText().toString();
            switch (method) {
                case "Category": presenter.searchByCategory(suggestion); break;
                case "Ingredient": presenter.searchByIngredient(suggestion); break;
                case "Area": presenter.searchByArea(suggestion); break;
            }
        });
        suggestionsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        suggestionsRecyclerView.setAdapter(suggestionAdapter);
    }

    // Your original animation methods - completely preserved
    private void setupSearchBarAnimations() {
        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                expandSearch();
            } else {
                collapseSearch();
            }
        });

        btnCloseSearch.setOnClickListener(v -> {
            searchEditText.setText("");
            searchEditText.clearFocus();
            hideKeyboard();
        });

        searchIcon.setOnClickListener(v -> searchEditText.requestFocus());
    }

    private void expandSearch() {
        if (isSearchExpanded) return;
        isSearchExpanded = true;

        Animation expandAnim = AnimationUtils.loadAnimation(this, R.anim.search_expand);
        searchContainer.startAnimation(expandAnim);

        btnCloseSearch.setVisibility(View.VISIBLE);
        searchIcon.setVisibility(View.GONE);

        // Move search bar to top when expanded
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) searchContainer.getLayoutParams();
        params.gravity = Gravity.TOP;
        params.topMargin = getResources().getDimensionPixelSize(R.dimen.search_margin_top);
        searchContainer.setLayoutParams(params);

        showKeyboard();
    }

    private void collapseSearch() {
        if (!isSearchExpanded) return;
        isSearchExpanded = false;

        Animation collapseAnim = AnimationUtils.loadAnimation(this, R.anim.search_collapse);
        searchContainer.startAnimation(collapseAnim);

        btnCloseSearch.setVisibility(View.GONE);
        searchIcon.setVisibility(View.VISIBLE);

        // Move search bar back to center when collapsed
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) searchContainer.getLayoutParams();
        params.gravity = Gravity.CENTER;
        searchContainer.setLayoutParams(params);

        hideKeyboard();
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
    }

    private void setupSearchFunctionality() {
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        // Add text watcher for real-time suggestions
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSuggestions(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch() {
        String query = searchEditText.getText().toString().trim();
        String method = searchMethodSpinner.getText().toString();

        if (!query.isEmpty()) {
            if (method.equals("Name")) {
                presenter.searchByName(query);
            }
            searchEditText.clearFocus();
        }
    }

    private void setupSearchMethodSpinner() {
        String[] searchMethods = {"Name", "Category", "Ingredient", "Area"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                R.layout.dropdown_menu_item,
                searchMethods
        );

        searchMethodSpinner.setAdapter(spinnerAdapter);
        searchMethodSpinner.setText(searchMethods[0], false);

        searchMethodSpinner.setOnItemClickListener((parent, view, position, id) -> {
            String method = spinnerAdapter.getItem(position);
            updateUIForSearchMethod(method);
            filterSuggestions(searchEditText.getText().toString());
        });
    }

    private void updateUIForSearchMethod(String method) {
        if (suggestionsTitle != null) {
            suggestionsTitle.setText(method.equals("Name") ? "Suggestions" : method + " Suggestions");
        }

        searchEditText.setHint(method.equals("Name")
                ? "Search meal names..."
                : "Filter " + method.toLowerCase() + "...");

        if (filterGroup != null) {
            filterGroup.setVisibility(method.equals("Name") ? View.GONE : View.VISIBLE);
        }
    }

    private void filterSuggestions(String query) {
        if (query.isEmpty()) {
            suggestionAdapter.updateSuggestions(Collections.emptyList());
            return;
        }

        String method = searchMethodSpinner.getText().toString();
        List<String> filtered = new ArrayList<>();

        switch (method) {
            case "Category":
                for (Category c : allCategories) {
                    if (c.getStrCategory().toLowerCase().contains(query.toLowerCase())) {
                        filtered.add(c.getStrCategory());
                    }
                }
                break;
            case "Ingredient":
                for (Ingredient i : allIngredients) {
                    if (i.getStrIngredient().toLowerCase().contains(query.toLowerCase())) {
                        filtered.add(i.getStrIngredient());
                    }
                }
                break;
            case "Area":
                for (Area a : allAreas) {
                    if (a.getStrArea().toLowerCase().contains(query.toLowerCase())) {
                        filtered.add(a.getStrArea());
                    }
                }
                break;
        }

        suggestionAdapter.updateSuggestions(filtered);
    }

    // SearchView implementation
    @Override
    public void showSearchResults(List<FilteredMeal> meals) {
        adapter.updateMeals(meals);
    }

    @Override
    public void showCategories(List<Category> categories) {
        allCategories = categories;
        filterSuggestions(searchEditText.getText().toString());
    }

    @Override
    public void showIngredients(List<Ingredient> ingredients) {
        allIngredients = ingredients;
        filterSuggestions(searchEditText.getText().toString());
    }

    @Override
    public void showAreas(List<Area> areas) {
        allAreas = areas;
        filterSuggestions(searchEditText.getText().toString());
    }



    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (isSearchExpanded) {
            searchEditText.clearFocus();
        } else {
            super.onBackPressed();
        }
    }
}
package com.example.gustoguru.features.search.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.gustoguru.R;
import com.example.gustoguru.features.navigation.view.NavigationCommunicator;
import com.example.gustoguru.features.search.presenter.SearchPresenter;
import com.example.gustoguru.features.search.view.suggestions.SuggestionAdapter;
import com.example.gustoguru.model.local.AppDatabase;
import com.example.gustoguru.model.pojo.Area;
import com.example.gustoguru.model.pojo.Category;
import com.example.gustoguru.model.pojo.FilteredMeal;
import com.example.gustoguru.model.pojo.Ingredient;
import com.example.gustoguru.model.remote.firebase.FirebaseClient;
import com.example.gustoguru.model.remote.retrofit.client.MealClient;
import com.example.gustoguru.model.repository.MealRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements SearchView {
    // Content Views
    private RecyclerView searchResultsRecyclerView;
    private ProgressBar progressBar;
    private TextInputEditText searchEditText;
    private RecyclerView suggestionsRecyclerView;
    private MaterialButton btnSearchByName, btnSearchByIngredient, btnSearchByCategory, btnSearchByCountry;

    // Animation Views
    private LottieAnimationView animationView;
    private TextView welcomeText;
    private TextView loadingMessage;
    private ViewGroup animationContainer;

    // Adapters
    private SearchAdapter adapter;
    private SuggestionAdapter suggestionAdapter;

    // Presenter
    private SearchPresenter presenter;
    private String currentSearchMethod = "Name";
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
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize animation views
        animationContainer = view.findViewById(R.id.animation_container);
        animationView = view.findViewById(R.id.search_loading_animation);
        welcomeText = view.findViewById(R.id.welcome_text);
        loadingMessage = view.findViewById(R.id.loading_message);

        // Initialize content views
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        searchEditText = view.findViewById(R.id.searchEditText);
        suggestionsRecyclerView = view.findViewById(R.id.suggestionsRecyclerView);
        btnSearchByName = view.findViewById(R.id.btnSearchByName);
        btnSearchByIngredient = view.findViewById(R.id.btnSearchByIngredient);
        btnSearchByCategory = view.findViewById(R.id.btnSearchByCategory);
        btnSearchByCountry = view.findViewById(R.id.btnSearchByCountry);

        if (shouldSkipAnimation) {
            showContentViews();
            initializeSearchComponents();
        } else {
            startLoadingAnimation();
        }
    }

    private void startLoadingAnimation() {
        try {
            animationView.setAnimation("search.json");
            animationView.setRepeatCount(0);
            animationView.setSpeed(2.5f);
            animationView.loop(false);

            animationView.addAnimatorListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    showContentViews();
                    initializeSearchComponents();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    showContentViews();
                    initializeSearchComponents();
                }
            });

            animationView.playAnimation();
        } catch (Exception e) {
            Log.e("SearchFragment", "Animation setup failed", e);
            showContentViews();
            initializeSearchComponents();
        }
    }

    private void showContentViews() {
        // Hide animation views
        animationContainer.setVisibility(View.GONE);

        // Show content views
        View contentView = getView().findViewById(R.id.content_view);
        if (contentView != null) {
            contentView.setVisibility(View.VISIBLE);
        }
    }

    private void initializeSearchComponents() {
        setupRecyclerViews();
        initializePresenter();
        setupSearchFunctionality();
    }

    private void setupRecyclerViews() {
        // Search results
        adapter = new SearchAdapter(requireContext(), new ArrayList<>(), meal -> {
            navigateToMealDetail(meal.getIdMeal());
        });
        searchResultsRecyclerView.setAdapter(adapter);

        // Suggestions
        suggestionAdapter = new SuggestionAdapter(this::handleSuggestionClick);
        suggestionsRecyclerView.setAdapter(suggestionAdapter);
    }

    private void handleSuggestionClick(String suggestion) {
        searchEditText.setText(suggestion);
        performSearch();
    }

    private void navigateToMealDetail(String mealId) {
        if (getActivity() instanceof NavigationCommunicator) {
            ((NavigationCommunicator) getActivity()).navigateToMealDetail(mealId);
        }
    }

    private void initializePresenter() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        presenter = new SearchPresenter(
                this,
                MealRepository.getInstance(
                        db.favoriteMealDao(),
                        db.plannedMealDao(),
                        MealClient.getInstance(requireContext()),
                        FirebaseClient.getInstance()
                )
        );

        // Load initial data
        presenter.loadCategories();
        presenter.loadIngredients();
        presenter.loadAreas();
    }

    private void setupSearchFunctionality() {
        setupSearchMethodButtons();
        setupSearchTextListener();
        setSearchMethod(currentSearchMethod);
    }

    private void setupSearchMethodButtons() {
        btnSearchByName.setOnClickListener(v -> setSearchMethod("Name"));
        btnSearchByIngredient.setOnClickListener(v -> setSearchMethod("Ingredient"));
        btnSearchByCategory.setOnClickListener(v -> setSearchMethod("Category"));
        btnSearchByCountry.setOnClickListener(v -> setSearchMethod("Country"));
    }

    private void setSearchMethod(String method) {
        currentSearchMethod = method;
        updateButtonStates();
        searchEditText.setHint("Search by " + method.toLowerCase() + "...");
        filterSuggestions(searchEditText.getText().toString());
    }

    private void updateButtonStates() {
        int selectedColor = ContextCompat.getColor(requireContext(), R.color.gray);
        int defaultColor = ContextCompat.getColor(requireContext(), R.color.gray_light);

        btnSearchByName.setBackgroundColor(currentSearchMethod.equals("Name") ? selectedColor : defaultColor);
        btnSearchByIngredient.setBackgroundColor(currentSearchMethod.equals("Ingredient") ? selectedColor : defaultColor);
        btnSearchByCategory.setBackgroundColor(currentSearchMethod.equals("Category") ? selectedColor : defaultColor);
        btnSearchByCountry.setBackgroundColor(currentSearchMethod.equals("Country") ? selectedColor : defaultColor);
    }

    private void setupSearchTextListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSuggestions(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

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
        if (!query.isEmpty()) {
            presenter.performSearch(query, currentSearchMethod);
        }
    }

    private void filterSuggestions(String query) {
        presenter.filterSuggestions(query, currentSearchMethod);
    }

    // SearchView implementation
    @Override
    public void showLoading() {
        requireActivity().runOnUiThread(() -> {
            progressBar.setVisibility(View.VISIBLE);
            searchResultsRecyclerView.setVisibility(View.GONE);
        });
    }

    @Override
    public void hideLoading() {
        requireActivity().runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
        });
    }

    @Override
    public void showSearchResults(List<FilteredMeal> meals) {
        requireActivity().runOnUiThread(() -> {
            suggestionsRecyclerView.setVisibility(View.GONE);
            searchResultsRecyclerView.setVisibility(View.VISIBLE);

            if (meals == null || meals.isEmpty()) {
                showError("No meals found");
            } else {
                adapter.updateMeals(meals);
            }
        });
    }

    @Override
    public void showCategories(List<Category> categories) {
        presenter.setCategories(categories);
    }

    @Override
    public void showIngredients(List<Ingredient> ingredients) {
        presenter.setIngredients(ingredients);
    }

    @Override
    public void showAreas(List<Area> areas) {
        presenter.setAreas(areas);
    }

    @Override
    public void showSuggestions(List<String> suggestions) {
        requireActivity().runOnUiThread(() -> {
            boolean hasSuggestions = suggestions != null && !suggestions.isEmpty();
            suggestionsRecyclerView.setVisibility(hasSuggestions ? View.VISIBLE : View.GONE);
            suggestionAdapter.updateSuggestions(suggestions);
        });
    }

    @Override
    public void showError(String message) {
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
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
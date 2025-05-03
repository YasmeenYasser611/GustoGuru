package com.example.gustoguru.features.search.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gustoguru.R;
import com.example.gustoguru.features.home.view.NavigationCommunicator;
import com.example.gustoguru.features.search.presenter.SearchPresenter;
import com.example.gustoguru.model.local.AppDatabase;
import com.example.gustoguru.model.pojo.Area;
import com.example.gustoguru.model.pojo.Category;
import com.example.gustoguru.model.pojo.FilteredMeal;
import com.example.gustoguru.model.pojo.Ingredient;
import com.example.gustoguru.model.remote.firebase.FirebaseClient;
import com.example.gustoguru.model.remote.retrofit.client.MealClient;
import com.example.gustoguru.model.repository.MealRepository;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;


public class SearchFragment extends Fragment implements SearchView {
    // Views
    private RecyclerView searchResultsRecyclerView;
    private ProgressBar progressBar;
    private TextInputEditText searchEditText;
    private LinearLayout searchContainer;
    private ImageButton btnCloseSearch;
    private ImageView searchIcon;
    private RecyclerView suggestionsRecyclerView;
    private MaterialAutoCompleteTextView searchMethodSpinner;
    private TextView suggestionsTitle;

    // Adapters
    private SearchAdapter adapter;
    private SuggestionAdapter suggestionAdapter;


    // Presenter
    private SearchPresenter presenter;

    // State
    private boolean isSearchExpanded = false;
    private Animation loadingFadeIn;
    private Animation loadingFadeOut;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeAnimations();
        initializeViews(view);
        setupRecyclerViews();
        setupSearchBar();
        setupSearchFunctionality();
        initializePresenter();
    }

    // Initialization methods
    private void initializeAnimations() {
        loadingFadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.search_collapse);
        loadingFadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.search_expand);
    }

    private void initializeViews(View view) {
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        searchEditText = view.findViewById(R.id.searchEditText);
        searchContainer = view.findViewById(R.id.searchContainer);
        btnCloseSearch = view.findViewById(R.id.btnCloseSearch);
        searchIcon = view.findViewById(R.id.searchIcon);
        suggestionsRecyclerView = view.findViewById(R.id.suggestionsRecyclerView);
        searchMethodSpinner = view.findViewById(R.id.searchMethodSpinner);

        // Initialize suggestionsTitle safely
        suggestionsTitle = view.findViewById(R.id.suggestionsTitle);
        if (suggestionsTitle != null) {
            suggestionsTitle.setVisibility(View.GONE);
        }

        // Set initial state
        btnCloseSearch.setVisibility(View.GONE);
        searchIcon.setVisibility(View.VISIBLE);
        centerSearchContainer();
    }

    private void centerSearchContainer() {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) searchContainer.getLayoutParams();
        params.gravity = Gravity.CENTER;
        searchContainer.setLayoutParams(params);
    }

    private void setupRecyclerViews() {
        setupSearchResultsRecyclerView();
        setupSuggestionsRecyclerView();
    }

    private void setupSearchResultsRecyclerView() {
        adapter = new SearchAdapter(requireContext(), new ArrayList<>(), meal -> {
            navigateToMealDetail(meal.getIdMeal());
        });
        searchResultsRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        searchResultsRecyclerView.setAdapter(adapter);
    }

    private void setupSuggestionsRecyclerView() {
        suggestionAdapter = new SuggestionAdapter(this::handleSuggestionClick);
        suggestionsRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        suggestionsRecyclerView.setAdapter(suggestionAdapter);
    }

    private void handleSuggestionClick(String suggestion) {
        searchEditText.setText("");
        String method = searchMethodSpinner.getText().toString();
        presenter.performSearch(suggestion);
    }

    private void navigateToMealDetail(String mealId) {
        if (getActivity() instanceof NavigationCommunicator) {
            ((NavigationCommunicator) getActivity()).navigateToMealDetail(mealId);
        }
    }

    // Search functionality
    private void setupSearchFunctionality() {
        setupSearchMethodSpinner();
        setupSearchTextListener();
    }

    private void setupSearchMethodSpinner() {
        String[] searchMethods = {"Name", "Category", "Ingredient", "Area"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.dropdown_menu_item,
                searchMethods
        );
        searchMethodSpinner.setAdapter(spinnerAdapter);
        searchMethodSpinner.setText(searchMethods[0], false);

        searchMethodSpinner.setOnItemClickListener((parent, view, position, id) -> {
            updateUIForSearchMethod(spinnerAdapter.getItem(position));
            filterSuggestions(searchEditText.getText().toString());
        });
    }

    private void updateUIForSearchMethod(String method) {
        searchEditText.setHint(method.equals("Name")
                ? "Search meal names..."
                : "Filter " + method.toLowerCase() + "...");
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
        presenter.performSearch(query);
    }

    private void filterSuggestions(String query) {
        String method = searchMethodSpinner.getText().toString();
        presenter.filterSuggestions(query, method);
    }

    // Search bar animations
    private void setupSearchBar() {
        setupSearchBarAnimations();
        setupSearchIconClick();
        setupCloseButtonClick();
    }

    private void setupSearchBarAnimations() {
        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                expandSearch();
            } else {
                collapseSearch();
            }
        });
    }

    private void setupSearchIconClick() {
        searchIcon.setOnClickListener(v -> searchEditText.requestFocus());
    }

    private void setupCloseButtonClick() {
        btnCloseSearch.setOnClickListener(v -> {
            searchEditText.setText("");
            searchEditText.clearFocus();
            hideKeyboard();
        });
    }

    private void expandSearch() {
        if (isSearchExpanded) return;
        isSearchExpanded = true;

        Animation expandAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.search_expand);
        searchContainer.startAnimation(expandAnim);

        btnCloseSearch.setVisibility(View.VISIBLE);
        searchIcon.setVisibility(View.GONE);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) searchContainer.getLayoutParams();
        params.gravity = Gravity.TOP;
        params.topMargin = getResources().getDimensionPixelSize(R.dimen.search_margin_top);
        searchContainer.setLayoutParams(params);
        if (suggestionAdapter.getItemCount() > 0) {
            suggestionsTitle.setVisibility(View.VISIBLE);
        }

        showKeyboard();
    }

    private void collapseSearch() {
        if (!isSearchExpanded) return;
        isSearchExpanded = false;

        Animation collapseAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.search_collapse);
        searchContainer.startAnimation(collapseAnim);

        btnCloseSearch.setVisibility(View.GONE);
        searchIcon.setVisibility(View.VISIBLE);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) searchContainer.getLayoutParams();
        params.topMargin = getResources().getDimensionPixelSize(R.dimen.search_margin_top);
        searchContainer.setLayoutParams(params);
        suggestionsTitle.setVisibility(View.GONE);
        suggestionAdapter.updateSuggestions(new ArrayList<>());

        hideKeyboard();
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
    }

    // Presenter initialization
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

        presenter.loadCategories();
        presenter.loadIngredients();
        presenter.loadAreas();
    }

    // SearchView implementation
    @Override
    public void showLoading() {
        requireActivity().runOnUiThread(() -> {
            progressBar.startAnimation(loadingFadeIn);
            progressBar.setVisibility(View.VISIBLE);
            searchResultsRecyclerView.startAnimation(loadingFadeOut);
        });
    }

    @Override
    public void hideLoading() {
        requireActivity().runOnUiThread(() -> {
            progressBar.startAnimation(loadingFadeOut);
            progressBar.setVisibility(View.GONE);
            searchResultsRecyclerView.startAnimation(loadingFadeIn);
        });
    }

    @Override
    public void showSearchResults(List<FilteredMeal> meals) {
        requireActivity().runOnUiThread(() -> {
            suggestionsTitle.setVisibility(View.GONE); // Hide when showing results
            suggestionsRecyclerView.setVisibility(View.GONE);
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
        filterSuggestions(searchEditText.getText().toString());
    }

    @Override
    public void showIngredients(List<Ingredient> ingredients) {
        presenter.setIngredients(ingredients);
        filterSuggestions(searchEditText.getText().toString());
    }

    @Override
    public void showAreas(List<Area> areas) {
        presenter.setAreas(areas);
        filterSuggestions(searchEditText.getText().toString());
    }

    @Override
    public void showSuggestions(List<String> suggestions) {
        requireActivity().runOnUiThread(() -> {
            boolean hasSuggestions = suggestions != null && !suggestions.isEmpty();
            suggestionsTitle.setVisibility(hasSuggestions ? View.VISIBLE : View.GONE);
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
        super.onDestroyView();
        if (presenter != null) {
            presenter.detachView();
        }
    }
}
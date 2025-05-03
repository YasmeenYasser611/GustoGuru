package com.example.gustoguru.features.home.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gustoguru.R;
import com.example.gustoguru.features.home.presenter.HomePresenter;
import com.example.gustoguru.features.meal.view.MealAdapter;
import com.example.gustoguru.features.meal.view.MealDetailFragment;
import com.example.gustoguru.features.sessionmanager.SessionManager;
import com.example.gustoguru.model.local.AppDatabase;
import com.example.gustoguru.model.network.NetworkUtil;
import com.example.gustoguru.model.pojo.Area;
import com.example.gustoguru.model.pojo.Category;
import com.example.gustoguru.model.pojo.FilteredMeal;
import com.example.gustoguru.model.pojo.Ingredient;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.remote.firebase.FirebaseClient;
import com.example.gustoguru.model.remote.retrofit.client.MealClient;
import com.example.gustoguru.model.repository.MealRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
public class HomeFragment extends Fragment implements HomeView,
        CategoryAdapter.OnCategoryClickListener,
        AreaAdapter.OnAreaClickListener,
        IngredientAdapter.OnIngredientClickListener {

    // Views
    private TextView tvGreeting;
    private ImageView ivMealOfTheDay;
    private TextView tvMealOfTheDayName;

    // Categories section
    private RecyclerView categoriesContainer;
    private RecyclerView mealsByCategoryContainer;
    private TextView tvMealsByCategoryTitle;

    // Areas section
    private RecyclerView areasContainer;
    private RecyclerView mealsByAreaContainer;
    private TextView tvMealsByAreaTitle;

    // Ingredients section
    private RecyclerView ingredientsContainer;
    private RecyclerView mealsByIngredientContainer;
    private TextView tvIngredientsTitle;
    private TextView tvMealsByIngredientTitle;

    // Adapters
    private CategoryAdapter categoryAdapter;
    private MealAdapter mealsByCategoryAdapter;
    private AreaAdapter areaAdapter;
    private MealAdapter mealsByAreaAdapter;
    private IngredientAdapter ingredientAdapter;
    private MealAdapter mealsByIngredientAdapter;

    // Presenter and dependencies
    private HomePresenter presenter;
    private SessionManager sessionManager;
    private HomeCommunicator communicator;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof HomeCommunicator) {
            communicator = (HomeCommunicator) context;
        } else {
            throw new RuntimeException(context + " must implement HomeCommunicator");
        }
        sessionManager = new SessionManager(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupAdapters();
        setupRecyclerViews();
        initializePresenter();
        setupPersonalizedGreeting();
        loadData();
    }

    private void initializeViews(View view) {
        tvGreeting = view.findViewById(R.id.tvGreeting);
        ivMealOfTheDay = view.findViewById(R.id.ivMealOfTheDay);
        tvMealOfTheDayName = view.findViewById(R.id.tvMealOfTheDayName);

        // Categories section
        categoriesContainer = view.findViewById(R.id.categoriesContainer);
        mealsByCategoryContainer = view.findViewById(R.id.mealsByCategoryContainer);
        tvMealsByCategoryTitle = view.findViewById(R.id.tvMealsByCategoryTitle);

        // Areas section
        areasContainer = view.findViewById(R.id.areasContainer);
        mealsByAreaContainer = view.findViewById(R.id.mealsByAreaContainer);
        tvMealsByAreaTitle = view.findViewById(R.id.tvMealsByAreaTitle);

        // Ingredients section
        ingredientsContainer = view.findViewById(R.id.ingredientsContainer);
        mealsByIngredientContainer = view.findViewById(R.id.mealsByIngredientContainer);
        tvIngredientsTitle = view.findViewById(R.id.tvIngredientsTitle);
        tvMealsByIngredientTitle = view.findViewById(R.id.tvMealsByIngredientTitle);

    }

    private void setupAdapters() {
        // Category adapters
        categoryAdapter = new CategoryAdapter(requireContext(), new ArrayList<>(), this);
        mealsByCategoryAdapter = new MealAdapter(requireContext(), new ArrayList<>(),
                meal -> communicator.navigateToMealDetail(meal.getIdMeal()), null);

        // Area adapters
        areaAdapter = new AreaAdapter(requireContext(), new ArrayList<>(), this);
        mealsByAreaAdapter = new MealAdapter(requireContext(), new ArrayList<>(),
                meal -> communicator.navigateToMealDetail(meal.getIdMeal()), null);

        // Ingredient adapters
        ingredientAdapter = new IngredientAdapter(requireContext(), new ArrayList<>(), this);
        mealsByIngredientAdapter = new MealAdapter(requireContext(), new ArrayList<>(),
                meal -> communicator.navigateToMealDetail(meal.getIdMeal()), null);
    }

    private void setupRecyclerViews() {
        // Categories setup
        categoriesContainer.setLayoutManager(new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false));
        categoriesContainer.setAdapter(categoryAdapter);

        mealsByCategoryContainer.setLayoutManager(new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false));
        mealsByCategoryContainer.setAdapter(mealsByCategoryAdapter);

        // Areas setup
        areasContainer.setLayoutManager(new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false));
        areasContainer.setAdapter(areaAdapter);

        mealsByAreaContainer.setLayoutManager(new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false));
        mealsByAreaContainer.setAdapter(mealsByAreaAdapter);

        // Ingredients setup
        ingredientsContainer.setLayoutManager(new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false));
        ingredientsContainer.setAdapter(ingredientAdapter);

        mealsByIngredientContainer.setLayoutManager(new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false));
        mealsByIngredientContainer.setAdapter(mealsByIngredientAdapter);
    }

    private void initializePresenter() {
        presenter = new HomePresenter(this,
                MealRepository.getInstance(
                        AppDatabase.getInstance(requireContext()).favoriteMealDao(),
                        AppDatabase.getInstance(requireContext()).plannedMealDao(),
                        MealClient.getInstance(requireContext()),
                        FirebaseClient.getInstance()
                ),
                requireContext()
        );
    }

    private void setupPersonalizedGreeting() {
        String userName = sessionManager.getUserName();
        String greetingText = sessionManager.isLoggedIn()
                ? (!userName.isEmpty() ? String.format("Hey chef %s 👩‍🍳", userName) : "Hey Chef! 👩‍🍳")
                : "Hey Guest Chef! 👨‍🍳";
        tvGreeting.setText(greetingText);
    }

    private void loadData() {
        presenter.getRandomMeal();
        presenter.getAllCategories();
        presenter.getAllAreas();
        presenter.getPopularIngredients();
    }

    // Click handlers
    @Override
    public void onCategoryClick(Category category) {
        hideAllMealSections();
        tvMealsByCategoryTitle.setVisibility(View.VISIBLE);
        mealsByCategoryContainer.setVisibility(View.VISIBLE);
        tvMealsByCategoryTitle.setText("Meals in " + category.getStrCategory());
        presenter.searchByCategory(category.getStrCategory());
    }

    @Override
    public void onAreaClick(Area area) {
        hideAllMealSections();
        tvMealsByAreaTitle.setVisibility(View.VISIBLE);
        mealsByAreaContainer.setVisibility(View.VISIBLE);
        tvMealsByAreaTitle.setText("Meals from " + area.getStrArea());
        presenter.searchByArea(area.getStrArea());
    }

    @Override
    public void onIngredientClick(Ingredient ingredient) {
        hideAllMealSections();
        tvMealsByIngredientTitle.setVisibility(View.VISIBLE);
        mealsByIngredientContainer.setVisibility(View.VISIBLE);
        tvMealsByIngredientTitle.setText("Meals with " + ingredient.getStrIngredient());
        presenter.searchByIngredient(ingredient.getStrIngredient());
    }

    private void hideAllMealSections() {
        tvMealsByCategoryTitle.setVisibility(View.GONE);
        mealsByCategoryContainer.setVisibility(View.GONE);
        tvMealsByAreaTitle.setVisibility(View.GONE);
        mealsByAreaContainer.setVisibility(View.GONE);
        tvMealsByIngredientTitle.setVisibility(View.GONE);
        mealsByIngredientContainer.setVisibility(View.GONE);
    }

    // View interface implementations
    @Override
    public void showMealOfTheDay(Meal meal) {
        requireActivity().runOnUiThread(() -> {
            tvMealOfTheDayName.setText(meal.getStrMeal());
            Glide.with(requireContext())
                    .load(meal.getStrMealThumb())
                    .centerCrop()
                    .into(ivMealOfTheDay);

            requireView().findViewById(R.id.mealOfTheDayCard).setOnClickListener(v -> {
                if (getActivity() instanceof NavigationCommunicator) {
                    ((NavigationCommunicator) getActivity()).navigateToMealDetail(meal.getIdMeal());
                }
            });
        });
    }

    @Override
    public void showSearchResults(List<FilteredMeal> meals) {

    }

    @Override
    public void showCategories(List<Category> categories) {
        categoryAdapter.setCategories(categories);

    }

    @Override
    public void showAreas(List<Area> areas) {
        areaAdapter.setAreas(areas);
    }

    @Override
    public void showIngredients(List<Ingredient> ingredients) {
        ingredientAdapter.setIngredients(ingredients);
    }


    @Override
    public void showMealsByCategory(List<FilteredMeal> meals) {
        if (meals == null || meals.isEmpty()) {
            hideAllMealSections();
            showError("No meals found in this category");
        } else {
            updateMealsAdapter(meals, mealsByCategoryAdapter);
        }
    }

    @Override

    public void showMealsByArea(List<FilteredMeal> meals) {
        if (meals == null || meals.isEmpty()) {
            hideAllMealSections();
            showError("No meals found from this country");
        } else {
            updateMealsAdapter(meals, mealsByAreaAdapter);
        }
    }

    @Override
    public void showMealsByIngredient(List<FilteredMeal> meals) {
        if (meals == null || meals.isEmpty()) {
            hideAllMealSections();
            showError("No meals found with this ingredient");
        } else {
            updateMealsAdapter(meals, mealsByIngredientAdapter);
        }
    }

    private void updateMealsAdapter(List<FilteredMeal> filteredMeals, MealAdapter adapter) {
        List<Meal> meals = new ArrayList<>();
        for (FilteredMeal filteredMeal : filteredMeals) {
            Meal meal = new Meal();
            meal.setIdMeal(filteredMeal.getIdMeal());
            meal.setStrMeal(filteredMeal.getStrMeal());
            meal.setStrMealThumb(filteredMeal.getStrMealThumb());
            meals.add(meal);
        }
        adapter.updateMeals(meals);
    }

    @Override
    public void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.onDestroy();

    }



}
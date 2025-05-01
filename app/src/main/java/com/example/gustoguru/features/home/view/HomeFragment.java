package com.example.gustoguru.features.home.view;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.gustoguru.model.pojo.Category;
import com.example.gustoguru.model.pojo.FilteredMeal;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.remote.firebase.FirebaseClient;
import com.example.gustoguru.model.remote.retrofit.client.MealClient;
import com.example.gustoguru.model.repository.MealRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment implements HomeView, CategoryAdapter.OnCategoryClickListener {
    private HomePresenter presenter;
    private RecyclerView categoriesContainer;
    private CategoryAdapter categoryAdapter;
    private TextView tvMealOfTheDayName;
    private ImageView ivMealOfTheDay;
    private RecyclerView mealsByCategoryContainer;
    private TextView tvMealsByCategoryTitle;
    private MealAdapter mealsAdapter;
    private TextView tvGreeting;
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

    private void setupPersonalizedGreeting() {
        String userName = sessionManager.getUserName();
        String greetingText;

        if (sessionManager.isLoggedIn()) {
            greetingText = !userName.isEmpty()
                    ? String.format("Hey chef %s 👩‍🍳", userName)
                    : "Hey Chef! 👩‍🍳";
        } else {
            greetingText = "Hey Guest Chef! 👨‍🍳";
        }

        tvGreeting.setText(greetingText);
    }

    private void initializeViews(View view) {
        tvGreeting = view.findViewById(R.id.tvGreeting);
        ivMealOfTheDay = view.findViewById(R.id.ivMealOfTheDay);
        tvMealOfTheDayName = view.findViewById(R.id.tvMealOfTheDayName);
        categoriesContainer = view.findViewById(R.id.categoriesContainer);
        mealsByCategoryContainer = view.findViewById(R.id.mealsByCategoryContainer);
        tvMealsByCategoryTitle = view.findViewById(R.id.tvMealsByCategoryTitle);
    }

    private void setupAdapters() {
        categoryAdapter = new CategoryAdapter(requireContext(), new ArrayList<>(), this);
        mealsAdapter = new MealAdapter(requireContext(), new ArrayList<>(),
                meal -> communicator.navigateToMealDetail(meal.getIdMeal()),
                null);
    }

    private void setupRecyclerViews() {
        categoriesContainer.setHasFixedSize(true);
        categoriesContainer.setLayoutManager(new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false));
        categoriesContainer.setAdapter(categoryAdapter);

        mealsByCategoryContainer.setHasFixedSize(true);
        mealsByCategoryContainer.setLayoutManager(new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false));
        mealsByCategoryContainer.setAdapter(mealsAdapter);
    }

    private void initializePresenter() {
        presenter = new HomePresenter(this,
                MealRepository.getInstance(
                        AppDatabase.getInstance(requireContext()).favoriteMealDao(),
                        AppDatabase.getInstance(requireContext()).plannedMealDao(),
                        MealClient.getInstance(),
                        FirebaseClient.getInstance()
                ),
                requireContext()
        );
    }

    private void loadData() {
        presenter.getRandomMeal();
        presenter.getAllCategories();
    }

    @Override
    public void onCategoryClick(Category category) {
        tvMealsByCategoryTitle.setVisibility(View.VISIBLE);
        mealsByCategoryContainer.setVisibility(View.VISIBLE);
        tvMealsByCategoryTitle.setText("Meals in " + category.getStrCategory());
        presenter.searchByCategory(category.getStrCategory());
    }

    @Override
    public void showCategories(List<Category> categories) {
        categoryAdapter.setCategories(categories);
    }

    @Override
    public void showMealOfTheDay(Meal meal) {
        requireActivity().runOnUiThread(() -> {
            tvMealOfTheDayName.setText(meal.getStrMeal());
            Glide.with(requireContext())
                    .load(meal.getStrMealThumb())
                    .centerCrop()
                    .into(ivMealOfTheDay);

            requireView().findViewById(R.id.mealOfTheDayCard).setOnClickListener(v -> {
                // Get the communicator from the host activity
                if (getActivity() instanceof NavigationCommunicator) {
                    ((NavigationCommunicator) getActivity()).navigateToMealDetail(meal.getIdMeal());
                }
            });
        });
    }

    @Override
    public void showSearchResults(List<FilteredMeal> meals) {
        if (meals == null || meals.isEmpty()) {
            showError("No meals found in this category");
            mealsAdapter.updateMeals(new ArrayList<>());
        } else {
            List<Meal> mealList = new ArrayList<>();
            for (FilteredMeal filteredMeal : meals) {
                Meal meal = new Meal();
                meal.setIdMeal(filteredMeal.getIdMeal());
                meal.setStrMeal(filteredMeal.getStrMeal());
                meal.setStrMealThumb(filteredMeal.getStrMealThumb());
                mealList.add(meal);
            }
            mealsAdapter.updateMeals(mealList);
        }
    }

    @Override
    public void showError(String message) {
        Toast.makeText(requireContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.onDestroy();
    }
}

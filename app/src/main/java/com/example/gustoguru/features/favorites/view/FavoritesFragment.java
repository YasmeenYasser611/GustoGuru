package com.example.gustoguru.features.favorites.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.gustoguru.R;
import com.example.gustoguru.features.favorites.presenter.FavoritesPresenter;
import com.example.gustoguru.features.navigation.view.NavigationCommunicator;
import com.example.gustoguru.features.meal.view.MealAdapter;
import com.example.gustoguru.model.local.AppDatabase;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.remote.firebase.FirebaseClient;
import com.example.gustoguru.model.remote.retrofit.client.MealClient;
import com.example.gustoguru.model.repository.MealRepository;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
public class FavoritesFragment extends Fragment implements FavoritesView {
    private FavoritesPresenter presenter;
    private RecyclerView recyclerView;
    private MealAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    // Animation views
    private LottieAnimationView animationView;
    private TextView welcomeText;
    private TextView loadingMessage;
    private ViewGroup animationContainer;
    private boolean shouldSkipAnimation = false;

    public FavoritesFragment() {
        // Required empty public constructor
    }

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
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize animation views
        animationContainer = view.findViewById(R.id.animation_container);
        animationView = view.findViewById(R.id.fav_loading_animation);
        welcomeText = view.findViewById(R.id.welcome_text);
        loadingMessage = view.findViewById(R.id.loading_message);

        // Initialize content views
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tv_empty);
        recyclerView = view.findViewById(R.id.rv_favorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new MealAdapter(requireContext(), new ArrayList<>(), this::onMealClick, this::onFavoriteClick);
        recyclerView.setAdapter(adapter);

        presenter = new FavoritesPresenter(
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
            presenter.loadFavorites();
        } else {
            startLoadingAnimation();
        }
    }

    private void startLoadingAnimation() {
        try {
            animationView.setAnimation("fav.json");
            animationView.setRepeatCount(0);
            animationView.setSpeed(2.5f);
            animationView.loop(false);

            animationView.addAnimatorListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    showContentViews();
                    presenter.loadFavorites();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    showContentViews();
                    presenter.loadFavorites();
                }
            });

            animationView.playAnimation();
        } catch (Exception e) {
            Log.e("FavoritesFragment", "Animation setup failed", e);
            showContentViews();
            presenter.loadFavorites();
        }
    }

    private void showContentViews() {
        // Hide animation views
        animationContainer.setVisibility(View.GONE);

        // Show content views
        recyclerView.setVisibility(View.VISIBLE);
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

    @Override
    public boolean isActive() {
        return isAdded() && !isDetached() && getActivity() != null;
    }

    private void onMealClick(Meal meal) {
        if (getActivity() instanceof NavigationCommunicator) {
            ((NavigationCommunicator) getActivity()).navigateToMealDetail(meal.getIdMeal());
        }
    }

    private void onFavoriteClick(Meal meal, int position) {
        presenter.toggleFavorite(meal, position);
    }

    @Override
    public void showFavorites(List<Meal> meals) {
        requireActivity().runOnUiThread(() -> {
            adapter.updateMeals(meals);
            tvEmpty.setVisibility(meals.isEmpty() ? View.VISIBLE : View.GONE);
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
    public void showUndoSnackbar(Meal meal) {
        requireActivity().runOnUiThread(() -> {
            Snackbar snackbar = Snackbar.make(
                    requireView(),
                    "Meal removed from favorites",
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
                        presenter.loadFavorites();
                    }
                }
            });

            snackbar.show();
        });
    }

    @Override
    public void removeMealAt(int position) {
        requireActivity().runOnUiThread(() -> {
            adapter.removeAt(position);
            if (adapter.getItemCount() == 0) {
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void insertMealAt(Meal meal, int position) {
        requireActivity().runOnUiThread(() -> {
            adapter.insertAt(meal, position);
            recyclerView.scrollToPosition(position);
            tvEmpty.setVisibility(View.GONE);
        });
    }
}
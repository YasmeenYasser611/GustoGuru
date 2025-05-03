package com.example.gustoguru.features.favorites.view;

import android.os.Bundle;
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

import com.example.gustoguru.R;
import com.example.gustoguru.features.favorites.presenter.FavoritesPresenter;
import com.example.gustoguru.features.home.view.NavigationCommunicator;
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

    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.detachView();
        }
    }

    @Override
    public boolean isActive() {
        return isAdded() && !isDetached() && getActivity() != null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


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

        presenter.loadFavorites();
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
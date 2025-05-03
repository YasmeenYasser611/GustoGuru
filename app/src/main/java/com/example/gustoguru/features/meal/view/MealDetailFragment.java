package com.example.gustoguru.features.meal.view;

import static android.content.Intent.getIntent;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gustoguru.R;
import com.example.gustoguru.features.authentication.login.view.LoginActivity;
import com.example.gustoguru.features.home.view.CountryFlagUtils;
import com.example.gustoguru.features.meal.presenter.MealDetailPresenter;
import com.example.gustoguru.features.sessionmanager.SessionManager;
import com.example.gustoguru.model.local.AppDatabase;
import com.example.gustoguru.model.network.NetworkUtil;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.remote.firebase.FirebaseClient;
import com.example.gustoguru.model.remote.retrofit.client.MealClient;
import com.example.gustoguru.model.repository.MealRepository;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MealDetailFragment extends Fragment implements MealDetailView {
    private static final int REQUEST_CALENDAR_PERMISSION = 1001;
    private MealDetailPresenter presenter;
    private IngredientsAdapter ingredientsAdapter;

    // Views
    private ProgressBar progressBar;
    private ImageView ivMeal;
    private ImageButton btnFavorite;
    private TextView tvMealName, tvCategory, tvArea, tvInstructions;
    private YouTubePlayerView youtubePlayerView;
    private RecyclerView rvIngredients;
    private String mealId;
    private RecyclerView rvCountry;
    private CountryAdapter countryAdapter;
    private LinearLayout youtubeOfflineIndicator;

    public MealDetailFragment() {
        // Required empty public constructor
    }

    public static MealDetailFragment newInstance(String mealId) {
        MealDetailFragment fragment = new MealDetailFragment();
        Bundle args = new Bundle();
        args.putString("MEAL_ID", mealId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meal_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        progressBar = view.findViewById(R.id.progressBar);
        ivMeal = view.findViewById(R.id.ivMeal);
        btnFavorite = view.findViewById(R.id.btnFavorite);
        tvMealName = view.findViewById(R.id.tvMealName);
        tvCategory = view.findViewById(R.id.tvCategory);
        tvInstructions = view.findViewById(R.id.tvInstructions);
        youtubePlayerView = view.findViewById(R.id.youtubePlayerView);
        rvIngredients = view.findViewById(R.id.rvIngredients);
        rvCountry = view.findViewById(R.id.rvCountry);
        rvCountry.setLayoutManager(new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false));
        countryAdapter = new CountryAdapter(requireContext(), new ArrayList<>());
        rvCountry.setAdapter(countryAdapter);
        youtubeOfflineIndicator = view.findViewById(R.id.youtubeOfflineIndicator);

        // Setup RecyclerView
        setupRecyclerView();

        // Initialize presenter
        initializePresenter();

        // Get meal ID from arguments
        if (getArguments() != null) {
            mealId = getArguments().getString("MEAL_ID");
            loadMealDetails();
        }

        // Set up click listeners
        setupClickListeners();
    }

    private void setupClickListeners() {
        SessionManager sessionManager = new SessionManager(requireContext());

        if (!sessionManager.isLoggedIn()) {
            btnFavorite.setImageResource(R.drawable.ic_favorite_border);
            btnFavorite.setAlpha(0.5f);
        }

        btnFavorite.setOnClickListener(v -> {
            if (!sessionManager.isLoggedIn()) {
                showLoginRequired("Please register or login to add favorites");
                return;
            }
            presenter.toggleFavorite();
        });


        ImageButton btnAddToCalendar = requireView().findViewById(R.id.btnAddToCalendar);
        btnAddToCalendar.setOnClickListener(v -> {
            if (!sessionManager.isLoggedIn()) {
                showLoginRequired("Please register or login to plan meals");
                return;
            }
            showDatePickerForNextWeek(); // This will handle permission check/request
        });
    }

    private void setupRecyclerView() {
        ingredientsAdapter = new IngredientsAdapter(requireContext());
        rvIngredients.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvIngredients.setAdapter(ingredientsAdapter);
    }

    private void initializePresenter() {
        presenter = new MealDetailPresenter(
                this,
                MealRepository.getInstance(
                        AppDatabase.getInstance(requireContext()).favoriteMealDao(),
                        AppDatabase.getInstance(requireContext()).plannedMealDao(),
                        MealClient.getInstance(requireContext()),
                        FirebaseClient.getInstance()
                ),
                requireContext()
        );
    }

    private void loadMealDetails() {
        if (mealId == null || mealId.isEmpty()) {
            showError("Invalid meal ID");
            return;
        }
        presenter.getMealDetails(mealId);
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
    public void showLoginRequired(String message) {
        requireActivity().runOnUiThread(() -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Login Required")
                    .setMessage(message)
                    .setPositiveButton("Login", (dialog, which) -> {
                        startActivity(new Intent(requireContext(), LoginActivity.class));
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public void showMealDetails(Meal meal) {
        requireActivity().runOnUiThread(() -> {
            tvMealName.setText(meal.getStrMeal());
            tvCategory.setText(meal.getStrCategory());
            tvInstructions.setText(meal.getStrInstructions());

            Glide.with(requireContext())
                    .load(meal.getStrMealThumb())
                    .into(ivMeal);

            // Update country RecyclerView
            if (meal.getStrArea() != null && !meal.getStrArea().isEmpty()) {
                List<CountryAdapter.CountryItem> countries = new ArrayList<>();
                countries.add(new CountryAdapter.CountryItem(
                        meal.getStrArea(),
                        CountryFlagUtils.getFlagUrl(meal.getStrArea())
                ));
                countryAdapter = new CountryAdapter(requireContext(), countries);
                rvCountry.setAdapter(countryAdapter);
                rvCountry.setVisibility(View.VISIBLE);
            } else {
                rvCountry.setVisibility(View.GONE);
            }

            updateFavoriteButton(meal.isFavorite());
        });
    }

    @Override
    public void showIngredients(Map<String, String> ingredientMeasureMap) {
        requireActivity().runOnUiThread(() -> {
            ingredientsAdapter.setMeal(ingredientMeasureMap);
        });
    }


    @Override
    public void showYoutubeVideo(String videoUrl) {
        requireActivity().runOnUiThread(() -> {
            // Reset UI first
            youtubePlayerView.setVisibility(View.VISIBLE);
            youtubeOfflineIndicator.setVisibility(View.GONE);

            if (videoUrl == null || videoUrl.isEmpty()) {
                youtubePlayerView.setVisibility(View.GONE);
                return;
            }

            // Check network status
            boolean isOnline = NetworkUtil.isNetworkAvailable(requireContext());

            if (!isOnline) {
                // Show indicator INSIDE the YouTube frame
                youtubeOfflineIndicator.setVisibility(View.VISIBLE);

                // Optional: Add a semi-transparent background to the YouTube player
                youtubePlayerView.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.youtube_offline_bg)
                );

                // Release player to save resources
                youtubePlayerView.release();
                return;
            }

            // Extract video ID (only if online)
            String videoId = extractYouTubeId(videoUrl);
            if (videoId == null) {
                youtubePlayerView.setVisibility(View.GONE);
                return;
            }

            // Initialize YouTube player
            getLifecycle().addObserver(youtubePlayerView);
            youtubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    youTubePlayer.cueVideo(videoId, 0);
                }
            });
        });
    }

    @Override
    public void showFavoriteStatus(boolean isFavorite) {
        requireActivity().runOnUiThread(() -> updateFavoriteButton(isFavorite));
    }

    @Override
    public void showError(String message) {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show());
    }

    private void updateFavoriteButton(boolean isFavorite) {
        int iconRes = isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border;
        btnFavorite.setImageResource(iconRes);
    }

    private String extractYouTubeId(String url) {
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\\?video_id=)([^#\\&\\?\\n]*)";
        Matcher matcher = Pattern.compile(pattern).matcher(url);
        return matcher.find() ? matcher.group() : null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (youtubePlayerView != null) {
            youtubePlayerView.release();
        }
    }

    @Override
    public void showPlannerSuccess(String date) {
        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(),
                getString(R.string.meal_added_to_planner, date),
                Toast.LENGTH_SHORT).show());
    }

    @Override
    public void showCalendarSuccess() {
        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(),
                R.string.added_to_device_calendar,
                Toast.LENGTH_SHORT).show());
    }



    @Override
    public void requestCalendarPermission(int requestCode) {
        // Simple permission request without rationale dialog
        requestPermissions(
                new String[]{Manifest.permission.WRITE_CALENDAR},
                requestCode
        );
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALENDAR_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showDatePickerForNextWeek();
            } else {
                showError("Calendar permission denied");
            }
        }
    }

    private void showDatePickerForNextWeek() {
        // First check if we already have permission
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            // We have permission, show the date picker
            Calendar today = Calendar.getInstance();
            Calendar maxDate = Calendar.getInstance();
            maxDate.add(Calendar.DAY_OF_YEAR, 6);
            showDatePicker(today, maxDate);
        } else {
            // Request permission directly
            requestCalendarPermission(REQUEST_CALENDAR_PERMISSION);
        }
    }

    @Override
    public void showDatePicker(Calendar minDate, Calendar maxDate) {
        if (!isAdded() || getActivity() == null) return;

        requireActivity().runOnUiThread(() -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, day) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, day);
                        if (presenter != null) {
                            presenter.handleDateSelected(selectedDate);
                        }
                    },
                    minDate.get(Calendar.YEAR),
                    minDate.get(Calendar.MONTH),
                    minDate.get(Calendar.DAY_OF_MONTH));

            datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
            datePickerDialog.show();
        });
    }

}
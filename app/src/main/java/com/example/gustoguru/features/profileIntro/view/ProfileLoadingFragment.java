package com.example.gustoguru.features.profileIntro.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.example.gustoguru.R;
import com.example.gustoguru.features.main.view.MainActivity;
import com.example.gustoguru.features.navigation.view.NavigationCommunicator;
import com.example.gustoguru.features.profile.view.ProfileFragment;
import com.example.gustoguru.features.profileIntro.presenter.ProfileLoadingPresenter;
import com.example.gustoguru.model.sessionmanager.SessionManager;

import javax.annotation.Nullable;
public class ProfileLoadingFragment extends Fragment implements ProfileLoadingView {


    private ProfileLoadingPresenter presenter;
    private LottieAnimationView animationView;
    private boolean hasNavigated = false;
    private boolean shouldSkipAnimation = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if we're coming from back navigation
        if (getArguments() != null) {
            shouldSkipAnimation = getArguments().getBoolean("skip_animation", false);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_intro, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (shouldSkipAnimation) {
            navigateToProfile();
            return;
        }

        animationView = view.findViewById(R.id.profile_loading_animation);
        try {
            animationView.setAnimation("profile.json");
            animationView.setRepeatCount(0);
            animationView.setSpeed(1f);
            animationView.loop(false);

            presenter = new ProfileLoadingPresenter(this);

            animationView.addAnimatorListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!hasNavigated && presenter != null) {
                        presenter.onAnimationComplete();
                    }
                }
            });

            animationView.playAnimation();
        } catch (Exception e) {
            showError("Animation setup failed: " + e.getMessage());
            navigateToProfile();
        }
    }


    @Override
    public void navigateToProfile() {
        if (hasNavigated || !isAdded() || isDetached() || getActivity() == null) {
            return;
        }
        hasNavigated = true;

        // Ensure this runs on the main thread and when the activity is ready
        new Handler(Looper.getMainLooper()).post(() -> {
            if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                ((MainActivity) getActivity()).replaceFragment(new ProfileFragment(), false);
            }
        });
    }

    @Override
    public LottieAnimationView getAnimationView() {
        return animationView;
    }



    @Override
    public void showError(String message) {
        Log.e("ProfileLoading", message);
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onDestroyView() {
        if (animationView != null) {
            animationView.cancelAnimation();
            animationView.removeAllAnimatorListeners();
        }
        if (presenter != null) {
            presenter.onDestroy();
            presenter.detachView();
        }
        super.onDestroyView();
    }

}
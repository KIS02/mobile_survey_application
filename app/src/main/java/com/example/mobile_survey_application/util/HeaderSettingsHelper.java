package com.example.mobile_survey_application.util;

import android.view.View;

import androidx.fragment.app.Fragment;

import com.example.mobile_survey_application.HomeActivity;
import com.example.mobile_survey_application.R;

public final class HeaderSettingsHelper {

    private HeaderSettingsHelper() {
    }

    public static void bindNavigateToSetting(View rootView, Fragment fragment) {
        View headerSettings = rootView.findViewById(R.id.headerSettings);
        if (headerSettings == null) {
            return;
        }

        headerSettings.setOnClickListener(v -> {
            if (fragment.requireActivity() instanceof HomeActivity) {
                ((HomeActivity) fragment.requireActivity()).navigateToSetting();
            }
        });
    }
}

package com.example.mobile_survey_application.util;

import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public final class EdgeToEdgeHelper {

    private EdgeToEdgeHelper() {
    }

    public static void enableWithRootInsets(ComponentActivity activity, int rootViewId) {
        EdgeToEdge.enable(activity);
        applyRootInsets(activity, rootViewId);
    }

    public static void applyRootInsets(ComponentActivity activity, int rootViewId) {
        View rootLayout = activity.findViewById(rootViewId);
        if (rootLayout == null) {
            return;
        }

        final int initialPaddingLeft = rootLayout.getPaddingLeft();
        final int initialPaddingTop = rootLayout.getPaddingTop();
        final int initialPaddingRight = rootLayout.getPaddingRight();
        final int initialPaddingBottom = rootLayout.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    initialPaddingLeft + systemBars.left,
                    initialPaddingTop + systemBars.top,
                    initialPaddingRight + systemBars.right,
                    initialPaddingBottom + systemBars.bottom
            );
            return insets;
        });
    }
}

package com.example.mobile_survey_application.util;

import android.content.Context;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.mobile_survey_application.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import data.local.TokenManager;
import data.network.RetrofitClient;
import model.UserResponse;

public final class ProfileImageHelper {

    private ProfileImageHelper() {
    }

    public static String resolveProfileImageUrl(Context context, UserResponse user) {
        TokenManager tokenManager = new TokenManager(context);
        String cachedUrl = tokenManager.getGoogleProfilePictureUrl();
        if (hasValue(cachedUrl)) {
            return cachedUrl;
        }

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account != null && account.getPhotoUrl() != null) {
            String photoUrl = account.getPhotoUrl().toString();
            if (hasValue(photoUrl)) {
                return photoUrl;
            }
        }

        if (user != null && hasValue(user.getImagePath())) {
            String imagePath = user.getImagePath().trim();
            if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                return imagePath;
            }

            return RetrofitClient.getBaseUrl().replaceAll("/$", "") + imagePath;
        }

        return null;
    }

    public static void loadProfileImage(Fragment fragment, ImageView imageView, UserResponse user) {
        String imageUrl = resolveProfileImageUrl(fragment.requireContext(), user);

        imageView.setImageTintList(null);

        if (!hasValue(imageUrl)) {
            imageView.setImageResource(R.drawable.ic_profile_default);
            return;
        }

        Glide.with(fragment)
                .load(imageUrl)
                .placeholder(R.drawable.ic_profile_default)
                .error(R.drawable.ic_profile_default)
                .circleCrop()
                .into(imageView);
    }

    private static boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

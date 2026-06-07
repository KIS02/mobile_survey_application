package data.local;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.TimeUnit;

public class RandomSurveyCooldownManager {

    private static final String PREF_NAME = "random_survey_cooldown";
    private static final String KEY_AVAILABLE_AT_MILLIS = "available_at_millis";

    private final SharedPreferences preferences;

    public RandomSurveyCooldownManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public long startCooldown(long time, TimeUnit unit) {
        long availableAtMillis = System.currentTimeMillis() + unit.toMillis(time);

        preferences.edit()
                .putLong(KEY_AVAILABLE_AT_MILLIS, availableAtMillis)
                .apply();

        return availableAtMillis;
    }

    public long getAvailableAtMillis() {
        return preferences.getLong(KEY_AVAILABLE_AT_MILLIS, 0L);
    }

    public long getRemainingMillis() {
        long remainingMillis = getAvailableAtMillis() - System.currentTimeMillis();
        return Math.max(remainingMillis, 0L);
    }

    public boolean isCoolingDown() {
        return getRemainingMillis() > 0;
    }

    public void clearCooldown() {
        preferences.edit()
                .remove(KEY_AVAILABLE_AT_MILLIS)
                .apply();
    }
}
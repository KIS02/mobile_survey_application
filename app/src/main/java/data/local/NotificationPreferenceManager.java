package data.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.work.WorkManager;

public class NotificationPreferenceManager {

    public static final String RANDOM_SURVEY_REMINDER_WORK_NAME = "random_survey_reminder_work";

    private static final String PREF_NAME = "app_prefs";
    private static final String KEY_NOTIFICATION_ENABLED = "notification_enabled";

    private final SharedPreferences prefs;
    private final Context appContext;

    public NotificationPreferenceManager(Context context) {
        appContext = context.getApplicationContext();
        prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isNotificationEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATION_ENABLED, true);
    }

    public void setNotificationEnabled(boolean enabled) {
        prefs.edit()
                .putBoolean(KEY_NOTIFICATION_ENABLED, enabled)
                .apply();

        if (!enabled) {
            cancelRandomSurveyReminder();
        }
    }

    public void cancelRandomSurveyReminder() {
        WorkManager.getInstance(appContext).cancelUniqueWork(RANDOM_SURVEY_REMINDER_WORK_NAME);
    }
}

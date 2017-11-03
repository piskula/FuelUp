package sk.momosi.fuelup.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

/**
 * @author Martin Styk
 */
public class PreferencesUtils {

    public static final String BACKUP_FRAGMENT_ACCOUNT_NAME = "accountName";
    public static final String BACKUP_FRAGMENT_ACCOUNT_IMPORT_ASKED = "accountImportAsked";
    public static final String BACKUP_LAST_SYNC = "sync_done_when";
    public static final String BACKUP_LAST_CHANGE = "sync_configured_when";

    @Nullable
    public static String getString(@NonNull Context context, @NonNull String key) {
        return getSharedPreferences(context).getString(key, null);
    }

    public static boolean getBoolean(@NonNull Context context, @NonNull String key) {
        return getSharedPreferences(context).getBoolean(key, false);
    }

    public static void setString(@NonNull Context context, @NonNull String key, @Nullable String value) {
        getSharedPreferences(context).edit()
                .putString(key, value)
                .apply();
    }

    public static void setBoolean(@NonNull Context context, @NonNull String key, boolean value) {
        getSharedPreferences(context).edit()
                .putBoolean(key, value)
                .apply();
    }

    public static void remove(@NonNull Context context, @NonNull String... keys) {
        SharedPreferences preferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        for (String key : keys)
            editor.remove(key);

        editor.apply();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setLastChange(Context context) {
        getSharedPreferences(context).edit().putLong(BACKUP_LAST_CHANGE, new Date().getTime()).apply();
    }

    public static void setLastSync(Context context) {
        getSharedPreferences(context).edit().putLong(BACKUP_LAST_SYNC, new Date().getTime()).apply();
    }

    public static long getLastChange(Context context) {
        return getSharedPreferences(context).getLong(BACKUP_LAST_CHANGE, 0L);
    }

    public static long getLastSync(Context context) {
        return getSharedPreferences(context).getLong(BACKUP_LAST_SYNC, 0L);
    }
}

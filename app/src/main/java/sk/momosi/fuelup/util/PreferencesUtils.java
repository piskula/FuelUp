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
    public static String getAccountName(@NonNull Context context) {
        return getSharedPreferences(context).getString(BACKUP_FRAGMENT_ACCOUNT_NAME, null);
    }

    public static boolean hasBeenImportDone(@NonNull Context context) {
        return getSharedPreferences(context).getBoolean(BACKUP_FRAGMENT_ACCOUNT_IMPORT_ASKED, false);
    }

    public static void setAccountName(@NonNull Context context, @Nullable String value) {
        getSharedPreferences(context).edit()
                .putString(BACKUP_FRAGMENT_ACCOUNT_NAME, value)
                .apply();
    }

    public static void setHasBeenImportDone(@NonNull Context context, boolean value) {
        getSharedPreferences(context).edit()
                .putBoolean(BACKUP_FRAGMENT_ACCOUNT_IMPORT_ASKED, value)
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

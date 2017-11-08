package sk.momosi.fuelup.business.googledrive.syncing;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import sk.momosi.fuelup.business.googledrive.authenticator.AccountService;
import sk.momosi.fuelup.data.FuelUpContract;
import sk.momosi.fuelup.util.PreferencesUtils;

/**
 * @author Ondrej Oravcok
 * @version 19.10.2017
 */
public class SyncAdapterContentObserver extends ContentObserver {

    private static final String LOG_TAG = SyncAdapterContentObserver.class.getSimpleName();
    private final Context context;

    public SyncAdapterContentObserver(Handler handler, Context context) {
        super(handler);
        this.context = context;
    }

    @Override
    public void onChange(boolean selfChange) {
        this.onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(
                AccountService.getAccount(),
                FuelUpContract.CONTENT_AUTHORITY,
                bundle);
        PreferencesUtils.setLastChange(context);
        Log.i(LOG_TAG, "sync adapter has been notified");
    }
}

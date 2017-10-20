package sk.momosi.fuelup.business.googledrive.syncing;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import sk.momosi.fuelup.business.googledrive.authenticator.AccountService;
import sk.momosi.fuelup.data.FuelUpContract;

/**
 * @author Ondrej Oravcok
 * @version 19.10.2017
 */
public class SyncAdapterContentObserver extends ContentObserver {

    private static final String LOG_TAG = SyncAdapterContentObserver.class.getSimpleName();

    public SyncAdapterContentObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange) {
        this.onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        ContentResolver.requestSync(
                AccountService.getAccount(),
                FuelUpContract.CONTENT_AUTHORITY,
                new Bundle());
        Log.i(LOG_TAG, "sync adapter has been notified");
    }
}

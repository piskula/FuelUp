package sk.momosi.fuelup.business.googledrive.syncing;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import sk.momosi.fuelup.business.googledrive.authenticator.AccountService;
import sk.momosi.fuelup.data.FuelUpContract;

/**
 * Created by Martin Styk on 18.10.2017.
 */

public class DriveSyncingUtils {


    public static void setUpPeriodicSync(Context context) {
        Account account = AccountService.getAccount();
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        if (accountManager.addAccountExplicitly(account, null, null)) {
            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(account, FuelUpContract.CONTENT_AUTHORITY, 1);
            // Inform the system that this account is eligible for auto sync when the network is up
            ContentResolver.setSyncAutomatically(account, FuelUpContract.CONTENT_AUTHORITY, true);
            // Recommend a schedule for automatic synchronization. The system may modify this based
            // on other scheduled syncs and network utilization.
            ContentResolver.addPeriodicSync(account, FuelUpContract.CONTENT_AUTHORITY, new Bundle(), 60);

            Toast.makeText(context, "Syncing is enabled", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(context, "Syncing disabled", Toast.LENGTH_SHORT).show();
        }

    }

    public static void requestImmediateSync(Context context) {
        Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        Account account = AccountService.getAccount();
        ContentResolver.requestSync(account, FuelUpContract.CONTENT_AUTHORITY, b);

        Toast.makeText(context, "Syncing started", Toast.LENGTH_SHORT).show();
    }
}

package sk.momosi.fuelup.business.googledrive.syncing;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

import sk.momosi.fuelup.business.googledrive.authenticator.AccountService;
import sk.momosi.fuelup.data.FuelUpContract;
import sk.momosi.fuelup.util.PreferencesUtils;

/**
 * @author Martin Styk
 * @version 18.10.2017
 */
public class DriveSyncingUtils {
    private static final String LOG_TAG = DriveSyncingUtils.class.getSimpleName();

    public static void enableSyncGlobally(Context context) {
        Account genericAccount = AccountService.getAccount();

        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        List<Account> accounts = Arrays.asList(accountManager.getAccountsByType(AccountService.ACCOUNT_TYPE));

        if(accounts.isEmpty()) {
            Log.e(LOG_TAG, "Generic sync account must be added but it should have been already done when first running application.");
            accountManager.addAccountExplicitly(genericAccount, null, null);
        }

        ContentResolver.setIsSyncable(genericAccount, FuelUpContract.CONTENT_AUTHORITY, 1);
    }

    public static void disableSync(Context context) {
        PreferencesUtils.remove(context, PreferencesUtils.BACKUP_FRAGMENT_ACCOUNT_IMPORT_ASKED,
                PreferencesUtils.BACKUP_FRAGMENT_ACCOUNT_NAME);

        Account genericAccount = AccountService.getAccount();
        ContentResolver.setIsSyncable(genericAccount, FuelUpContract.CONTENT_AUTHORITY, 0);
        Log.i(LOG_TAG, "Syncing is now disabled.");
    }

    public static boolean isSyncable() {
        return ContentResolver.getIsSyncable(AccountService.getAccount(), FuelUpContract.CONTENT_AUTHORITY) > 0;
    }

    public static boolean isSyncPending() {
        return ContentResolver.isSyncPending(AccountService.getAccount(), FuelUpContract.CONTENT_AUTHORITY);
    }

    public static void requestImmediateSync() {
        Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        Account account = AccountService.getAccount();
        ContentResolver.requestSync(account, FuelUpContract.CONTENT_AUTHORITY, b);
    }
}

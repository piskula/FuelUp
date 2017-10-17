package sk.momosi.fuelup.business.googledrive.syncing;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import sk.momosi.fuelup.business.VehicleService;
import sk.momosi.fuelup.business.googledrive.DriveBackupFileUtil;
import sk.momosi.fuelup.business.googledrive.JsonUtil;
import sk.momosi.fuelup.util.PreferencesUtils;

/**
 * @author Ondrej Oravcok
 * @version 17.10.2017
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOG_TAG = SyncAdapter.class.getSimpleName();

    private GoogleAccountCredential mCredential;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mCredential = DriveBackupFileUtil.generateCredential(context);
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mCredential = DriveBackupFileUtil.generateCredential(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Log.e(LOG_TAG, "SYNC STARTED");

        List<Long> vehicleIds = VehicleService.getAvailableVehicleIds(getContext());
        String json = JsonUtil.getWholeDbAsJson(vehicleIds, getContext());
        if (json == null) {
            Log.e(LOG_TAG, "Error transfering DB to JSON.");
            syncResult.stats.numParseExceptions++;
            return;
        }

        // TODO temporary file
        String path = getContext().getExternalFilesDir(null) + "/" + DriveBackupFileUtil.BACKUP_DB_FILE_NAME;
        try {
            FileOutputStream os = new FileOutputStream(path);
            os.write(json.getBytes());
            os.flush();
            os.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Cannot create temp file.", e);
            syncResult.stats.numIoExceptions++;
            return;
        }

        java.io.File file = new java.io.File(path);

        File fileMetadata = new File();
        fileMetadata.setName(DriveBackupFileUtil.BACKUP_DB_FILE_NAME);
        fileMetadata.setParents(Collections.singletonList(DriveBackupFileUtil.BACKUP_DB_FOLDER));

        if (!file.exists()) {
            Log.e(LOG_TAG, "Failed to prepare backup file (before upload to Google Drive).");
            syncResult.stats.numIoExceptions++;
            return;
        }

        String accountName = PreferencesUtils.getString(getContext(), PreferencesUtils.BACKUP_FRAGMENT_ACCOUNT_NAME);
        if (accountName == null) {
            Log.e(LOG_TAG, "No account set for Google Drive.");
            syncResult.stats.numAuthExceptions++;
            return;
        }

        mCredential.setSelectedAccountName(accountName);
        Drive mService = DriveBackupFileUtil.getServiceForCredentials(mCredential);
        FileContent mediaContent = new FileContent("application/json", file);

        try {
            String backupFileId = DriveBackupFileUtil.getBackupFileId(mService);

            if (backupFileId == null) {
                mService.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();
            } else {
                mService.files().update(backupFileId, null, mediaContent)
                        .execute();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "", e);
            syncResult.stats.numIoExceptions++;
        }

        Log.i(LOG_TAG, "Syncing DB ended successfully.");
    }
}

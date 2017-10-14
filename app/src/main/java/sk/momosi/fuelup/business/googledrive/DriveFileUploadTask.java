package sk.momosi.fuelup.business.googledrive;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import sk.momosi.fuelup.business.VehicleService;


public class DriveFileUploadTask extends GoogleDriveAbstractAsyncTask<Void, Void, Boolean> {

    private WeakReference<Callback> callbackWeakReference;
    private WeakReference<Context> contextWeakReference;

    public DriveFileUploadTask(GoogleAccountCredential credential, @NonNull Callback callback, @NonNull Context context) {
        super(credential);
        this.callbackWeakReference = new WeakReference<>(callback);
        this.contextWeakReference = new WeakReference<>(context);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            return uploadFile();
        } catch (Exception e) {
            mLastError = e;
            cancel(true);
            return null;
        }
    }

    private Boolean uploadFile() throws IOException {

        Context context = contextWeakReference.get();
        // if activity is finished, abort action
        if (context == null)
            return false;

        List<Long> vehicleIds = VehicleService.getAvailableVehicleIds(context);
        String json = JsonUtil.getWholeDbAsJson(vehicleIds, context);
        if (json == null) {
            throw new IOException("Cannot backup database because of Error while parsing data.");
        }

        // TODO temporary file
        String path = context.getExternalFilesDir(null) + "/" + DriveBackupFileUtil.BACKUP_DB_FILE_NAME;
        FileOutputStream os = new FileOutputStream(path);
        os.write(json.getBytes());
        os.flush();
        os.close();

        java.io.File file = new java.io.File(path);

        File fileMetadata = new File();
        fileMetadata.setName(DriveBackupFileUtil.BACKUP_DB_FILE_NAME);
        fileMetadata.setParents(Collections.singletonList(DriveBackupFileUtil.BACKUP_DB_FOLDER));

        if (!file.exists()) {
            throw new IOException("Failed to prepare backup file (before upload to Google Drive).");
        }

        FileContent mediaContent = new FileContent("application/json", file);
        String backupFileId = DriveBackupFileUtil.getBackupFileId(mService);

        if (backupFileId == null) {
            mService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
        } else {
            mService.files().update(backupFileId, null, mediaContent)
                    .execute();
        }
        return true;
    }

    @Override
    protected void onPreExecute() {
        Callback callback = callbackWeakReference.get();
        if (callback != null)
            callback.onDriveFileUploadTaskPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean output) {
        Callback callback = callbackWeakReference.get();
        if (callback != null)
            callback.onDriveFileUploadTaskPostExecute(output);
    }

    @Override
    protected void onCancelled() {
        Callback callback = callbackWeakReference.get();
        if (callback != null)
            callback.onAsyncTaskCancel(mLastError);
    }

    public interface Callback {
        void onDriveFileUploadTaskPreExecute();

        void onDriveFileUploadTaskPostExecute(Boolean output);

        void onAsyncTaskCancel(Exception mLastError);
    }
}
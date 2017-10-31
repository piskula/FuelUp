package sk.momosi.fuelup.business.googledrive;

import android.support.annotation.NonNull;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * An asynchronous task that handles the Drive API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 *
 * @author Martin Styk
 * @author Ondrej Oravčok
 */
// TODO remove before release
public class DriveRequestTask extends GoogleDriveAbstractAsyncTask<Void, Void, String> {

    private WeakReference<Callback> callbackReference;

    public DriveRequestTask(@NonNull GoogleAccountCredential credential, @NonNull Callback callback) {
        super(credential);
        this.callbackReference = new WeakReference<>(callback);
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            return getDataFromApi();
        } catch (Exception e) {
            mLastError = e;
            cancel(true);
            return null;
        }
    }

    private String getDataFromApi() throws IOException {
        // Get a list of up to 10 files.

        String backupFileId = DriveBackupFileUtil.getBackupFolderId(mService);
        if (backupFileId == null) {
            throw new IOException("Cannot find your previous Backup.");
        }

        InputStream is = mService.files().get(backupFileId).executeMediaAsInputStream();
        String result = JsonUtil.getWholeJsonInputStreamAsString(is);
        return result.length() > 1000 ? result.substring(0, 1000) + "\n..." : result;
    }

    @Override
    protected void onPreExecute() {
        Callback callback = callbackReference.get();
        if (callback != null)
            callback.onDriveRequestTaskPreExecute();
    }

    @Override
    protected void onPostExecute(String output) {
        Callback callback = callbackReference.get();
        if (callback != null)
            callback.onDriveRequestTaskPostExecute(output);
    }

    @Override
    protected void onCancelled() {
        Callback callback = callbackReference.get();
        if (callback != null)
            callback.onAsyncTaskCancel(mLastError);
    }

    public interface Callback {
        void onDriveRequestTaskPreExecute();

        void onDriveRequestTaskPostExecute(String output);

        void onAsyncTaskCancel(Exception mLastError);
    }
}
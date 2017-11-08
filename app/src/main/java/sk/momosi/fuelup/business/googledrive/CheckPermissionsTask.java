package sk.momosi.fuelup.business.googledrive;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class CheckPermissionsTask extends GoogleDriveAbstractAsyncTask<Void, Void, Boolean> {

    private final WeakReference<Callback> callbackReference;

    public CheckPermissionsTask(GoogleAccountCredential credential, Callback callback) {
        super(credential);
        this.callbackReference = new WeakReference<>(callback);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            return hasPermissions();
        } catch (Exception e) {
            mLastError = e;
            cancel(true);
            return null;
        }
    }

    private Boolean hasPermissions() throws IOException {
        // try to get folderId to check permissions
        String folderId = DriveBackupFileUtil.getBackupFolderId(mService);
        return true;
    }

    @Override
    protected void onPreExecute() {
        Callback callback = callbackReference.get();
        if (callback != null)
            callback.onCheckPermissionsTaskPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean output) {
        Callback callback = callbackReference.get();
        if (callback != null)
            callback.onCheckPermissionsTaskPostExecute(output);
    }

    @Override
    protected void onCancelled() {
        Callback callback = callbackReference.get();
        if (callback != null)
            callback.onAsyncTaskCancel(mLastError);
    }

    public interface Callback {
        void onCheckPermissionsTaskPreExecute();

        void onCheckPermissionsTaskPostExecute(Boolean output);

        void onAsyncTaskCancel(Exception mLastError);
    }
}

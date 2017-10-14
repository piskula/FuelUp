package sk.momosi.fuelup.business.googledrive;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class CheckPreviousAppInstalledTask extends GoogleDriveAbstractAsyncTask<Void, Void, JSONObject> {

    private WeakReference<Callback> callbackReference;

    public CheckPreviousAppInstalledTask(GoogleAccountCredential credential, Callback callback) {
        super(credential);
        this.callbackReference = new WeakReference<>(callback);
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        try {
            return checkIfPreviousVersionsHaveBeenUsed();
        } catch (Exception e) {
            mLastError = e;
            cancel(true);
            return null;
        }
    }

    private JSONObject checkIfPreviousVersionsHaveBeenUsed() throws IOException {

        String backupFileId = DriveBackupFileUtil.getBackupFileId(mService);
        if (backupFileId == null) {
            return null;
        }

        InputStream is = mService.files().get(backupFileId).executeMediaAsInputStream();
        try {
            return new JSONObject(JsonUtil.getWholeJsonInputStreamAsString(is));
        } catch (JSONException e) {
            throw new IOException("Cannot parse JSON.", e);
        }
    }

    @Override
    protected void onPreExecute() {
        Callback callback = callbackReference.get();
        if (callback != null)
            callback.onCheckPreviousAppInstalledTaskPreExecute();
    }

    @Override
    protected void onPostExecute(JSONObject output) {
        Callback callback = callbackReference.get();
        if (callback != null)
            callback.onCheckPreviousAppInstalledTaskPostExecute(output);
    }

    @Override
    protected void onCancelled() {
        Callback callback = callbackReference.get();
        if (callback != null)
            callback.onAsyncTaskCancel(mLastError);
    }

    public interface Callback {
        void onCheckPreviousAppInstalledTaskPreExecute();

        void onCheckPreviousAppInstalledTaskPostExecute(JSONObject json);

        void onAsyncTaskCancel(Exception mLastError);
    }
}
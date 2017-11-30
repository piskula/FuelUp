package sk.momosi.fuelup.business.googledrive;

import android.content.Context;
import android.support.v4.util.Pair;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;

import sk.momosi.fuelup.business.VehicleService;

public class CheckPreviousAppInstalledTask extends GoogleDriveAbstractAsyncTask<Void, Void, Pair<JSONObject, Set<String>>> {

    private final WeakReference<Callback> callbackReference;
    private final Context context;

    public CheckPreviousAppInstalledTask(GoogleAccountCredential credential, Callback callback, Context context) {
        super(credential);
        this.callbackReference = new WeakReference<>(callback);
        this.context = context;
    }

    @Override
    protected Pair<JSONObject, Set<String>> doInBackground(Void... params) {
        try {
            return checkIfPreviousVersionsHaveBeenUsed();
        } catch (Exception e) {
//            throw new RuntimeException(e);
            mLastError = e;
            cancel(true);
            return new Pair<>(null, null);
        }
    }

    private Pair<JSONObject, Set<String>> checkIfPreviousVersionsHaveBeenUsed() throws IOException {

        String backupFolderId = DriveBackupFileUtil.getBackupFolderId(mService);
        String backupFileId = DriveBackupFileUtil.getBackupFileId(mService, backupFolderId);
        if (backupFileId == null) {
            return new Pair<>(null, null);
        }

        InputStream is = mService.files().get(backupFileId).executeMediaAsInputStream();
        JSONObject jsonResult;
        try {
            jsonResult = new JSONObject(JsonUtil.getWholeJsonInputStreamAsString(is));
        } catch (JSONException e) {
            throw new IOException("Cannot parse JSON.", e);
        }

        Set<String> vehicleNamesInDb = VehicleService.getAvailableVehicleNames(context);
        return new Pair<>(jsonResult, vehicleNamesInDb);
    }

    @Override
    protected void onPreExecute() {
        Callback callback = callbackReference.get();
        if (callback != null)
            callback.onCheckPreviousAppInstalledTaskPreExecute();
    }

    @Override
    protected void onPostExecute(Pair<JSONObject, Set<String>> output) {
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

        void onCheckPreviousAppInstalledTaskPostExecute(Pair<JSONObject, Set<String>> json);

        void onAsyncTaskCancel(Exception mLastError);
    }
}
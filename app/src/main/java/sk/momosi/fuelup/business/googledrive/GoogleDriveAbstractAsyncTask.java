package sk.momosi.fuelup.business.googledrive;

import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

/**
 * @author Ondrej Oravcok
 * @version 12.10.2017.
 */
public abstract class GoogleDriveAbstractAsyncTask<Params, Progress, Object> extends AsyncTask<Void, Void, Object> {
    protected com.google.api.services.drive.Drive mService = null;
    protected Exception mLastError = null;

    protected GoogleDriveAbstractAsyncTask(GoogleAccountCredential credential) {
        mService = DriveBackupFileUtil.getServiceForCredentials(credential);
    }


}

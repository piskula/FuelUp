package sk.momosi.fuelup.business.googledrive;

import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

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

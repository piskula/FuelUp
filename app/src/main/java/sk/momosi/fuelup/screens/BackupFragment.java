package sk.momosi.fuelup.screens;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import pub.devrel.easypermissions.EasyPermissions;
import sk.momosi.fuelup.R;
import sk.momosi.fuelup.business.VehicleService;
import sk.momosi.fuelup.business.googledrive.GoogleDriveAbstractAsyncTask;
import sk.momosi.fuelup.business.googledrive.JsonUtil;
import sk.momosi.fuelup.screens.dialog.RestoreVehicleDialog;
import sk.momosi.fuelup.util.PreferencesUtils;

import static android.app.Activity.RESULT_OK;

/**
 * @author Ondro
 * @version 10.10.2017
 */
public class BackupFragment extends Fragment implements EasyPermissions.PermissionCallbacks,
        RestoreVehicleDialog.Callback {

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String LOG_TAG = BackupFragment.class.getSimpleName();
    private static final String BACKUP_DB_FILE_NAME = "fuelup_backup.json";
    private static final String BACKUP_DB_FOLDER = "appDataFolder";
    private static final String[] SCOPES = {DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_FILE};
    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;
    private TextView mOutputText;
    private TextView mAccountName;
    private TextView mSyncStatus;
    private Button uploadBtn;
    private Button downloadBtn;
    private Button removeBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootview = inflater.inflate(R.layout.fragment_backup, container, false);
        initializeViews(rootview);

        mCredential = GoogleAccountCredential.usingOAuth2(
                getContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        getActivity().findViewById(R.id.fab_add_vehicle).setVisibility(View.GONE);
        return rootview;
    }

    @Override
    public void onStart() {
        super.onStart();

        String accountName = PreferencesUtils.getString(getContext(), PreferencesUtils.BACKUP_FRAGMENT_ACCOUNT_NAME);

        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();

        } else if (!isDeviceOnline()) {
            mSyncStatus.setText(R.string.googledrive_no_connection);

        } else if (accountName != null) {
            mCredential.setSelectedAccountName(accountName);
            removeBtn.setEnabled(true);
            mAccountName.setText(accountName);
            mSyncStatus.setText(R.string.googledrive_cannot_connect);
            checkPermissions();

        } else {
            removeBtn.setEnabled(false);
            mAccountName.setText(R.string.googledrive_none);
            mSyncStatus.setText(R.string.googledrive_not_configured);
            chooseAccount();
        }
    }

    private void initializeViews(View rootview) {
        mAccountName = rootview.findViewById(R.id.txt_sync_account);
        mProgress = new ProgressDialog(getContext());
        downloadBtn = rootview.findViewById(R.id.btn_sync);
        uploadBtn = rootview.findViewById(R.id.btn_sync_upload_file);
        mSyncStatus = rootview.findViewById(R.id.sync_status);
        removeBtn = rootview.findViewById(R.id.btn_sync_remove_account);
        mOutputText = rootview.findViewById(R.id.txt_sync);

        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFilesFromDrive();
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFileThroughApi();
            }
        });

        removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAccount();
            }
        });

        mProgress.setMessage("Calling Drive API ...");

        mOutputText.setVerticalScrollBarEnabled(true);
        mOutputText.setMovementMethod(new ScrollingMovementMethod());
    }

    private void removeAccount() {
        PreferencesUtils.remove(getContext(), PreferencesUtils.BACKUP_FRAGMENT_ACCOUNT_IMPORT_ASKED,
                PreferencesUtils.BACKUP_FRAGMENT_ACCOUNT_NAME);

        mCredential.setSelectedAccountName(null);
        removeBtn.setEnabled(false);
        mAccountName.setText(R.string.googledrive_none);
        mSyncStatus.setText(R.string.googledrive_not_configured);

        chooseAccount();
    }

    private void checkPermissions() {
        new CheckPermissionsTask(mCredential).execute();
    }

    private void assignAccount(String googleDriveAccount) {
        PreferencesUtils.setString(getContext(),PreferencesUtils.BACKUP_FRAGMENT_ACCOUNT_NAME, googleDriveAccount);
        PreferencesUtils.remove(getContext(), PreferencesUtils.BACKUP_FRAGMENT_ACCOUNT_IMPORT_ASKED);

        mCredential.setSelectedAccountName(googleDriveAccount);
        removeBtn.setEnabled(true);
        checkPermissions();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().findViewById(R.id.fab_add_vehicle).setVisibility(View.VISIBLE);
    }


    private void getFilesFromDrive() {
        new MakeRequestTask(mCredential).execute();
    }

    private void uploadFileThroughApi() {
        new UploadFileTask(mCredential).execute();
    }

//    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                getContext(), Manifest.permission.GET_ACCOUNTS)) {
            startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);

        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google accounts list.",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    public void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    mSyncStatus.setText(R.string.googledrive_requires_google_play);
                } else {
                    onStart();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        assignAccount(accountName);
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    onStart();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        Toast.makeText(getContext(), "onPermissionsGranted " + requestCode, Toast.LENGTH_SHORT).show();
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        Toast.makeText(getContext(), "onPermissionsDenied " + requestCode, Toast.LENGTH_SHORT).show();
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(getContext());
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(getContext());
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                getActivity(),
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @Override
    public void onDialogPositiveClick(Set<String> vehicleNames) {
        Toast.makeText(getContext(), "You have choosed: " + vehicleNames.size() + " vehicles to import. Now it is time to import and save preference.", Toast.LENGTH_LONG).show();

        PreferencesUtils.setBoolean(getContext(), PreferencesUtils.BACKUP_FRAGMENT_ACCOUNT_IMPORT_ASKED, true);
    }

    private void importSpecifiedVehiclesFromJson(JSONObject json) {

        ArrayList<String> vehicles;
        try {
            vehicles = JsonUtil.getVehicleNamesFromJson(json);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Json format exception occured.");
            vehicles = new ArrayList<>();
        }

        RestoreVehicleDialog.newInstance(vehicles, this).show(getFragmentManager(), RestoreVehicleDialog.class.getSimpleName());
    }

    private String getBackupFileId(com.google.api.services.drive.Drive mService) throws IOException {
        FileList result = mService.files().list()
                .setSpaces(BACKUP_DB_FOLDER)
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name, modifiedTime)")
                .execute();

        if (result == null || result.getFiles().isEmpty()) {
            return null;
        }

        List<File> files = result.getFiles();
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return Long.valueOf(f2.getModifiedTime().getValue()).compareTo(f1.getModifiedTime().getValue());
            }
        });
        return files.get(0).getId();
    }

    private void onAsyncTaskCancel(Exception mLastError) {
        mProgress.hide();
        if (mLastError != null) {
            if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                showGooglePlayServicesAvailabilityErrorDialog(
                        ((GooglePlayServicesAvailabilityIOException) mLastError)
                                .getConnectionStatusCode());
            } else if (mLastError instanceof UserRecoverableAuthIOException) {
                startActivityForResult(
                        ((UserRecoverableAuthIOException) mLastError).getIntent(),
                        REQUEST_AUTHORIZATION);
            } else {
                mOutputText.setText("The following error occurred:\n"
                        + mLastError.getMessage());
            }
        } else {
            mOutputText.setText("Request cancelled.");
        }
    }

    private class UploadFileTask extends GoogleDriveAbstractAsyncTask<Void, Void, Boolean> {
        UploadFileTask(GoogleAccountCredential credential) {
            super(credential);
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

            List<Long> vehicleIds = VehicleService.getAvailableVehicleIds(getContext());
            String json = JsonUtil.getWholeDbAsJson(vehicleIds, getContext());
            if (json == null) {
                throw new IOException("Cannot backup database because of Error while parsing data.");
            }

//           java.io.File file = java.io.File.createTempFile("config", "json");
            // TODO temporary file
            String path = getContext().getExternalFilesDir(null) + "/" + BACKUP_DB_FILE_NAME;
            FileOutputStream os = new FileOutputStream(path);
            os.write(json.getBytes());
            os.flush();
            os.close();

            java.io.File file = new java.io.File(path);

            File fileMetadata = new File();
            fileMetadata.setName(BACKUP_DB_FILE_NAME);
            fileMetadata.setParents(Collections.singletonList(BACKUP_DB_FOLDER));

            if (!file.exists()) {
                throw new IOException("Failed to prepare backup file (before upload to Google Drive).");
            }

            FileContent mediaContent = new FileContent("application/json", file);
            String backupFileId = getBackupFileId(mService);

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
            mOutputText.setText("Upload in progress ...");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(Boolean output) {
            mProgress.hide();
            if (output) {
                mOutputText.setText("Uploaded successfully.");
            } else {
                mOutputText.setText("Upload failed.");
            }
        }

        @Override
        protected void onCancelled() {
            onAsyncTaskCancel(mLastError);
        }
    }

    private class CheckPreviousVersionsOfAppInstalledTask extends GoogleDriveAbstractAsyncTask<Void, Void, JSONObject> {
        CheckPreviousVersionsOfAppInstalledTask(GoogleAccountCredential credential) {
            super(credential);
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

            String backupFileId = getBackupFileId(mService);
            if (backupFileId == null) {
                return null;
            }

            InputStream is = mService.files().get(backupFileId).executeMediaAsInputStream();
            try {
                return new JSONObject(JsonUtil.getWholeJsonInputStreamAsString(is));
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Cannot parse JSON for '" + JsonUtil.JSON_DEVICE_APP_INSTANCE + "' InstanceID.", e);
                throw new IOException("Cannot parse JSON.", e);
            }
        }

        @Override
        protected void onPreExecute() {
            mOutputText.setText("Retrieving Google Drive last used InstanceID ...");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            mProgress.hide();
            if (json == null) {
                mOutputText.setText("There is no backed-up previous version on Google Drive to restore. Your account is now syncing.");
                PreferencesUtils.setBoolean(getContext(), PreferencesUtils.BACKUP_FRAGMENT_ACCOUNT_IMPORT_ASKED, true);
            }
            importSpecifiedVehiclesFromJson(json);
        }

        @Override
        protected void onCancelled() {
            onAsyncTaskCancel(mLastError);
        }
    }

    private class CheckPermissionsTask extends GoogleDriveAbstractAsyncTask<Void, Void, Boolean> {
        CheckPermissionsTask(GoogleAccountCredential credential) {
            super(credential);
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
            // try to get file to check permissions
            List<String> fileInfo = new ArrayList<String>();
            FileList result = mService.files().list()
                    .setSpaces(BACKUP_DB_FOLDER)
                    .setPageSize(1)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            return result.getFiles() != null;
        }

        @Override
        protected void onPreExecute() {
            mOutputText.setText("Checking permissions ...");
            uploadBtn.setEnabled(false);
            downloadBtn.setEnabled(false);
            mProgress.show();
        }

        @Override
        protected void onPostExecute(Boolean output) {
            mProgress.hide();
            if (output) {
                mOutputText.setText("Permissions OK.");
                uploadBtn.setEnabled(true);
                downloadBtn.setEnabled(true);
                mSyncStatus.setText("");

                if (!PreferencesUtils.getBoolean(getContext(), PreferencesUtils.BACKUP_FRAGMENT_ACCOUNT_IMPORT_ASKED)) {
                    new CheckPreviousVersionsOfAppInstalledTask(mCredential).execute();
                }
            } else {
                mOutputText.setText("Permissions not granted.");
            }
        }

        @Override
        protected void onCancelled() {
            onAsyncTaskCancel(mLastError);
        }
    }

    /**
     * An asynchronous task that handles the Drive API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends GoogleDriveAbstractAsyncTask<Void, Void, String> {

        MakeRequestTask(GoogleAccountCredential credential) {
            super(credential);
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

            String backupFileId = getBackupFileId(mService);
            if (backupFileId == null) {
                throw new IOException("Cannot find your previous Backup.");
            }

            InputStream is = mService.files().get(backupFileId).executeMediaAsInputStream();
            String result = JsonUtil.getWholeJsonInputStreamAsString(is);
            return result.length() > 1000 ? result.substring(0, 1000) + "\n..." : result;
        }


        @Override
        protected void onPreExecute() {
            mOutputText.setText("Retrieving data ...");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(String output) {
            mProgress.hide();
            if (output == null) {
                mOutputText.setText("Error occured.");
            } else {
                mOutputText.setText("Data:\n" + output);
            }
        }

        @Override
        protected void onCancelled() {
            onAsyncTaskCancel(mLastError);
        }
    }
}

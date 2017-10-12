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
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import sk.momosi.fuelup.R;
import sk.momosi.fuelup.business.googledrive.GoogleDriveAbstractAsyncTask;

import static android.app.Activity.RESULT_OK;

/**
 * @author Ondro
 * @version 10.10.2017
 */
public class BackupFragment extends Fragment implements EasyPermissions.PermissionCallbacks {

    GoogleAccountCredential mCredential;
    private TextView mOutputText;
    private TextView mAccountName;
    private TextView mSyncStatus;
    private Button uploadBtn;
    private Button downloadBtn;
    private Button removeBtn;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_FILE };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootview = inflater.inflate(R.layout.fragment_backup, container, false);
        initializeViews(rootview);

        mCredential = GoogleAccountCredential.usingOAuth2(
                getContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        String accountName = getActivity().getPreferences(Context.MODE_PRIVATE)
                .getString(PREF_ACCOUNT_NAME, null);

        if (accountName != null) {
            mCredential.setSelectedAccountName(accountName);
            removeBtn.setEnabled(true);
            mAccountName.setText(accountName);
            mSyncStatus.setText(R.string.googledrive_cannot_connect);
        } else {
            removeBtn.setEnabled(false);
            mAccountName.setText(R.string.googledrive_none);
            mSyncStatus.setText(R.string.googledrive_not_configured);
        }

        connectToDrive();
        getActivity().findViewById(R.id.fab_add_vehicle).setVisibility(View.GONE);
        return rootview;
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
                SharedPreferences settings =
                        getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.remove(PREF_ACCOUNT_NAME);
                editor.apply();
                mCredential.setSelectedAccountName(null);
                removeBtn.setEnabled(false);
                mAccountName.setText(R.string.googledrive_none);
                mSyncStatus.setText(R.string.googledrive_not_configured);
                connectToDrive();
            }
        });

        mProgress.setMessage("Calling Drive API ...");

        mOutputText.setVerticalScrollBarEnabled(true);
        mOutputText.setMovementMethod(new ScrollingMovementMethod());
    }

    private void connectToDrive() {
        uploadBtn.setEnabled(false);
        downloadBtn.setEnabled(false);
        mOutputText.setText("");

        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
//            Toast.makeText(getContext(), R.string.googledrive_no_connection, Toast.LENGTH_SHORT).show();
            mSyncStatus.setText(R.string.googledrive_no_connection);
        } else {
            // allright, Google Drive account successfully connected
            // TODO Snackbar (but must check View for null)
            Toast.makeText(getContext(), mCredential.getSelectedAccountName(), Toast.LENGTH_SHORT).show();
            mAccountName.setText(mCredential.getSelectedAccountName());
            new CheckPermissionsTask(mCredential).execute();
        }
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

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                getContext(), Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getActivity().getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                removeBtn.setEnabled(true);
                connectToDrive();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
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
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    public void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    mSyncStatus.setText(R.string.googledrive_requires_google_play);
                } else {
                    connectToDrive();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        removeBtn.setEnabled(true);
                        mAccountName.setText(accountName);
                        connectToDrive();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    connectToDrive();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
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
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        Toast.makeText(getContext(), "onPermissionsGranted " + requestCode, Toast.LENGTH_SHORT).show();
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        Toast.makeText(getContext(), "onPermissionsDenied " + requestCode, Toast.LENGTH_SHORT).show();
    }

    /**
     * Checks whether the device currently has a network connection.
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
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
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
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
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

    private class UploadFileTask extends GoogleDriveAbstractAsyncTask<Void, Void, Boolean> {
        UploadFileTask (GoogleAccountCredential credential) {
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

            java.io.File file = new java.io.File(getContext().getFilesDir(), "config.json");
            FileOutputStream os = new FileOutputStream(file);
            os.write("frjfneifbnier".getBytes());
            os.flush();
            os.close();

            File fileMetadata = new File();
            fileMetadata.setName("config.json");
            fileMetadata.setParents(Collections.singletonList("appDataFolder"));

            if (!file.exists()) {
                return false;
            }
            FileContent mediaContent = new FileContent("application/json", file);
            mService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();

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

    private class CheckPermissionsTask extends GoogleDriveAbstractAsyncTask<Void, Void, Boolean> {
        CheckPermissionsTask (GoogleAccountCredential credential) {
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
                    .setSpaces("appDataFolder")
                    .setPageSize(1)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            return result.getFiles() != null;
        }

        @Override
        protected void onPreExecute() {
            mOutputText.setText("Checking permissions ...");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(Boolean output) {
            mProgress.hide();
            if (output) {
                mOutputText.setText("Permissions OK.");
            } else {
                mOutputText.setText("Permissions not granted.");
            }
            uploadBtn.setEnabled(true);
            downloadBtn.setEnabled(true);
            mSyncStatus.setText("");
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
    private class MakeRequestTask extends GoogleDriveAbstractAsyncTask<Void, Void, List<String>> {

        MakeRequestTask(GoogleAccountCredential credential) {
            super(credential);
        }

        /**
         * Background task to call Drive API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of up to 10 file names and IDs.
         * @return List of Strings describing files, or an empty list if no files
         *         found.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            // Get a list of up to 10 files.
            List<String> fileInfo = new ArrayList<String>();
            FileList result = mService.files().list()
                    .setSpaces("appDataFolder")
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            List<File> files = result.getFiles();
            if (files != null) {
                for (File file : files) {
                    fileInfo.add(String.format("%s (%s)\n",
                            file.getName(), file.getId()));
                }
            }
            return fileInfo;
        }


        @Override
        protected void onPreExecute() {
            mOutputText.setText("Retrieving data ...");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            if (output == null) {
                mOutputText.setText("Error occured.");
            } else if (output.size() == 0) {
                mOutputText.setText("No results returned.");
            } else {
                output.add(0, "Data retrieved using the Drive API:");
                mOutputText.setText(TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
            onAsyncTaskCancel(mLastError);
        }
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
}

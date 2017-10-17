package sk.momosi.fuelup.screens;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
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

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import pub.devrel.easypermissions.EasyPermissions;
import sk.momosi.fuelup.R;
import sk.momosi.fuelup.business.googledrive.CheckPermissionsTask;
import sk.momosi.fuelup.business.googledrive.CheckPreviousAppInstalledTask;
import sk.momosi.fuelup.business.googledrive.DriveBackupFileUtil;
import sk.momosi.fuelup.business.googledrive.DriveFileUploadTask;
import sk.momosi.fuelup.business.googledrive.DriveRequestTask;
import sk.momosi.fuelup.business.googledrive.ImportVehicleJsonException;
import sk.momosi.fuelup.business.googledrive.ImportVehiclesTask;
import sk.momosi.fuelup.business.googledrive.JsonUtil;
import sk.momosi.fuelup.data.FuelUpContract;
import sk.momosi.fuelup.screens.dialog.RestoreVehicleDialog;
import sk.momosi.fuelup.util.ConnectivityUtils;
import sk.momosi.fuelup.util.PreferencesUtils;

import static android.app.Activity.RESULT_OK;

/**
 * @author Ondro
 * @version 10.10.2017
 */
public class BackupFragment extends Fragment implements EasyPermissions.PermissionCallbacks,
        RestoreVehicleDialog.Callback,
        DriveRequestTask.Callback,
        DriveFileUploadTask.Callback,
        CheckPermissionsTask.Callback,
        CheckPreviousAppInstalledTask.Callback,
        ImportVehiclesTask.Callback {

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String LOG_TAG = BackupFragment.class.getSimpleName();
    private GoogleAccountCredential mCredential;
    private ProgressDialog mProgress;
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
        setRetainInstance(true);
        View rootview = inflater.inflate(R.layout.fragment_backup, container, false);
        initializeViews(rootview);

        mCredential = DriveBackupFileUtil.generateCredential(getContext());

        getActivity().findViewById(R.id.fab_add_vehicle).setVisibility(View.GONE);
        return rootview;
    }

    @Override
    public void onStart() {
        super.onStart();

        mProgress = new ProgressDialog(getContext());
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.setCancelable(false);
        mProgress.setMessage("Calling Drive API ...");

        String accountName = PreferencesUtils.getString(getContext(), PreferencesUtils.BACKUP_FRAGMENT_ACCOUNT_NAME);

        if (!ConnectivityUtils.isGooglePlayServicesAvailable(getContext())) {
            acquireGooglePlayServices();

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
        if (!ConnectivityUtils.isDeviceOnline(getContext()))
            Toast.makeText(getContext(), "You must be online to check permissions", Toast.LENGTH_SHORT).show();
        else
            new CheckPermissionsTask(mCredential, this).execute();
    }

    private void assignAccount(String googleDriveAccount) {
        PreferencesUtils.setString(getContext(), PreferencesUtils.BACKUP_FRAGMENT_ACCOUNT_NAME, googleDriveAccount);
        PreferencesUtils.remove(getContext(), PreferencesUtils.BACKUP_FRAGMENT_ACCOUNT_IMPORT_ASKED);

        mAccountName.setText(googleDriveAccount);
        mCredential.setSelectedAccountName(googleDriveAccount);
        removeBtn.setEnabled(true);
        Account account = new Account(FuelUpContract.CONTENT_AUTHORITY, FuelUpContract.CONTENT_AUTHORITY);
        ContentResolver.setIsSyncable(account, FuelUpContract.CONTENT_AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(account, FuelUpContract.CONTENT_AUTHORITY, true);
        ContentResolver.addPeriodicSync(
                account, FuelUpContract.CONTENT_AUTHORITY, new Bundle(), 60 * 60);
        Log.e(LOG_TAG, "Sync enabled");
        checkPermissions();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().findViewById(R.id.fab_add_vehicle).setVisibility(View.VISIBLE);
    }


    private void getFilesFromDrive() {
        new DriveRequestTask(mCredential, this).execute();
    }

    private void uploadFileThroughApi() {
        if (PreferencesUtils.getBoolean(getContext(), PreferencesUtils.BACKUP_FRAGMENT_ACCOUNT_IMPORT_ASKED)) {
//            new DriveFileUploadTask(mCredential, this, getContext()).execute();

            Bundle b = new Bundle();
            b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

            Account account = new Account(FuelUpContract.CONTENT_AUTHORITY, FuelUpContract.CONTENT_AUTHORITY);
            ContentResolver.requestSync(account, FuelUpContract.CONTENT_AUTHORITY, b);

            Toast.makeText(getContext(), "Syncing started", Toast.LENGTH_SHORT).show();
        } else {
            mOutputText.setText("Backup is available only after importing or deleting previous version from Google Drive. If you want to remove your old backup and use only actual data, select no vehicle and press 'Start import' on Import Dialog.");
        }
    }

    // @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
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
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(getContext());
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
    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                getActivity(),
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @Override
    public void onDialogPositiveClick(Set<String> vehicleNames) {
        if (vehicleNames != null && !vehicleNames.isEmpty()) {
            new ImportVehiclesTask(vehicleNames, json, this, getContext()).execute();
        } else {
            Toast.makeText(getContext(), "You chose nothing to import. Your previous back up have been removed.", Toast.LENGTH_LONG).show();
            mOutputText.setText("Your account is set and syncing.");
            // TODO set syncing job
            PreferencesUtils.setBoolean(getContext(), PreferencesUtils.BACKUP_FRAGMENT_ACCOUNT_IMPORT_ASKED, true);
        }
    }

    private JSONObject json = null;

    private void importSpecifiedVehiclesFromJson(JSONObject json) {

        ArrayList<String> vehicles;
        try {
            vehicles = JsonUtil.getVehicleNamesFromJson(json);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Json format exception occurred.", e);
            vehicles = new ArrayList<>();
        }

        this.json = json;
        RestoreVehicleDialog.newInstance(vehicles, this).show(getFragmentManager(), RestoreVehicleDialog.class.getSimpleName());
    }


    @Override
    public void onDriveRequestTaskPreExecute() {
        Log.i(LOG_TAG, "AsyncTask DriveRequestTask started");
        mOutputText.setText("Retrieving data ...");
        mProgress.show();
    }

    @Override
    public void onDriveRequestTaskPostExecute(String output) {
        mProgress.hide();
        if (output == null) {
            mOutputText.setText("Error occured.");
        } else {
            mOutputText.setText("Data:\n" + output);
        }
    }

    @Override
    public void onDriveFileUploadTaskPreExecute() {
        Log.i(LOG_TAG, "AsyncTask DriveFileUploadTask started");
        mOutputText.setText("Upload in progress ...");
        mProgress.show();
    }

    @Override
    public void onDriveFileUploadTaskPostExecute(Boolean output) {
        mProgress.hide();
        if (output) {
            mOutputText.setText("Uploaded successfully.");
        } else {
            mOutputText.setText("Upload failed.");
        }
    }

    @Override
    public void onCheckPermissionsTaskPreExecute() {
        Log.i(LOG_TAG, "AsyncTask CheckPermissionsTask started");
        mOutputText.setText("Checking permissions ...");
        uploadBtn.setEnabled(false);
        downloadBtn.setEnabled(false);
        mProgress.show();
    }

    @Override
    public void onCheckPermissionsTaskPostExecute(Boolean output) {
        mProgress.hide();
        if (output) {
            mOutputText.setText("Permissions OK.");
            uploadBtn.setEnabled(true);
            downloadBtn.setEnabled(true);
            mSyncStatus.setText("");

            if (!PreferencesUtils.getBoolean(getContext(), PreferencesUtils.BACKUP_FRAGMENT_ACCOUNT_IMPORT_ASKED)) {
                new CheckPreviousAppInstalledTask(mCredential, this).execute();
            }
        } else {
            mOutputText.setText("Permissions not granted.");
        }
    }


    @Override
    public void onCheckPreviousAppInstalledTaskPreExecute() {
        Log.i(LOG_TAG, "AsyncTask CheckPreviousAppInstalledTask started");
        mOutputText.setText("Retrieving Google Drive last backup ...");
        mProgress.show();
    }

    @Override
    public void onCheckPreviousAppInstalledTaskPostExecute(JSONObject json) {
        mProgress.hide();
        if (json == null) {
            mOutputText.setText("There is no backed-up previous version on Google Drive to restore. Your account is now syncing.");
            PreferencesUtils.setBoolean(getContext(), PreferencesUtils.BACKUP_FRAGMENT_ACCOUNT_IMPORT_ASKED, true);
        } else {
            mOutputText.setText("There is previous version of backup on your Google Drive account.");
            importSpecifiedVehiclesFromJson(json);
        }
    }

    @Override
    public void onAsyncTaskCancel(Exception mLastError) {
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

    @Override
    public void onImportVehiclesTaskPreExecute() {
        Log.i(LOG_TAG, "AsyncTask ImportVehiclesTask started");
        mOutputText.setText("Importing vehicles from backup ...");
        mProgress.show();
    }

    @Override
    public void onImportVehiclesTaskPostExecute(Integer output) {
        PreferencesUtils.setBoolean(getContext(), PreferencesUtils.BACKUP_FRAGMENT_ACCOUNT_IMPORT_ASKED, true);
        mOutputText.setText("Your vehicles have been imported successfully. Your account is now set and syncing.");
        // TODO set syncing job
        mProgress.hide();
    }

    @Override
    public void onImportVehiclesTaskCancel(Exception mLastError) {
        mProgress.hide();
        if (mLastError != null) {
            if (mLastError instanceof ImportVehicleJsonException) {
                mOutputText.setText("Your backed-up vehicles are broken. Please, try again later and if problem persist, contact us.");
                Log.e(LOG_TAG, "ImportingVehicleError: " + mLastError.toString());
            }
        } else {
            mOutputText.setText("Request cancelled.");
        }
    }
}

package sk.momosi.fuelup.screens;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import sk.momosi.fuelup.R;
import sk.momosi.fuelup.business.googledrive.CheckPermissionsTask;
import sk.momosi.fuelup.business.googledrive.DriveBackupFileUtil;
import sk.momosi.fuelup.business.googledrive.syncing.DriveSyncingUtils;
import sk.momosi.fuelup.screens.backup.ChooseAccountActivity;
import sk.momosi.fuelup.util.ConnectivityUtils;
import sk.momosi.fuelup.util.PreferencesUtils;

import static android.app.Activity.RESULT_OK;

/**
 * @author Ondrej Oravcok
 * @version 10.10.2017
 */
public class BackupFragment extends Fragment implements
        EasyPermissions.PermissionCallbacks,
        CheckPermissionsTask.Callback {

    private static final String LOG_TAG = BackupFragment.class.getSimpleName();

    private static final int PRIMARY_COLOR = R.color.colorPrimary;
    private static final int RED_WARN_COLOR = R.color.colorWarningRed;

    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;

    private ProgressDialog mProgress;
    private TextView mOutputText;
    private TextView mAccountName;
    private TextView mSyncStatus;

    private GoogleAccountCredential mCredential;

    private boolean showRemoveButton;
    private boolean showForceUploadButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true);
        View rootView = inflater.inflate(R.layout.fragment_backup, container, false);
        initializeViews(rootView);

        mCredential = DriveBackupFileUtil.generateCredential(getContext());

        getActivity().findViewById(R.id.fab_add_vehicle).setVisibility(View.GONE);
        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        mProgress = new ProgressDialog(getContext());
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.setMessage("Calling Drive API ...");

        String accountName = PreferencesUtils.getAccountName(getContext());

        if (!ConnectivityUtils.isGooglePlayServicesAvailable(getContext())) {
            acquireGooglePlayServices();

        } else {
            mCredential.setSelectedAccountName(accountName);
            showRemoveButton = true;
            mAccountName.setText(accountName);
            mSyncStatus.setText(R.string.googleDrive_cannot_connect);
            checkPermissions();
        }
    }

    private void initializeViews(View rootview) {
        mAccountName = rootview.findViewById(R.id.txt_sync_account);
        mSyncStatus = rootview.findViewById(R.id.sync_status);
        mOutputText = rootview.findViewById(R.id.txt_sync);

        mOutputText.setVerticalScrollBarEnabled(true);
        mOutputText.setTextColor(ContextCompat.getColor(getContext(), PRIMARY_COLOR));
        mOutputText.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.backup_action_bar, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.googleDrive_removeAccount_item).setEnabled(showRemoveButton);
        menu.findItem(R.id.googleDrive_forceUpload_item).setEnabled(showForceUploadButton);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.googleDrive_forceUpload_item:
                uploadFileThroughApi();
                return true;
            case R.id.googleDrive_removeAccount_item:
                removeAccount();
                return true;
            case R.id.googleDrive_testSync:
                testSync();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void testSync() {
        if (!DriveSyncingUtils.isSyncable() || mCredential.getSelectedAccount() == null)
            Toast.makeText(getContext(), R.string.googleDrive_syncing_NOT_active, Toast.LENGTH_SHORT).show();
        else if (PreferencesUtils.getLastSync(getContext()) < PreferencesUtils.getLastChange(getContext()))
            if (DriveSyncingUtils.isSyncPending())
                Toast.makeText(getContext(), R.string.googleDrive_syncingActive_pendingUploads, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getContext(), R.string.googleDrive_syncingActive_backupDiscarded, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getContext(), R.string.googleDrive_sync_upToDate, Toast.LENGTH_SHORT).show();
    }

    private void removeAccount() {
        mCredential.setSelectedAccountName(null);
        showRemoveButton = false;
        mAccountName.setText(R.string.googleDrive_noneAccount);
        mSyncStatus.setText(R.string.googleDrive_not_configured);

        DriveSyncingUtils.disableSync(getContext());

        startActivity(new Intent(getActivity(), ChooseAccountActivity.class));
    }

    private void checkPermissions() {
        if (ConnectivityUtils.isNotDeviceOnline(getContext()))
            Toast.makeText(getContext(), R.string.googleDrive_mustBeOnline, Toast.LENGTH_SHORT).show();
        else
            new CheckPermissionsTask(mCredential, this).execute();
    }

    private void assignAccount(String googleDriveAccount) {
        PreferencesUtils.setAccountName(getContext(), googleDriveAccount);
        PreferencesUtils.remove(getContext(), PreferencesUtils.BACKUP_FRAGMENT_ACCOUNT_IMPORT_ASKED);

        mAccountName.setText(googleDriveAccount);
        mCredential.setSelectedAccountName(googleDriveAccount);
        showRemoveButton = true;

        checkPermissions();
    }

    private void uploadFileThroughApi() {
        boolean isUploadAvailable = PreferencesUtils.hasBeenImportDone(getContext());

        if (isUploadAvailable) {
            if (ConnectivityUtils.isNotDeviceOnline(getContext())) {
                Toast.makeText(getContext(), R.string.googleDrive_mustBeOnline, Toast.LENGTH_SHORT).show();
            } else {
                DriveSyncingUtils.requestImmediateSync();
                Toast.makeText(getContext(),
                        R.string.googleDrive_toast_syncInProgress,
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            mOutputText.setTextColor(ContextCompat.getColor(getContext(), RED_WARN_COLOR));
            mOutputText.setText(R.string.googleDrive_err_backup_not_available);
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
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    onStart();
                }
                break;
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    mSyncStatus.setText(R.string.googleDrive_requires_google_play);
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

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) { }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) { }

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
    private void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                getActivity(),
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @Override
    public void onCheckPermissionsTaskPreExecute() {
        Log.i(LOG_TAG, "AsyncTask CheckPermissionsTask started");
        mOutputText.setTextColor(ContextCompat.getColor(getContext(), PRIMARY_COLOR));
        mOutputText.setText(R.string.googleDrive_checkPermissions_loader);

        showForceUploadButton = false;
        mProgress.show();
    }

    @Override
    public void onCheckPermissionsTaskPostExecute(Boolean output) {
        mProgress.hide();
        if (output) {
            mOutputText.setText("");
            showForceUploadButton = true;
            mSyncStatus.setText("");

        } else {
            mOutputText.setTextColor(ContextCompat.getColor(getContext(), RED_WARN_COLOR));
            mOutputText.setText(R.string.googleDrive_permissionsNotGranted);
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
                mOutputText.setTextColor(ContextCompat.getColor(getContext(), RED_WARN_COLOR));
                if (mLastError instanceof GoogleAuthIOException) {
                    Log.e(LOG_TAG,"Authentication error occured when calling Google Drive API.", mLastError);
                    mOutputText.setText(R.string.googleDrive_authErr);
                } else {
                    mOutputText.setText(getString(R.string.googleDrive_errOccurred, mLastError.getMessage()));
                }
            }
        } else {
            mOutputText.setTextColor(ContextCompat.getColor(getContext(), RED_WARN_COLOR));
            mOutputText.setText(R.string.googleDrive_cancelledRequest);
        }
    }

}

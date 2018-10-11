package sk.momosi.fuelup.screens.backup;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import sk.momosi.fuelup.R;
import sk.momosi.fuelup.business.googledrive.CheckPermissionsTask;
import sk.momosi.fuelup.business.googledrive.DriveBackupFileUtil;
import sk.momosi.fuelup.util.ConnectivityUtils;

/**
 * @author Ondro
 * @version 12.11.2017
 */
public class CheckPermissionsActivity extends AppCompatActivity implements CheckPermissionsTask.Callback {

    private static final String LOG_TAG = CheckPermissionsActivity.class.getSimpleName();

    public static final String KEY_ACCOUNT_FROM_CHECK_PERMISSIONS = "key_account";

    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final int REQUEST_CODE_PICK_ACCOUNT = 1003;

    private GoogleAccountCredential mCredential;
    private CheckPermissionsTask task;

    private TextView txtStatus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_check_permissions);

        ProgressBar progressBar = findViewById(R.id.checkPermissions_progress);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        txtStatus = findViewById(R.id.checkPermissions_txtStatus);

        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                new String[] {"com.google"},
                false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    private void checkPermissions() {
        txtStatus.setText("");
        if (ConnectivityUtils.isNotDeviceOnline(this)) {
            Toast.makeText(this, R.string.googleDrive_mustBeOnline, Toast.LENGTH_SHORT).show();
        } else {
            if (task == null)
              task = new CheckPermissionsTask(mCredential, this);

            if (task.getStatus().equals(AsyncTask.Status.PENDING))
                task.execute();
        }
    }


    /* Async Task */
    @Override
    public void onCheckPermissionsTaskPreExecute() {
        Log.e(LOG_TAG, "pre execute");
    }

    @Override
    public void onCheckPermissionsTaskPostExecute(Boolean output) {
        Log.e(LOG_TAG, "post execute");
        Intent intent = new Intent(this, CheckPreviousVersionsActivity.class);
        intent.putExtra(KEY_ACCOUNT_FROM_CHECK_PERMISSIONS, mCredential.getSelectedAccountName());
        startActivity(intent);
    }

    @Override
    public void onAsyncTaskCancel(Exception mLastError) {
        Log.e(LOG_TAG, "async error");
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
                if (mLastError instanceof GoogleAuthIOException) {
                    Log.e(LOG_TAG,"Authentication error occured when calling Google Drive API.", mLastError);
                    txtStatus.setText(R.string.googleDrive_authErr);
                } else {
                    txtStatus.setText(getString(R.string.googleDrive_errOccurred, mLastError.getMessage()));
                }
            }
        } else {
            txtStatus.setText(R.string.googleDrive_cancelledRequest);
        }
    }

    private void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_PICK_ACCOUNT:
                Bundle extras = data.getExtras();
                if (extras != null) {
                    mCredential = DriveBackupFileUtil.generateCredential(this);
                    mCredential.setSelectedAccountName(extras.getString(AccountManager.KEY_ACCOUNT_NAME));
                    checkPermissions();
                }
                break;
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this, R.string.googleDrive_requires_google_play, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    checkPermissions();
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    checkPermissions();
                } else {
                    Toast.makeText(this, R.string.googleDrive_haveToGrantPrivileges, Toast.LENGTH_LONG).show();
                    onBackPressed();
                }
                break;
        }
    }
}

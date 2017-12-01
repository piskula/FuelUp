package sk.momosi.fuelup.screens.backup;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import sk.momosi.fuelup.R;
import sk.momosi.fuelup.business.googledrive.DriveBackupFileUtil;
import sk.momosi.fuelup.business.googledrive.ImportVehiclesTask;
import sk.momosi.fuelup.business.googledrive.syncing.DriveSyncingUtils;
import sk.momosi.fuelup.screens.MainActivity;
import sk.momosi.fuelup.util.ConnectivityUtils;
import sk.momosi.fuelup.util.PreferencesUtils;

/**
 * @author Ondro
 * @version 12.11.2017
 */
public class ImportVehiclesActivity extends AppCompatActivity implements ImportVehiclesTask.Callback {

    private static final String LOG_TAG = ImportVehiclesActivity.class.getSimpleName();

    public static final String KEY_ACCOUNT = "import_account_key";
    public static final String KEY_VEHCLES = "import_vehicles_key";
    public static final String KEY_JSON = "import_json_key";

    private ImportVehiclesTask task;
    private String account;

    private TextView txtStatus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_import_vehicles);

        ProgressBar progressBar = findViewById(R.id.importVehicles_progress);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        txtStatus = findViewById(R.id.importVehicles_txtStatus);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            initDataAndStartImport(extras);
        } else {
            Log.e(LOG_TAG, "No account or vehicles passed to activity in Intent.");
        }
    }

    private void initDataAndStartImport(final Bundle extras) {
        txtStatus.setText("");
        if (ConnectivityUtils.isNotDeviceOnline(this)) {
            Toast.makeText(this, R.string.googleDrive_mustBeOnline, Toast.LENGTH_SHORT).show();
        } else {
            List<String> vehiclesToImport;
            JSONObject json;

            account = extras.getString(KEY_ACCOUNT);
            vehiclesToImport = extras.getStringArrayList(KEY_VEHCLES);

            try { json = new JSONObject(extras.getString(KEY_JSON)); }
                catch(JSONException e) {
                Log.e(LOG_TAG, "");
                return;
            }

            if (task == null)
                task = new ImportVehiclesTask(vehiclesToImport, json, this, this);

            if (task.getStatus().equals(AsyncTask.Status.PENDING))
                task.execute();
        }
    }


    /* Async Task */
    @Override
    public void onImportVehiclesTaskPreExecute() {
    }

    @Override
    public void onImportVehiclesTaskPostExecute(Integer output) {
        PreferencesUtils.setAccountName(this, account);
        initializeSyncing();
        Toast.makeText(this, getString(R.string.googleDrive_setAndSyncing, account), Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onImportVehiclesTaskCancel(Exception mLastError) {
        if (mLastError != null) {
            txtStatus.setText(getString(R.string.googleDrive_errOccurred, mLastError.getMessage()));
        } else {
            txtStatus.setText(R.string.googleDrive_cancelledRequest);
        }
    }

    private void initializeSyncing() {
        DriveSyncingUtils.enableSyncGlobally(this);
        DriveSyncingUtils.requestImmediateSync();
    }
}

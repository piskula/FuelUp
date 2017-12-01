package sk.momosi.fuelup.screens.backup;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import sk.momosi.fuelup.R;
import sk.momosi.fuelup.adapters.ListVehiclesRestoreAdapter;
import sk.momosi.fuelup.business.googledrive.CheckPreviousAppInstalledTask;
import sk.momosi.fuelup.business.googledrive.DriveBackupFileUtil;
import sk.momosi.fuelup.business.googledrive.JsonUtil;
import sk.momosi.fuelup.business.googledrive.syncing.DriveSyncingUtils;
import sk.momosi.fuelup.screens.MainActivity;
import sk.momosi.fuelup.util.ConnectivityUtils;
import sk.momosi.fuelup.util.PreferencesUtils;

import static sk.momosi.fuelup.screens.backup.CheckPermissionsActivity.KEY_ACCOUNT_FROM_CHECK_PERMISSIONS;
import static sk.momosi.fuelup.screens.backup.ImportVehiclesActivity.KEY_ACCOUNT;
import static sk.momosi.fuelup.screens.backup.ImportVehiclesActivity.KEY_JSON;
import static sk.momosi.fuelup.screens.backup.ImportVehiclesActivity.KEY_VEHCLES;

/**
 * @author Ondro
 * @version 14.11.2017
 */
public class CheckPreviousVersionsActivity extends AppCompatActivity
        implements View.OnClickListener, CheckPreviousAppInstalledTask.Callback, ListVehiclesRestoreAdapter.Callback {

    private static final String LOG_TAG = CheckPreviousVersionsActivity.class.getSimpleName();

    private GoogleAccountCredential mCredential;
    private CheckPreviousAppInstalledTask task;
    private JSONObject json = null;

    private ListVehiclesRestoreAdapter adapter;

    private RelativeLayout chooseVehiclesLayout;
    private LinearLayout loader;
    private RecyclerView vehiclesList;
    private ProgressBar progress;
    private TextView count;
    private Button nextBtn;


    private Set<String> vehiclesChosen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_check_previous);

        chooseVehiclesLayout = findViewById(R.id.checkPrevious_chooseVehicles);
        loader = findViewById(R.id.checkPrevious_loader);
        vehiclesList = findViewById(R.id.checkPermissions_listVehicles);
        progress = findViewById(R.id.checkPrevious_progress);
        count = findViewById(R.id.choose_vehicle_title_count);
        nextBtn = findViewById(R.id.checkPrevious_btnNext);

        vehiclesList.setLayoutManager(new LinearLayoutManager(this));
        progress.getIndeterminateDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        nextBtn.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mCredential = DriveBackupFileUtil.generateCredential(this);
            mCredential.setSelectedAccountName(extras.getString(KEY_ACCOUNT_FROM_CHECK_PERMISSIONS));
            checkPreviousVersions();
        } else {
            Log.e(LOG_TAG, "No account passed to activity in Intent.");
        }
    }

    private void checkPreviousVersions() {
        if (ConnectivityUtils.isNotDeviceOnline(this)) {
            Toast.makeText(this, R.string.googleDrive_mustBeOnline, Toast.LENGTH_SHORT).show();
        } else {
            if (task == null)
                task = new CheckPreviousAppInstalledTask(mCredential, this, this);

            if (!task.getStatus().equals(AsyncTask.Status.RUNNING))
                task.execute();
        }
    }

    private int numberOfClicks = 0;
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.checkPrevious_btnNext:
                numberOfClicks++;
                if (vehiclesChosen.size() < adapter.getItemCount()) {
                    if (numberOfClicks <= 1) {
                        Toast.makeText(this, R.string.googleDrive_import_delete_warning, Toast.LENGTH_LONG).show();
                    } else if (vehiclesChosen.isEmpty()) {
                        setAccountAndFinish();
                    } else {
                        startImportActivity();
                    }
                } else {
                    startImportActivity();
                }
                break;
        }
    }

    private void startImportActivity() {
        Intent intent = new Intent(this, ImportVehiclesActivity.class);
        intent.putExtra(KEY_ACCOUNT, mCredential.getSelectedAccountName());
        intent.putExtra(KEY_VEHCLES, new ArrayList<>(vehiclesChosen));
        intent.putExtra(KEY_JSON, json.toString());
        startActivity(intent);
    }


    /* async task */
    @Override
    public void onCheckPreviousAppInstalledTaskPreExecute() {
        loader.setVisibility(View.VISIBLE);
        chooseVehiclesLayout.setVisibility(View.GONE);
    }

    @Override
    public void onCheckPreviousAppInstalledTaskPostExecute(Pair<JSONObject, Set<String>> result) {
        JSONObject json = result.first;
        if (json == null) {
            setAccountAndFinish();

        } else {
            List<String> vehicles = getVehiclesFromJsonOrShowErrorToast(json);
            if (vehicles.isEmpty()) {   // previous version exists but is empty
                setAccountAndFinish();

            } else {
                this.json = json;
                loader.setVisibility(View.GONE);
                chooseVehiclesLayout.setVisibility(View.VISIBLE);
                nextBtn.setVisibility(View.VISIBLE);
                if (adapter == null)
                    adapter = new ListVehiclesRestoreAdapter(this, this, vehicles, result.second);
                vehiclesList.setAdapter(adapter);
            }
        }
    }

    private void setAccountAndFinish() {
        String account = mCredential.getSelectedAccountName();

        PreferencesUtils.setAccountName(this, account);
        initializeSyncing();
        Toast.makeText(this, getString(R.string.googleDrive_setAndSyncing, account), Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onAsyncTaskCancel(Exception mLastError) {
        Log.e(LOG_TAG, "cancel");
    }

    private List<String> getVehiclesFromJsonOrShowErrorToast(JSONObject json) {
        ArrayList<String> vehicles;

        try {
            vehicles = JsonUtil.getVehicleNamesFromJson(json);
        } catch (JSONException e) {
            Toast.makeText(this, "There is previous version of your backup, which may be damaged. Try again and if problem persists, contact us.", Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, "Json format exception occurred.", e);
            vehicles = new ArrayList<>();
        }

        return Collections.unmodifiableList(vehicles);
    }

    @Override
    public void onVehiclesChosenChange(Set<String> vehiclesChosen) {
        this.vehiclesChosen = Collections.unmodifiableSet(vehiclesChosen);
        this.count.setText(String.valueOf(vehiclesChosen.size()));
        numberOfClicks = 0;
    }

    @Override
    public void makeWarningToastForVehicle(String name) {
        Snackbar.make(chooseVehiclesLayout, R.string.googleDrive_existing_vehicle, Snackbar.LENGTH_SHORT).show();
    }

    private void initializeSyncing() {
        DriveSyncingUtils.enableSyncGlobally(this);
        DriveSyncingUtils.requestImmediateSync();
    }
}

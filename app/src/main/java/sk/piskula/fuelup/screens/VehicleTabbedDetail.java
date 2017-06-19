package sk.piskula.fuelup.screens;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.data.DatabaseHelper;
import sk.piskula.fuelup.entity.FillUp;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.screens.detailfragments.ExpensesListFragment;
import sk.piskula.fuelup.screens.detailfragments.FillUpsListFragment;
import sk.piskula.fuelup.screens.detailfragments.StatisticsFragment;
import sk.piskula.fuelup.screens.edit.UpdateVehicle;

/**
 * @author Ondrej Oravcok
 * @version 17.6.2017
 */
public class VehicleTabbedDetail extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "VehicleTabbedDetail";

    public static final String VEHICLE_TO_FRAGMENT = "fragment-vehicle";

    private Fragment fragment;
    private FragmentManager fragmentManager;
    private Vehicle vehicle;

    private FloatingActionButton addButton;

    private DatabaseHelper databaseHelper;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(VEHICLE_TO_FRAGMENT, vehicle);
            switch (item.getItemId()) {
                case R.id.navigation_fillUps:
                    fragment = new FillUpsListFragment();
                    getSupportActionBar().setTitle("Fill Ups");
                    addButton.setVisibility(View.VISIBLE);
                    break;
                case R.id.navigation_expenses:
                    fragment = new ExpensesListFragment();
                    getSupportActionBar().setTitle("Expenses");
                    addButton.setVisibility(View.VISIBLE);
                    break;
                case R.id.navigation_statistics:
                    fragment = new StatisticsFragment();
                    getSupportActionBar().setTitle("Statistics");
                    addButton.setVisibility(View.GONE);
                    break;
            }
            fragment.setArguments(bundle);
            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.vehicle_detail_frame, fragment).commit();
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vehicle_tabbed_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        addButton = (FloatingActionButton) findViewById(R.id.fab_add);
        addButton.setOnClickListener(this);
        Intent intent = getIntent();
        vehicle = (Vehicle) intent.getSerializableExtra(VehicleList.EXTRA_ADDED_CAR);

        fragmentManager = getSupportFragmentManager();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.vehicle_detail_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_fillUps);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.vehicle_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_vehicle_remove:
                removeVehicle();
                return true;
            case R.id.btn_vehicle_update:
                updateVehicle();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private void removeVehicle() {
        final AlertDialog confirmDialog = confirmDeletion();
        confirmDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                //only styling here, no business logic
                Button negative = confirmDialog.getButton(confirmDialog.BUTTON_NEGATIVE);
                negative.setFocusable(true);
                negative.setFocusableInTouchMode(true);
                negative.requestFocus();
            }
        });
        confirmDialog.show();
    }

    private void updateVehicle() {
        Intent i = new Intent(this, UpdateVehicle.class);
        i.putExtra(VEHICLE_TO_FRAGMENT, vehicle);
        startActivity(i);
    }

    private AlertDialog confirmDeletion() {
        return new AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle("Delete")
                .setMessage("Do you want to completely remove vehicle '" + vehicle.getName()
                        + "' and all its data? You can never get it back.")
                .setIcon(R.drawable.tow)
                .setPositiveButton("Yes, delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        dialog.dismiss();
                        try {
                            getHelper().getVehicleDao().delete(vehicle);
                        } catch (SQLException e) {
                            String msg = "Failed to remove vehicle '" + vehicle.getName() + "'.";
                            Log.e(TAG, msg, e);
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                        }
                        String msg = "Vehicle removed successfully.";
                        Log.i(TAG, msg);
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

                        finish();
                    }
                })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == addButton.getId() && fragment instanceof FillUpsListFragment){
            Snackbar.make(view, "Add FillUp", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            return;
        }
        if(view.getId() == addButton.getId() && fragment instanceof ExpensesListFragment){
            Snackbar.make(view, "Add Expense", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }
    }
}

package sk.piskula.fuelup.screens;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import java.sql.SQLException;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.data.DatabaseProvider;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.screens.detailfragments.ExpensesListFragment;
import sk.piskula.fuelup.screens.detailfragments.FillUpsListFragment;
import sk.piskula.fuelup.screens.detailfragments.StatisticsFragment;
import sk.piskula.fuelup.screens.dialog.DeleteDialog;
import sk.piskula.fuelup.screens.edit.EditVehicle;

/**
 * @author Ondrej Oravcok
 * @author Martin Styk
 * @version 17.6.2017
 */
public class VehicleTabbedDetail extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, DeleteDialog.Callback {

    private static final String TAG = VehicleTabbedDetail.class.getSimpleName();

    public static final String VEHICLE_TO_FRAGMENT = "fragment-vehicle";

    private Fragment fragment;
    private FragmentManager fragmentManager;
    private Vehicle vehicle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vehicle_tabbed_detail);

        setSupportActionBar((Toolbar) findViewById(R.id.detail_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        vehicle = (Vehicle) intent.getSerializableExtra(VehicleList.EXTRA_ADDED_CAR);

        fragmentManager = getSupportFragmentManager();

        ((BottomNavigationView) findViewById(R.id.vehicle_detail_navigation)).setOnNavigationItemSelectedListener(this);

        ((ImageView) findViewById(R.id.toolbar_layout_image)).setImageBitmap(vehicle.getPicture());

        if (savedInstanceState == null) {
            fragment = new FillUpsListFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(VEHICLE_TO_FRAGMENT, vehicle);
            fragment.setArguments(bundle);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.vehicle_detail_frame, fragment).commit();
        }
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
                DeleteDialog.newInstance(
                        getString(R.string.delete_vehicle_dialog_title, vehicle.getName()),
                        getString(R.string.delete_vehicle_dialog_message, vehicle.getName()),
                        R.drawable.tow)
                        .show(getSupportFragmentManager(), DeleteDialog.class.getSimpleName());
                return true;
            case R.id.btn_vehicle_update:
                Intent i = new Intent(this, EditVehicle.class).putExtra(VEHICLE_TO_FRAGMENT, vehicle);
                startActivity(i);
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(VEHICLE_TO_FRAGMENT, vehicle);
        switch (item.getItemId()) {
            case R.id.navigation_fillUps:
                fragment = new FillUpsListFragment();
                break;
            case R.id.navigation_expenses:
                fragment = new ExpensesListFragment();
                break;
            case R.id.navigation_statistics:
                fragment = new StatisticsFragment();
                break;
        }
        fragment.setArguments(bundle);
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.vehicle_detail_frame, fragment).commit();
        return false;
    }

    @Override
    public void onDeleteDialogPositiveClick(DeleteDialog dialog) {
        dialog.dismiss();
        try {
            DatabaseProvider.get(getParent()).getVehicleDao().delete(vehicle);
        } catch (SQLException e) {
            String msg = getString(R.string.delete_vehicle_fail, vehicle.getName());
            Log.e(TAG, msg, e);
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        }
        String msg = getString(R.string.delete_vehicle_success, vehicle.getName());
        Log.i(TAG, msg);
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

        // close detail activity when we delete vehicle
        finish();
    }

    @Override
    public void onDeleteDialogNegativeClick(DeleteDialog dialog) {
        dialog.dismiss();
    }
}

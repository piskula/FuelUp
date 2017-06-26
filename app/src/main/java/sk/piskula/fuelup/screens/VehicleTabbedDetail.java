package sk.piskula.fuelup.screens;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.business.ServiceResult;
import sk.piskula.fuelup.business.VehicleService;
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

    private static final int UPDATE_VEHICLE_REQUEST = 123;

    private Fragment fragment;
    private FragmentManager fragmentManager;
    private Vehicle vehicle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vehicle_tabbed_detail);

        setSupportActionBar((Toolbar) findViewById(R.id.detail_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fragmentManager = getSupportFragmentManager();

        ((BottomNavigationView) findViewById(R.id.vehicle_detail_navigation)).setOnNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            vehicle = intent.getParcelableExtra(VehicleList.EXTRA_ADDED_CAR);

            fragment = new FillUpsListFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(VEHICLE_TO_FRAGMENT, vehicle);
            fragment.setArguments(bundle);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.vehicle_detail_frame, fragment).commit();
        } else {
            vehicle = savedInstanceState.getParcelable(VEHICLE_TO_FRAGMENT);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Bitmap bmp = vehicle.getPicture();
        ((ImageView) findViewById(R.id.toolbar_layout_image)).setImageBitmap(bmp);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(VEHICLE_TO_FRAGMENT, vehicle);
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
                startActivityForResult(i, UPDATE_VEHICLE_REQUEST);
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(VEHICLE_TO_FRAGMENT, vehicle);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_VEHICLE_REQUEST && resultCode == RESULT_OK) {
            vehicle = new VehicleService(this).find(vehicle.getId());
        }
    }

    @Override
    public void onDeleteDialogPositiveClick(DeleteDialog dialog) {
        VehicleService vehicleService = new VehicleService(VehicleTabbedDetail.this);
        ServiceResult result = vehicleService.delete(vehicle);

        if (ServiceResult.SUCCESS.equals(result)) {
            Toast.makeText(getApplicationContext(), getString(R.string.delete_vehicle_success, vehicle.getName()), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), R.string.delete_vehicle_fail, Toast.LENGTH_LONG).show();
        }

        // close detail activity when we delete vehicle
        dialog.dismiss();
        finish();
    }

    @Override
    public void onDeleteDialogNegativeClick(DeleteDialog dialog) {
        dialog.dismiss();
    }
}

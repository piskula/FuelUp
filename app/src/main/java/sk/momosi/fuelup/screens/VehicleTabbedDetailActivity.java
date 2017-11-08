package sk.momosi.fuelup.screens;

import android.content.Intent;
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

import com.squareup.picasso.Picasso;

import sk.momosi.fuelup.R;
import sk.momosi.fuelup.business.VehicleService;
import sk.momosi.fuelup.entity.Vehicle;
import sk.momosi.fuelup.screens.detailfragments.ExpensesListFragment;
import sk.momosi.fuelup.screens.detailfragments.FillUpsListFragment;
import sk.momosi.fuelup.screens.detailfragments.StatisticsFragment;
import sk.momosi.fuelup.screens.edit.EditVehicleActivity;

/**
 * @author Ondrej Oravcok
 * @author Martin Styk
 * @version 17.6.2017
 */
public class VehicleTabbedDetailActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String LOG_TAG = VehicleTabbedDetailActivity.class.getSimpleName();

    public static final String VEHICLE_TO_FRAGMENT = "fragment-vehicle";

    private static final int UPDATE_VEHICLE_REQUEST = 123;

    private Fragment fragment;
    private FragmentManager fragmentManager;
    private Vehicle vehicle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_tabbed_detail);

        setSupportActionBar((Toolbar) findViewById(R.id.detail_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fragmentManager = getSupportFragmentManager();

        ((BottomNavigationView) findViewById(R.id.vehicle_detail_navigation)).setOnNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            long vehicleId = intent.getLongExtra(VehicleListFragment.EXTRA_ADDED_VEHICLE_ID, 0);
            vehicle = VehicleService.getVehicleById(vehicleId, this);

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

        Picasso.with(getApplicationContext()).load(vehicle.getPicture())
                .into((ImageView) findViewById(R.id.toolbar_layout_image));
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
            case R.id.btn_vehicle_update:
                Intent i = new Intent(this, EditVehicleActivity.class).putExtra(VEHICLE_TO_FRAGMENT, vehicle);
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
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_VEHICLE_REQUEST && resultCode == RESULT_OK) {
            vehicle = VehicleService.getVehicleById(vehicle.getId(), this);
        }
    }
}

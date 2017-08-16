package sk.piskula.fuelup.screens;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.MenuItem;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.data.FuelUpContract;
import sk.piskula.fuelup.screens.dialog.CreateVehicleDialog;

public class MainActivity extends AppCompatActivity implements OnNavigationItemSelectedListener, CreateVehicleDialog.Callback {

    private static MainActivity singleton;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.vehicle_list);
            getSupportFragmentManager().beginTransaction().replace(R.id.activty_main_frame, new VehicleListFragment(), TAG).commit();
        }

//        ContentValues values = new ContentValues();
//        getContentResolver().insert(FuelUpContract.VehicleEntry.CONTENT_URI,
//                values);

        singleton = this;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment currentFragment;
        if (id == R.id.vehicle_list) {
            currentFragment = new VehicleListFragment();
        } else if (id == R.id.about) {
            currentFragment = new AboutFragment();
        } else {
            throw new RuntimeException("onNavigationItemSelected unhandled case");
        }
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activty_main_frame, currentFragment, TAG).commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Interfragment communication
     */
    @Override
    public void onDialogCreateBtnClick(CreateVehicleDialog dialog, Editable vehicleName) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(TAG);
        if (currentFragment instanceof CreateVehicleDialog.Callback) {
            ((CreateVehicleDialog.Callback) currentFragment).onDialogCreateBtnClick(dialog, vehicleName);
        }
    }

    @Override
    public void onDialogAdvancedBtnClick(CreateVehicleDialog dialog, Editable vehicleName) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(TAG);
        if (currentFragment instanceof CreateVehicleDialog.Callback) {
            ((CreateVehicleDialog.Callback) currentFragment).onDialogAdvancedBtnClick(dialog, vehicleName);
        }
    }

    public static MainActivity getInstance() {
        return singleton;
    }
}

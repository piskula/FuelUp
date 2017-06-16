package sk.piskula.fuelup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;
import sk.piskula.fuelup.adapters.ListVehiclesAdapter;
import sk.piskula.fuelup.data.DatabaseHelper;
import sk.piskula.fuelup.entity.Vehicle;

@Slf4j
public class VehicleList extends AppCompatActivity
        implements OnNavigationItemSelectedListener, OnItemClickListener {

    private static final String SHARED_PREFERENCES_NAME = "sk.piskula.fuelup.preferences";
    private static final String PREFS_VEHICLE_ID_KEY = "vehicle_id";
    private static final String EXTRA_ADDED_CAR = "extra_key_added_car";

    private DatabaseHelper databaseHelper = null;

    private SharedPreferences sharedPreferences;

    private ListView listView;
    private ListVehiclesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);

        //TODO open last viewed car if possible

        setContentView(R.layout.vehicle_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.listView = (ListView) findViewById(R.id.list_cars);
        listView.setVisibility(View.VISIBLE);

        if (mAdapter == null) {
            mAdapter = new ListVehiclesAdapter(this);
            listView.setAdapter(mAdapter);
        } else {
            mAdapter.refreshItems(this);
            mAdapter.notifyDataSetChanged();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                final Dialog dialog = createVehicleDialog();
                dialog.setContentView(R.layout.create_vehicle_dialog);
                dialog.setTitle("Create Vehicle");

                EditText name = dialog.findViewById(R.id.createVehicleDialog_name);
                Button ok = dialog.findViewById(R.id.createVehicleDialog_ok);
                Button advanced = dialog.findViewById(R.id.createVehicleDialog_advanced);

                name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean hasFocus) {
                        if (hasFocus) {
                            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        }
                    }
                });

                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText name = dialog.findViewById(R.id.createVehicleDialog_name);
                        saveVehicle(name.getText().toString(), view);
                        dialog.dismiss();
                    }
                });

                advanced.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO
                    }
                });
                dialog.show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private Dialog createVehicleDialog() {
        return new Dialog(this);
    }

    private void saveVehicle(String name, View view) {
        Vehicle vehicle = new Vehicle();
        vehicle.setName(name);

        final Dao<Vehicle, Integer> vehicleDao;

        try {
            vehicleDao = getHelper().getVehicleDao();
            vehicleDao.create(vehicle);
            mAdapter.refreshItems(this);
        } catch (SQLException e) {
            String status;
            if (e.getCause().getCause().getMessage().contains("UNIQUE")) {
                status = "Cannot create duplicate vehicle";
            } else {
                status = "Unexpected error. See logs for details.";
            }
            Snackbar.make(view, "ERROR: " + status, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            e.printStackTrace();
        }
    }

    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this,DatabaseHelper.class);
        }
        return databaseHelper;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.vehicle_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Vehicle clickedVehicle = mAdapter.getItem(position);
        log.debug("shortClickedItem : " + clickedVehicle);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PREFS_VEHICLE_ID_KEY, clickedVehicle.getId());
        editor.commit();

        Snackbar.make(view, "Clicked " + clickedVehicle.getName(), Snackbar.LENGTH_SHORT);
        //TODO
//        Intent i = new Intent(this, CarDataActivity.class);
//        i.putExtra(EXTRA_ADDED_CAR, clickedCar);
//        startActivityForResult(i, REQUEST_CODE_DELETED_CAR);
    }
}

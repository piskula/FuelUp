package sk.piskula.fuelup.screens;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.adapters.ListVehiclesAdapter;
import sk.piskula.fuelup.data.DatabaseHelper;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.entity.VehicleType;
import sk.piskula.fuelup.entity.enums.DistanceUnit;
import sk.piskula.fuelup.screens.edit.AddVehicle;

public class VehicleList extends AppCompatActivity
        implements OnNavigationItemSelectedListener {

    private static final String TAG = "VehicleList";

    private static final String SHARED_PREFERENCES_NAME = "sk.piskula.fuelup.preferences";
    private static final String PREFS_VEHICLE_ID_KEY = "vehicle_id";
    public static final String EXTRA_ADDED_CAR = "extra_key_added_car";

    private DatabaseHelper databaseHelper = null;

    private SharedPreferences sharedPreferences;

    private ListView listView;
    private ListVehiclesAdapter adapter;

    private void initVehicleList() {
        listView = (ListView) findViewById(R.id.list_cars);
        listView.setVisibility(View.VISIBLE);

        adapter = new ListVehiclesAdapter(this);
        listView.setAdapter(adapter);

        listView.setFocusable(false);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent i = new Intent(view.getContext(), VehicleTabbedDetail.class);
                i.putExtra(EXTRA_ADDED_CAR, adapter.getItem(position));
                startActivity(i);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);

        //TODO open last viewed car if possible

        setContentView(R.layout.vehicle_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initVehicleList();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(createAddNewVehicleFloatingButton());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onRestart() {
        adapter.refreshItems(this);
        super.onRestart();
    }

    private View.OnClickListener createAddNewVehicleFloatingButton() {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final Dialog dialog = createVehicleDialog();
                dialog.setContentView(R.layout.create_vehicle_dialog);
                dialog.setTitle("Create Vehicle");

                final Button advancedButton = dialog.findViewById(R.id.createVehicleDialog_advanced);
                final Button okButton = dialog.findViewById(R.id.createVehicleDialog_ok);
                final EditText nameTextView = dialog.findViewById(R.id.createVehicleDialog_name);

                okButton.setEnabled(false);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveVehicle(nameTextView.getText().toString(), view);
                        dialog.dismiss();
                    }
                });

                nameTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean hasFocus) {
                        if (hasFocus) {
                            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        }
                    }
                });
                nameTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence,int i,int i1, int i2) {}
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (editable.toString().isEmpty()) {
                            okButton.setEnabled(false);
                        } else {
                            okButton.setEnabled(true);
                        }
                    }
                });

                advancedButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Intent i = new Intent(view.getContext(), AddVehicle.class);
                        startActivity(i);
                    }
                });
                dialog.show();
            }
        };
    }



    private Dialog createVehicleDialog() {
        return new Dialog(this);
    }

    private void saveVehicle(String name, View view) {
        Vehicle vehicle = new Vehicle();
        vehicle.setName(name);
        vehicle.setUnit(DistanceUnit.km);

        try {
            VehicleType type = getHelper().getVehicleTypeDao().queryBuilder().queryForFirst();
            vehicle.setType(type);
            getHelper().getVehicleDao().create(vehicle);
            adapter.refreshItems(this);
        } catch (SQLException e) {
            String status;
            if (e.getCause().getCause().getMessage().contains("UNIQUE")) {
                status = "Cannot create duplicate vehicle";
            } else {
                status = "Unexpected error. See logs for details.";
            }
            Snackbar.make(view, "ERROR: " + status, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            Log.e(TAG, status, e);
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

}

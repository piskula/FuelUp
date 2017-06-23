package sk.piskula.fuelup.screens;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.adapters.ListVehiclesAdapter;
import sk.piskula.fuelup.business.VehicleService;
import sk.piskula.fuelup.screens.dialog.CreateVehicleDialog;
import sk.piskula.fuelup.screens.edit.AddVehicle;

public class VehicleList extends AppCompatActivity
        implements OnNavigationItemSelectedListener, View.OnClickListener, ListView.OnItemClickListener,
        CreateVehicleDialog.Callback {

    private static final String TAG = "VehicleList";

    private static final String SHARED_PREFERENCES_NAME = "sk.piskula.fuelup.preferences";
    private static final String PREFS_VEHICLE_ID_KEY = "vehicle_id";
    public static final String EXTRA_ADDED_CAR = "extra_key_added_car";

    private FloatingActionButton addCarBtn;

    private ListView listView;
    private ListVehiclesAdapter adapter;

    private VehicleService vehicleService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vehicleService = new VehicleService(this);
        //TODO open last viewed car if possible

        setContentView(R.layout.vehicle_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        adapter = new ListVehiclesAdapter(this, (TextView) findViewById(R.id.txt_not_vehicle));

        listView = (ListView) findViewById(R.id.list_cars);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        addCarBtn = (FloatingActionButton) findViewById(R.id.fab_add_vehicle);
        addCarBtn.setOnClickListener(this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onRestart() {
        adapter.refreshItems(this);
        super.onRestart();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Intent i = new Intent(view.getContext(), VehicleTabbedDetail.class);
        i.putExtra(EXTRA_ADDED_CAR, adapter.getItem(position));
        startActivity(i);
    }

    @Override
    public void onClick(final View view) {
        if (view.getId() == addCarBtn.getId()) {
            new CreateVehicleDialog().show(getSupportFragmentManager(), CreateVehicleDialog.class.getSimpleName());
        }
    }

    @Override
    public void onDialogCreateBtnClick(CreateVehicleDialog dialog, Editable vehicleName) {
        vehicleService.save(vehicleName.toString());
        adapter.refreshItems(this);
        dialog.dismiss();
    }

    @Override
    public void onDialogAdvancedBtnClick(CreateVehicleDialog dialog, Editable vehicleName) {
        dialog.dismiss();
        Intent i = new Intent(this, AddVehicle.class);
        i.putExtra("vehicleName", vehicleName.toString());
        startActivity(i);
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
        int id = item.getItemId();

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

}

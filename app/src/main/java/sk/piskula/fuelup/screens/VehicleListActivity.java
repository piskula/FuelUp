package sk.piskula.fuelup.screens;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.adapters.ListVehiclesAdapter;
import sk.piskula.fuelup.business.ServiceResult;
import sk.piskula.fuelup.business.VehicleService;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.loaders.VehicleLoader;
import sk.piskula.fuelup.screens.dialog.CreateVehicleDialog;
import sk.piskula.fuelup.screens.edit.AddVehicleActivity;

public class VehicleListActivity extends AppCompatActivity implements OnNavigationItemSelectedListener, ListVehiclesAdapter.Callback,
        View.OnClickListener, CreateVehicleDialog.Callback, LoaderManager.LoaderCallbacks<List<Vehicle>> {

    private static final String TAG = "VehicleListActivity";
    public static final int VEHICLE_ACTION_REQUEST_CODE = 33;

    public static final String EXTRA_ADDED_CAR = "extra_key_added_car";

    private FloatingActionButton addCarBtn;

    private RecyclerView recyclerView;
    private ListVehiclesAdapter adapter;

    private TextView txtNoVehicle;

    private VehicleService vehicleService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vehicleService = new VehicleService(this);
        //TODO open last viewed car if possible

        setContentView(R.layout.fragment_vehicle_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        if (adapter == null)
            adapter = new ListVehiclesAdapter(this);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 1);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                ((LinearLayoutManager) recyclerView.getLayoutManager()).getOrientation()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.setOnClickListener(this);

        txtNoVehicle = (TextView) findViewById(R.id.txt_noVehicle);

        addCarBtn = (FloatingActionButton) findViewById(R.id.fab_add_vehicle);
        addCarBtn.setOnClickListener(this);

        getSupportLoaderManager().initLoader(VehicleLoader.ID, savedInstanceState, this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        adapter.dataChange((new VehicleService(getApplicationContext())).findAll());
        if (adapter.getItemCount() == 0)
            txtNoVehicle.setVisibility(View.VISIBLE);
        else
            txtNoVehicle.setVisibility(View.GONE);
        super.onResume();
    }

    @Override
    public void onItemClick(View v, Vehicle vehicle, int position) {
        Intent i = new Intent(this, VehicleTabbedDetailActivity.class);
        i.putExtra(EXTRA_ADDED_CAR, vehicle);
        startActivityForResult(i, VEHICLE_ACTION_REQUEST_CODE);
    }

    @Override
    public void onClick(final View view) {
        if (view.getId() == addCarBtn.getId()) {
            new CreateVehicleDialog().show(getSupportFragmentManager(), CreateVehicleDialog.class.getSimpleName());
        }
    }

    @Override
    public void onDialogCreateBtnClick(CreateVehicleDialog dialog, Editable vehicleName) {
        ServiceResult serviceResult = vehicleService.save(vehicleName.toString());
        if (ServiceResult.SUCCESS.equals(serviceResult)) {
            adapter.dataChange((new VehicleService(getApplicationContext())).findAll());
            dialog.dismiss();
            Snackbar.make(findViewById(android.R.id.content), R.string.addVehicle_success, Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(findViewById(android.R.id.content), R.string.addVehicle_fail, Snackbar.LENGTH_LONG).show();
        }

    }

    @Override
    public void onDialogAdvancedBtnClick(CreateVehicleDialog dialog, Editable vehicleName) {
        dialog.dismiss();
        Intent i = new Intent(this, AddVehicleActivity.class);
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

    @Override
    public Loader<List<Vehicle>> onCreateLoader(int id, Bundle args) {
        return new VehicleLoader(getApplicationContext(), new VehicleService(this));
    }

    @Override
    public void onLoadFinished(Loader<List<Vehicle>> loader, List<Vehicle> data) {
        adapter.dataChange(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Vehicle>> loader) {
        return;
    }
}

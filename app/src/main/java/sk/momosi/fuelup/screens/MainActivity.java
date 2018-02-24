package sk.momosi.fuelup.screens;

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
import android.view.MenuItem;

import sk.momosi.fuelup.R;

public class MainActivity extends AppCompatActivity implements OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static MainActivity singleton;

    public static MainActivity getInstance() {
        return singleton;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.vehicle_list);
            getSupportFragmentManager().beginTransaction().replace(R.id.activty_main_frame, new VehicleListFragment(), TAG).commit();
        }

        singleton = this;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}

package sk.momosi.fuelup.screens;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import sk.momosi.fuelup.R;
import sk.momosi.fuelup.business.googledrive.authenticator.AccountService;

public class MainActivity extends AppCompatActivity implements OnNavigationItemSelectedListener {

    private static MainActivity singleton;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String IS_FIRST_RUN = "is_this_fisrt_run";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.vehicle_list);
            getSupportFragmentManager().beginTransaction().replace(R.id.activty_main_frame, new VehicleListFragment(), TAG).commit();
        }

        // initialize with data when first running
        SharedPreferences settings = getSharedPreferences(IS_FIRST_RUN, 0);
        if (settings.getBoolean("my_first_time", true)) {
            // if you want to fill Dummy Vehicle with data
            // SampleDataUtils.initializeWhenFirstRun(getApplicationContext());

            Account genericAccount = AccountService.getAccount();

            AccountManager accountManager = (AccountManager) this.getSystemService(Context.ACCOUNT_SERVICE);
            accountManager.addAccountExplicitly(genericAccount, null, null);

            settings.edit().putBoolean("my_first_time", false).apply();
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
        Fragment currentFragment;
        if (id == R.id.vehicle_list) {
            currentFragment = new VehicleListFragment();
        } else if (id == R.id.about) {
            currentFragment = new AboutFragment();
        } else if (id == R.id.google_drive) {
            currentFragment = new BackupFragment();
        } else if (id == R.id.faq) {
            currentFragment = new FaqFragment();
        } else {
            throw new RuntimeException("onNavigationItemSelected unhandled case");
        }
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activty_main_frame, currentFragment, TAG).commit();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static MainActivity getInstance() {
        return singleton;
    }

    public void toggleAnswer(View v) {
        TextView answer = (TextView) ((LinearLayout) v).getChildAt(1);
        ImageView arrow = (ImageView) ((LinearLayout)((LinearLayout) v).getChildAt(0)).getChildAt(1);
        if (answer.getVisibility() == View.VISIBLE) {
            answer.setVisibility(View.GONE);
            arrow.setImageResource(R.drawable.ic_keyboard_arrow_left_black_24dp);
        } else {
            answer.setVisibility(View.VISIBLE);
            arrow.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
        }
    }
}

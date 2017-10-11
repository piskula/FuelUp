package sk.momosi.fuelup.screens;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import sk.momosi.fuelup.R;
import sk.momosi.fuelup.adapters.PagerStatisticsAdapter;
import sk.momosi.fuelup.entity.Vehicle;

/**
 * @author Martin Styk
 */
public class VehicleStatisticsActivity extends AppCompatActivity {

    public static final String VEHICLE_TO_ADVANCED_STATISTICS = "VEHICLE_TO_ADVANCED_STATISTICS";

    private PagerStatisticsAdapter adapter;
    private ViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_statistics);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.statistics_title);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent incomingIntent = getIntent();
        Vehicle vehicle = incomingIntent.getParcelableExtra(VEHICLE_TO_ADVANCED_STATISTICS);

        adapter = new PagerStatisticsAdapter(getSupportFragmentManager(), vehicle);

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

}

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

        ViewPager viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(new PagerStatisticsAdapter(getSupportFragmentManager(), vehicle));

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

}

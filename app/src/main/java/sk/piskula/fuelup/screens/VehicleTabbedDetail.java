package sk.piskula.fuelup.screens;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.screens.detailfragments.ExpensesListFragment;
import sk.piskula.fuelup.screens.detailfragments.FillUpsListFragment;
import sk.piskula.fuelup.screens.detailfragments.StatisticsFragment;

/**
 * @author Ondrej Oravcok
 * @version 17.6.2017
 */
public class VehicleTabbedDetail extends AppCompatActivity {

    private static final String TAG = "VehicleTabbedDetail";

    public static final String VEHICLE_TO_FRAGMENT = "fragment-vehicle";

    private BottomNavigationView navigation;
    private Fragment fragment;
    private FragmentManager fragmentManager;
    private Vehicle vehicle;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(VEHICLE_TO_FRAGMENT, vehicle);
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
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vehicle_tabbed_detail);

        Intent intent = getIntent();
        vehicle = (Vehicle) intent.getSerializableExtra(VehicleList.EXTRA_ADDED_CAR);

        fragmentManager = getSupportFragmentManager();

        navigation = (BottomNavigationView) findViewById(R.id.vehicle_detail_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_fillUps);
    }

}

package sk.piskula.fuelup.screens;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.screens.detailfragments.ExpensesListFragment;
import sk.piskula.fuelup.screens.detailfragments.FillUpsListFragment;
import sk.piskula.fuelup.screens.detailfragments.StatisticsFragment;

import static android.R.attr.id;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.vehicle_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.btn_vehicle_remove) {
            final AlertDialog confirmDialog = confirmDeletion();
            confirmDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Button negative = confirmDialog.getButton(confirmDialog.BUTTON_NEGATIVE);
//                    negative.setBackground(ContextCompat.getDrawable(VehicleTabbedDetail.this, R.drawable.orange_button));
//                    negative.setTextColor(Color.parseColor("#FFFFFF"));
                    negative.setFocusable(true);
                    negative.setFocusableInTouchMode(true);
                    negative.requestFocus();
                }
            });
            confirmDialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private AlertDialog confirmDeletion()
    {
        return new AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle("Delete")
                .setMessage("Do you want to completely remove vehicle '" + vehicle.getName()
                        + "' and all its data? You can never get it back.")
                .setIcon(R.drawable.tow)
                .setPositiveButton("Yes, delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //your deleting code
                        dialog.dismiss();
                    }
                })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }
}

package sk.piskula.fuelup.screens;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import sk.piskula.fuelup.R;

/**
 * @author Ondrej Oravcok
 * @version 17.6.2017
 */
public class VehicleTabbedDetail extends AppCompatActivity {

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_fillUps:
                    mTextMessage.setText(R.string.title_fillUps);
                    return true;
                case R.id.navigation_expenses:
                    mTextMessage.setText(R.string.title_expenses);
                    return true;
                case R.id.navigation_statistics:
                    mTextMessage.setText(R.string.title_statistics);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vehicle_tabbed_detail);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}

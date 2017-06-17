package sk.piskula.fuelup.screens.detailfragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.adapters.ListFillUpsAdapter;
import sk.piskula.fuelup.data.DatabaseHelper;
import sk.piskula.fuelup.entity.FillUp;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.screens.VehicleTabbedDetail;

/**
 * @author Ondrej Oravcok
 * @version 17.6.2017
 */
public class FillUpsListFragment extends ListFragment {

    private static final String TAG = "FillUpsListFragment";

    private Bundle args;
    private Vehicle vehicle;
    private List<FillUp> listFillUps;
    private ListFillUpsAdapter adapter;

    private DatabaseHelper databaseHelper = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        args = getArguments();
        if (args != null) {
            vehicle = (Vehicle) args.getSerializable(VehicleTabbedDetail.VEHICLE_TO_FRAGMENT);
            listFillUps = fillUpsOfVehicle(vehicle);
        }

        return inflater.inflate(R.layout.fillups_list, container, false);
    }

    private List<FillUp> fillUpsOfVehicle(Vehicle vehicle) {
        try {
            return getHelper().getFillUpDao().queryBuilder().where().eq("vehicle_id", vehicle.getId()).query();
        } catch (SQLException e) {
            Log.e(TAG, "Error getting fillUps from DB for vehicle " + vehicle, e);
            return new ArrayList<>();
        }
    }

    private void refreshList(){
        if (args != null) {
            vehicle = (Vehicle) args.getSerializable(VehicleTabbedDetail.VEHICLE_TO_FRAGMENT);
        }
        if (vehicle != null) {
            listFillUps = fillUpsOfVehicle(vehicle);
            adapter = new ListFillUpsAdapter(getActivity(), listFillUps);
            setListAdapter(adapter);
        }
    }

    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
        }
        return databaseHelper;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    @Override
    public void onResume() {
        refreshList();
        super.onResume();
    }


}

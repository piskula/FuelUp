package sk.piskula.fuelup.screens.detailfragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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

        View view = inflater.inflate(R.layout.fillups_list, container, false);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_fillup);
        fab.setOnClickListener(addNewFillUpFloatingButton());

        return view;
    }

    private View.OnClickListener addNewFillUpFloatingButton() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Add new FillUp", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        };
    }

    @Override
    public void onListItemClick(ListView list, View v, int position, long id) {
        super.onListItemClick(list, v, position, id);
        Snackbar.make(list, "update fillup", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
//        FillUp clickedFillUp = adapter.getItem(position);
//        Intent i = new Intent(getActivity(), AddFillUpActivity.class);
//        i.putExtra(AddFillUpActivity.EXTRA_FILLUP, clickedFillUp);
//        startActivityForResult(i, REQUEST_CODE_UPDATE_FILLUP);
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

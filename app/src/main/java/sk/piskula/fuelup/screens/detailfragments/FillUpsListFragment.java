package sk.piskula.fuelup.screens.detailfragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.adapters.ListFillUpsAdapter1;
import sk.piskula.fuelup.data.DatabaseHelper;
import sk.piskula.fuelup.entity.FillUp;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.screens.VehicleTabbedDetail;

/**
 * @author Ondrej Oravcok
 * @version 17.6.2017
 */
public class FillUpsListFragment extends Fragment implements ListFillUpsAdapter1.Callback {

    private static final String TAG = "FillUpsListFragment";

    private Bundle args;
    private Vehicle vehicle;
    private List<FillUp> listFillUps;
    private ListFillUpsAdapter1 adapter;

    private RecyclerView recyclerView;

    private DatabaseHelper databaseHelper = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        args = getArguments();
        if (args != null) {
            vehicle = (Vehicle) args.getSerializable(VehicleTabbedDetail.VEHICLE_TO_FRAGMENT);
            listFillUps = fillUpsOfVehicle(vehicle);
        }

        View view = inflater.inflate(R.layout.fillups_list, container, false);

        recyclerView = view.findViewById(R.id.fill_ups_list);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                ((LinearLayoutManager) recyclerView.getLayoutManager()).getOrientation()));


        adapter = new ListFillUpsAdapter1(this, listFillUps);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private List<FillUp> fillUpsOfVehicle(Vehicle vehicle) {
        try {
            return getHelper().getFillUpDao().queryBuilder().where().eq("vehicle_id", vehicle.getId()).query();
        } catch (SQLException e) {
            Log.e(TAG, "Error getting fillUps from DB for vehicle " + vehicle, e);
            return new ArrayList<>();
        }
    }

    private void refreshList() {
        if (args != null) {
            vehicle = (Vehicle) args.getSerializable(VehicleTabbedDetail.VEHICLE_TO_FRAGMENT);
        }
        if (vehicle != null) {
            listFillUps = fillUpsOfVehicle(vehicle);
            adapter.dataChange(listFillUps);
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


    @Override
    public void onItemClick(View v, FillUp fillUp, int position) {
        // TODO this is called when item is clicked
        Snackbar.make(v, "update fillup", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }
}

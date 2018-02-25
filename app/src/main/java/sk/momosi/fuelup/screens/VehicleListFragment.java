package sk.momosi.fuelup.screens;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import sk.momosi.fuelup.R;
import sk.momosi.fuelup.adapters.ListVehiclesAdapter;
import sk.momosi.fuelup.business.VehicleService;
import sk.momosi.fuelup.data.FuelUpContract;
import sk.momosi.fuelup.data.FuelUpContract.VehicleEntry;
import sk.momosi.fuelup.screens.edit.AddVehicleActivity;

public class VehicleListFragment extends Fragment implements ListVehiclesAdapter.Callback,
        View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_ADDED_VEHICLE_ID = "extra_key_added_car";
    private static final String TAG = VehicleListFragment.class.getSimpleName();
    private static final int VEHICLE_ACTION_REQUEST_CODE = 33;
    private static final int VEHICLE_LOADER_ID = 712;

    private FloatingActionButton addCarBtn;

    private ListVehiclesAdapter adapter;

    private RecyclerView recyclerView;
    private TextView txtNoVehicle;

    // In case of only one vehicle, go directly to vehicle details. We want to do this only on initial
    // data load. This allows us to use back button on vehicle details, and show vehicles list without
    // immediate redirect bac to vehicle details.
    private boolean redirectToSingleVehicle = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        adapter = new ListVehiclesAdapter(getContext(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_vehicle_list, container, false);

        recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                ((LinearLayoutManager) recyclerView.getLayoutManager()).getOrientation()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        txtNoVehicle = rootView.findViewById(R.id.txt_noVehicle);

        addCarBtn = getActivity().findViewById(R.id.fab_add_vehicle);
        addCarBtn.setOnClickListener(this);

        getActivity().findViewById(R.id.fab_add_vehicle).setVisibility(View.VISIBLE);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(VEHICLE_LOADER_ID, null, this);
    }

    @Override
    public void onClick(final View view) {
        if (view.getId() == addCarBtn.getId()) {
            startActivity(new Intent(getContext(), AddVehicleActivity.class));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(),
                VehicleEntry.CONTENT_URI,
                FuelUpContract.ALL_COLUMNS_VEHICLES,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            txtNoVehicle.setVisibility(View.VISIBLE);
        }
        else if (data.getCount() == 1 && redirectToSingleVehicle) {
            redirectToSingleVehicle = false;
            openVehicleDetail(VehicleService.getAvailableVehicleIds(getContext()).get(0));
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            txtNoVehicle.setVisibility(View.GONE);
        }
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onItemClick(long vehicleId) {
        Log.i(TAG, "Clicked vehicle id=" + vehicleId);
        openVehicleDetail(vehicleId);
    }

    private void openVehicleDetail(long vehicleId){
        Intent i = new Intent(getActivity(), VehicleTabbedDetailActivity.class);
        i.putExtra(EXTRA_ADDED_VEHICLE_ID, vehicleId);
        startActivityForResult(i, VEHICLE_ACTION_REQUEST_CODE);
    }
}

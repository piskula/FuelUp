package sk.piskula.fuelup.screens;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.adapters.ListVehiclesAdapter;
import sk.piskula.fuelup.data.FuelUpContract;
import sk.piskula.fuelup.data.FuelUpContract.VehicleEntry;
import sk.piskula.fuelup.screens.dialog.CreateVehicleDialog;
import sk.piskula.fuelup.screens.edit.AddVehicleActivity;

public class VehicleListFragment extends Fragment implements ListVehiclesAdapter.Callback,
        View.OnClickListener, CreateVehicleDialog.Callback, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = VehicleListFragment.class.getSimpleName();


    public static final int VEHICLE_ACTION_REQUEST_CODE = 33;
    public static final String EXTRA_ADDED_VEHICLE_ID = "extra_key_added_car";
    private static final int VEHICLE_LOADER = 712;

    private FloatingActionButton addCarBtn;

    private ListVehiclesAdapter adapter;

    private RecyclerView recyclerView;
    private TextView txtNoVehicle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        adapter = new ListVehiclesAdapter(this);
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

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(VEHICLE_LOADER, null, this);
    }

    @Override
    public void onClick(final View view) {
        if (view.getId() == addCarBtn.getId()) {
            new CreateVehicleDialog().show(getActivity().getSupportFragmentManager(), CreateVehicleDialog.class.getSimpleName());
        }
    }

    @Override
    public void onDialogCreateBtnClick(CreateVehicleDialog dialog, Editable vehicleName) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(VehicleEntry.COLUMN_NAME, vehicleName.toString());
        contentValues.put(VehicleEntry.COLUMN_VOLUME_UNIT, "LITRE");
        contentValues.put(VehicleEntry.COLUMN_CURRENCY, "EUR");
        contentValues.put(VehicleEntry.COLUMN_TYPE, 1);

        ContentResolver resolver = getActivity().getContentResolver();
        if (resolver.insert(VehicleEntry.CONTENT_URI, contentValues) == null) {
            Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.addVehicle_fail, Snackbar.LENGTH_LONG).show();
        } else {
            dialog.dismiss();
            Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.addVehicle_success, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDialogAdvancedBtnClick(CreateVehicleDialog dialog, Editable vehicleName) {
        dialog.dismiss();
        Intent i = new Intent(getContext(), AddVehicleActivity.class);
        i.putExtra("vehicleName", vehicleName.toString());
        startActivity(i);
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
        Intent i = new Intent(getActivity(), VehicleTabbedDetailActivity.class);
        i.putExtra(EXTRA_ADDED_VEHICLE_ID, vehicleId);
        Log.i(TAG, "Clicked vehicle id=" + vehicleId);
        startActivityForResult(i, VEHICLE_ACTION_REQUEST_CODE);
    }
}

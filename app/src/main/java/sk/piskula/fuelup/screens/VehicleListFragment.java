package sk.piskula.fuelup.screens;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
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

import java.util.List;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.adapters.ListVehiclesAdapter;
import sk.piskula.fuelup.business.ServiceResult;
import sk.piskula.fuelup.business.VehicleService;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.loaders.VehicleLoader;
import sk.piskula.fuelup.screens.dialog.CreateVehicleDialog;
import sk.piskula.fuelup.screens.edit.AddVehicleActivity;

public class VehicleListFragment extends Fragment implements ListVehiclesAdapter.Callback,
        View.OnClickListener, CreateVehicleDialog.Callback, LoaderManager.LoaderCallbacks<List<Vehicle>> {

    private static final String TAG = VehicleListFragment.class.getSimpleName();


    public static final int VEHICLE_ACTION_REQUEST_CODE = 33;
    public static final String EXTRA_ADDED_CAR = "extra_key_added_car";

    private FloatingActionButton addCarBtn;

    private RecyclerView recyclerView;
    private ListVehiclesAdapter adapter;

    private TextView txtNoVehicle;

    private VehicleService vehicleService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vehicleService = new VehicleService(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_vehicle_list, container, false);

        adapter = new ListVehiclesAdapter(this);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 1);

        recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                ((LinearLayoutManager) recyclerView.getLayoutManager()).getOrientation()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.setOnClickListener(this);

        txtNoVehicle = rootView.findViewById(R.id.txt_noVehicle);

        addCarBtn = getActivity().findViewById(R.id.fab_add_vehicle);
        addCarBtn.setOnClickListener(this);

        getActivity().getSupportLoaderManager().initLoader(VehicleLoader.ID, savedInstanceState, this);

        return rootView;

    }

    @Override
    public void onResume() {
        adapter.dataChange((new VehicleService(getActivity().getApplicationContext())).findAll());
        if (adapter.getItemCount() == 0)
            txtNoVehicle.setVisibility(View.VISIBLE);
        else
            txtNoVehicle.setVisibility(View.GONE);
        super.onResume();
    }

    @Override
    public void onClick(final View view) {
        if (view.getId() == addCarBtn.getId()) {
            new CreateVehicleDialog().show(getActivity().getSupportFragmentManager(), CreateVehicleDialog.class.getSimpleName());
        }
    }

    @Override
    public void onItemClick(View v, Vehicle vehicle, int position) {
        Intent i = new Intent(getActivity(), VehicleTabbedDetailActivity.class);
        i.putExtra(EXTRA_ADDED_CAR, vehicle);
        Log.i(TAG, "Clicked vehicle " + vehicle);
        startActivityForResult(i, VEHICLE_ACTION_REQUEST_CODE);
    }

    @Override
    public void onDialogCreateBtnClick(CreateVehicleDialog dialog, Editable vehicleName) {
        ServiceResult serviceResult = vehicleService.save(vehicleName.toString());
        if (ServiceResult.SUCCESS.equals(serviceResult)) {
            adapter.dataChange((new VehicleService(getContext().getApplicationContext())).findAll());
            dialog.dismiss();
            Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.addVehicle_success, Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.addVehicle_fail, Snackbar.LENGTH_LONG).show();
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
    public Loader<List<Vehicle>> onCreateLoader(int id, Bundle args) {
        return new VehicleLoader(getActivity().getApplicationContext(), new VehicleService(getContext()));
    }

    @Override
    public void onLoadFinished(Loader<List<Vehicle>> loader, List<Vehicle> data) {
        adapter.dataChange(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Vehicle>> loader) {
        return;
    }

}

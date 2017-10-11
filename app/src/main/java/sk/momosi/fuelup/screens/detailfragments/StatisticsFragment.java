package sk.momosi.fuelup.screens.detailfragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import sk.momosi.fuelup.R;
import sk.momosi.fuelup.business.StatisticsService;
import sk.momosi.fuelup.databinding.FragmentStatisticsBinding;
import sk.momosi.fuelup.entity.Vehicle;
import sk.momosi.fuelup.entity.dto.StatisticsDTO;
import sk.momosi.fuelup.loaders.StatisticsLoader;
import sk.momosi.fuelup.screens.VehicleStatisticsActivity;
import sk.momosi.fuelup.screens.VehicleTabbedDetailActivity;

import static sk.momosi.fuelup.screens.VehicleTabbedDetailActivity.VEHICLE_TO_FRAGMENT;

/**
 * @author Ondrej Oravcok
 * @version 17.6.2017
 */
public class StatisticsFragment extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<StatisticsDTO> {

    private Vehicle vehicle;

    private FragmentStatisticsBinding binding;

    private CollapsingToolbarLayout appBarLayout;
    private FloatingActionButton floatingActionButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentStatisticsBinding.inflate(inflater);

        Bundle args = getArguments();
        if (args != null) {
            vehicle = args.getParcelable(VehicleTabbedDetailActivity.VEHICLE_TO_FRAGMENT);
        }

        appBarLayout = getActivity().findViewById(R.id.toolbar_layout);
        appBarLayout.setTitle(getResources().getString(R.string.title_statistics));

        getActivity().findViewById(R.id.fab_add).setVisibility(View.GONE);
        floatingActionButton = getActivity().findViewById(R.id.fab_charts);
        floatingActionButton.setVisibility(View.VISIBLE);
        floatingActionButton.setOnClickListener(this);


        binding.setVehicle(vehicle);
        binding.setCurrency(vehicle.getCurrencySymbol());

        getLoaderManager().initLoader(StatisticsLoader.ID, args, this);

        setHasOptionsMenu(true);

        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.getItem(0).setVisible(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        floatingActionButton.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == floatingActionButton.getId()) {
            Intent i = new Intent(getActivity(), VehicleStatisticsActivity.class);
            i.putExtra(VehicleStatisticsActivity.VEHICLE_TO_ADVANCED_STATISTICS, vehicle);
            startActivity(i);
        }
    }

    @Override
    public Loader<StatisticsDTO> onCreateLoader(int id, Bundle args) {
        Vehicle vehicle = args.getParcelable(VEHICLE_TO_FRAGMENT);
        long vehicleId = vehicle.getId();
        return new StatisticsLoader(getActivity(), new StatisticsService(getContext(), vehicleId));
    }

    @Override
    public void onLoadFinished(Loader<StatisticsDTO> loader, StatisticsDTO data) {
        binding.setData(data);
    }

    @Override
    public void onLoaderReset(Loader<StatisticsDTO> loader) {
    }
}

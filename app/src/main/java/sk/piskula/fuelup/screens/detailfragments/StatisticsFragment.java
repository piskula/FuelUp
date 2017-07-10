package sk.piskula.fuelup.screens.detailfragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.business.FillUpService;
import sk.piskula.fuelup.databinding.FragmentStatisticsBinding;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.screens.VehicleStatisticsActivity;
import sk.piskula.fuelup.screens.VehicleTabbedDetailActivity;

/**
 * @author Ondrej Oravcok
 * @version 17.6.2017
 */
public class StatisticsFragment extends Fragment implements View.OnClickListener {

    private Bundle args;
    private Vehicle vehicle;

    private CollapsingToolbarLayout appBarLayout;
    private FloatingActionButton floatingActionButton;

    private FillUpService fillUpService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentStatisticsBinding binding = FragmentStatisticsBinding.inflate(inflater);

        args = getArguments();
        if (args != null) {
            vehicle = args.getParcelable(VehicleTabbedDetailActivity.VEHICLE_TO_FRAGMENT);
        }
        fillUpService = new FillUpService(getActivity());

        appBarLayout = getActivity().findViewById(R.id.toolbar_layout);
        appBarLayout.setTitle(getResources().getString(R.string.title_statistics));

        floatingActionButton = getActivity().findViewById(R.id.fab_add);
        floatingActionButton.setImageResource(android.R.drawable.ic_input_get);
        floatingActionButton.setOnClickListener(this);


        binding.setVehicle(vehicle);

        //TODO add more fields
        setFuelConsumption(binding);
        setPricePerDistance(binding);

        return binding.getRoot();
    }

    private void setFuelConsumption(FragmentStatisticsBinding binding) {
        binding.fuelConsumption.setValue(String.valueOf(fillUpService.getAverageConsumptionOfVehicle(vehicle.getId())));
        binding.fuelConsumption.setUnit("\u2113/" + vehicle.getUnit().toString());
    }

    private void setPricePerDistance(FragmentStatisticsBinding binding) {
        binding.pricePerDistance.setValue("0,45");
        binding.pricePerDistance.setUnit(vehicle.getCurrencySymbol(getActivity()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        floatingActionButton.setImageResource(android.R.drawable.ic_input_add);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == floatingActionButton.getId()) {
            Intent i = new Intent(getActivity(), VehicleStatisticsActivity.class);
            i.putExtra(VehicleStatisticsActivity.VEHICLE_TO_ADVANCED_STATISTICS, vehicle);
            startActivity(i);
        }
    }

}

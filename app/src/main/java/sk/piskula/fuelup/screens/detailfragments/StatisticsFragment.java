package sk.piskula.fuelup.screens.detailfragments;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.screens.VehicleTabbedDetail;

/**
 * @author Ondrej Oravcok
 * @version 17.6.2017
 */
public class StatisticsFragment extends Fragment {

    private Bundle args;
    private Vehicle vehicle;

    private CollapsingToolbarLayout appBarLayout;
    private FloatingActionButton addButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        args = getArguments();
        if (args != null) {
            vehicle = (Vehicle) args.getSerializable(VehicleTabbedDetail.VEHICLE_TO_FRAGMENT);
        }

        appBarLayout = getActivity().findViewById(R.id.toolbar_layout);
        appBarLayout.setTitle(getResources().getString(R.string.title_statistics));

        //hide add button
        addButton = getActivity().findViewById(R.id.fab_add);
        addButton.setVisibility(View.GONE);

        return inflater.inflate(R.layout.fillups_list, container, false);
    }

}

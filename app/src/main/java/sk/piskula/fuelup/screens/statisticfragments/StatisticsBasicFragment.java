package sk.piskula.fuelup.screens.statisticfragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.entity.Vehicle;

/**
 * @author Martin Styk
 */
public class StatisticsBasicFragment extends Fragment {

    private static final String ARG_VEHICLE = "vehicleArg";

    private Vehicle vehicle;

    public static StatisticsBasicFragment newInstance(Vehicle param) {
        StatisticsBasicFragment fragment = new StatisticsBasicFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_VEHICLE, param);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vehicle = getArguments().getParcelable(ARG_VEHICLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_statistics_basic, container, false);

        ((TextView) rootView.findViewById(R.id.placeholder)).setText(vehicle.toString());

        return rootView;
    }


}

package sk.momosi.fuelup.screens;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import sk.momosi.fuelup.BuildConfig;
import sk.momosi.fuelup.R;


public class AboutFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        getActivity().findViewById(R.id.fab_add_vehicle).setVisibility(View.GONE);
        ((TextView) rootView.findViewById(R.id.about_app_version)).setText(BuildConfig.VERSION_NAME);

        return rootView;
    }

}

package sk.momosi.fuelup.screens;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import sk.momosi.fuelup.BuildConfig;
import sk.momosi.fuelup.R;

/**
 * @author Ondro
 * @vresion 31.10.2017
 */
public class FaqFragment extends Fragment {

    private static final String LOG_TAG = FaqFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true);
        View rootview = inflater.inflate(R.layout.fragment_faq, container, false);

        getActivity().findViewById(R.id.fab_add_vehicle).setVisibility(View.GONE);

        return rootview;
    }

}

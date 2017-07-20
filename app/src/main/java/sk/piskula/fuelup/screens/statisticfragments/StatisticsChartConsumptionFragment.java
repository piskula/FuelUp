package sk.piskula.fuelup.screens.statisticfragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;
import sk.piskula.fuelup.business.FillUpService;
import sk.piskula.fuelup.databinding.FragmentStatisticsChartConsumptionBinding;
import sk.piskula.fuelup.entity.FillUp;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.loaders.ConsumptionChartDataLoader;
import sk.piskula.fuelup.screens.dialog.FillUpInfoDialog;

/**
 * @author Martin Styk
 */
public class StatisticsChartConsumptionFragment extends Fragment implements LoaderManager.LoaderCallbacks<Map<String, Object>> {

    private static final String ARG_VEHICLE = "vehicleArg";

    private FragmentStatisticsChartConsumptionBinding binding;

    private LineChartView lineChartView;

    private List<FillUp> displayedValues = new ArrayList<>(0);


    public static StatisticsChartConsumptionFragment newInstance(Vehicle param) {
        StatisticsChartConsumptionFragment fragment = new StatisticsChartConsumptionFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_VEHICLE, param);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(ConsumptionChartDataLoader.ID, getArguments(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatisticsChartConsumptionBinding.inflate(inflater);
        FrameLayout layout = (FrameLayout) binding.getRoot();

        lineChartView = new LineChartView(getActivity());
        lineChartView.setZoomType(ZoomType.HORIZONTAL);
        lineChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChartView.setOnValueTouchListener(new ValueTouchListener());
        lineChartView.setVisibility(View.GONE);

        layout.addView(lineChartView);

        return layout;
    }

    @Override
    public Loader<Map<String, Object>> onCreateLoader(int id, Bundle args) {
        Vehicle vehicle = args.getParcelable(ARG_VEHICLE);
        long vehicleId = vehicle.getId();
        return new ConsumptionChartDataLoader(getActivity(), vehicleId, new FillUpService(getActivity()));
    }

    @Override
    public void onLoadFinished(Loader<Map<String, Object>> loader, Map<String, Object> data) {
        binding.setHasData(data != null);
        if (data != null) {
            lineChartView.setLineChartData((LineChartData) data.get(ConsumptionChartDataLoader.CHART_DATA));
            lineChartView.setVisibility(View.VISIBLE);

            displayedValues = (List<FillUp>) data.get(ConsumptionChartDataLoader.FILL_UPS);
        }
    }

    @Override
    public void onLoaderReset(Loader<Map<String, Object>> loader) {
        lineChartView.setLineChartData(new LineChartData());
    }

    private boolean hasSomethingToDisplay(List<FillUp> data) {
        for (FillUp fillUp : data) {
            if (fillUp.getFuelConsumption() != null) {
                return true;
            }
        }
        return false;
    }

    private class ValueTouchListener implements LineChartOnValueSelectListener {

        @Override
        public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
            FillUpInfoDialog.newInstance(displayedValues.get(pointIndex))
                    .show(getActivity().getSupportFragmentManager(), FillUpInfoDialog.class.getSimpleName());
        }

        @Override
        public void onValueDeselected() {
        }

    }

}

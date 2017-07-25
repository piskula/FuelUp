package sk.piskula.fuelup.screens.statisticfragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.ColumnChartView;
import sk.piskula.fuelup.business.FillUpService;
import sk.piskula.fuelup.databinding.FragmentStatisticsChartConsumptionPerTimeBinding;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.loaders.ConsumptionPerMonthChartDataLoader;

/**
 * @author Martin Styk
 */
public class StatisticsChartConsumptionPerTimeFragment extends Fragment implements LoaderManager.LoaderCallbacks<ColumnChartData> {

    private static final String ARG_VEHICLE = "vehicleArg";

    private FragmentStatisticsChartConsumptionPerTimeBinding binding;

    private ColumnChartView chart;

    public static StatisticsChartConsumptionPerTimeFragment newInstance(Vehicle param) {
        StatisticsChartConsumptionPerTimeFragment fragment = new StatisticsChartConsumptionPerTimeFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_VEHICLE, param);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(ConsumptionPerMonthChartDataLoader.ID, getArguments(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatisticsChartConsumptionPerTimeBinding.inflate(inflater);

        chart = binding.chart;
        chart.setZoomEnabled(true);
        chart.setScrollEnabled(true);
        chart.setZoomType(ZoomType.HORIZONTAL);
        chart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);

        return binding.getRoot();
    }

    @Override
    public Loader<ColumnChartData> onCreateLoader(int id, Bundle args) {
        Vehicle vehicle = args.getParcelable(ARG_VEHICLE);
        long vehicleId = vehicle.getId();
        return new ConsumptionPerMonthChartDataLoader(getActivity(), vehicleId, new FillUpService(getActivity()));
    }

    @Override
    public void onLoadFinished(Loader<ColumnChartData> loader, ColumnChartData data) {
        binding.setHasData(data != null);
        if (data != null) {
            chart.setColumnChartData(data);
            setViewport();
        }
    }

    private void setViewport() {
        final Viewport maxViewPort = chart.getMaximumViewport();
        final Viewport v = new Viewport(maxViewPort);
        v.top = 1.05f * maxViewPort.top;
        chart.setMaximumViewport(v);
        chart.setCurrentViewport(v);
    }

    @Override
    public void onLoaderReset(Loader<ColumnChartData> loader) {
        chart.setColumnChartData(new ColumnChartData());
    }
}

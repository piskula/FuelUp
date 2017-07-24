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
import lecho.lib.hellocharts.view.ColumnChartView;
import sk.piskula.fuelup.business.ExpenseService;
import sk.piskula.fuelup.business.FillUpService;
import sk.piskula.fuelup.databinding.FragmentStatisticsChartConsumptionPerTimeBinding;
import sk.piskula.fuelup.databinding.FragmentStatisticsChartCostsPerTimeBinding;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.loaders.ConsumptionPerMonthChartDataLoader;
import sk.piskula.fuelup.loaders.CostsPerMonthChartDataLoader;

/**
 * @author Martin Styk
 */
public class StatisticsChartCostsPerTimeFragment extends Fragment implements LoaderManager.LoaderCallbacks<ColumnChartData> {

    private static final String ARG_VEHICLE = "vehicleArg";

    private FragmentStatisticsChartCostsPerTimeBinding binding;

    private ColumnChartView chart;

    public static StatisticsChartCostsPerTimeFragment newInstance(Vehicle param) {
        StatisticsChartCostsPerTimeFragment fragment = new StatisticsChartCostsPerTimeFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_VEHICLE, param);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(CostsPerMonthChartDataLoader.ID, getArguments(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatisticsChartCostsPerTimeBinding.inflate(inflater);

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
        return new CostsPerMonthChartDataLoader(getActivity(), vehicleId, new FillUpService(getActivity()), new ExpenseService(getActivity()));
    }

    @Override
    public void onLoadFinished(Loader<ColumnChartData> loader, ColumnChartData data) {
        binding.setHasData(data != null);
        if (data != null) {
            chart.setColumnChartData(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<ColumnChartData> loader) {
        chart.setColumnChartData(new ColumnChartData());
    }
}

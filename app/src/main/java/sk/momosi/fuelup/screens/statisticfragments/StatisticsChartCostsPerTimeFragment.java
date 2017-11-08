package sk.momosi.fuelup.screens.statisticfragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.ColumnChartView;
import sk.momosi.fuelup.R;
import sk.momosi.fuelup.business.ExpenseService;
import sk.momosi.fuelup.business.FillUpService;
import sk.momosi.fuelup.databinding.FragmentStatisticsChartCostsPerTimeBinding;
import sk.momosi.fuelup.entity.Vehicle;
import sk.momosi.fuelup.loaders.CostsPerMonthChartDataLoader;
import sk.momosi.fuelup.util.BigDecimalFormatter;

/**
 * @author Martin Styk
 */
public class StatisticsChartCostsPerTimeFragment extends Fragment implements LoaderManager.LoaderCallbacks<ColumnChartData> {

    private static final String ARG_VEHICLE = "vehicleArg";

    private Vehicle vehicle;

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

        vehicle = getArguments().getParcelable(ARG_VEHICLE);

        chart = binding.chart;
        chart.setZoomEnabled(true);
        chart.setScrollEnabled(true);
        chart.setZoomType(ZoomType.HORIZONTAL);
        chart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        chart.setOnValueTouchListener(new ValueTouchListener());

        return binding.getRoot();
    }

    @Override
    public Loader<ColumnChartData> onCreateLoader(int id, Bundle args) {
        Vehicle vehicle = args.getParcelable(ARG_VEHICLE);
        long vehicleId = vehicle.getId();
        return new CostsPerMonthChartDataLoader(getActivity(), vehicleId);
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
        v.right = 1.01f * maxViewPort.right;
        chart.setMaximumViewport(v);
        chart.setCurrentViewport(v);
    }

    @Override
    public void onLoaderReset(Loader<ColumnChartData> loader) {
        chart.setColumnChartData(new ColumnChartData());
    }

    private class ValueTouchListener implements ColumnChartOnValueSelectListener {

        /**
         * @param columnIndex    starts from the oldest
         * @param subcolumnIndex 0 -> fuel, 1-> expense
         * @param value
         */
        @Override
        public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {
            switch (subcolumnIndex) {
                case 0:
                    Toast.makeText(getActivity(), getString(R.string.statistics_expense_price_alert,
                            BigDecimalFormatter.getCommonFormat().format(value.getValue()) + " " +
                                    vehicle.getCurrencySymbol()), Toast.LENGTH_SHORT).show();
                    return;
                case 1:
                    Toast.makeText(getActivity(), getString(R.string.statistics_fuel_price_alert,
                            BigDecimalFormatter.getCommonFormat().format(value.getValue()) + " " +
                                    vehicle.getCurrencySymbol()), Toast.LENGTH_SHORT).show();
                    return;
                default:
                    throw new RuntimeException("Unknown value selected");

            }
        }

        @Override
        public void onValueDeselected() {

        }
    }
}

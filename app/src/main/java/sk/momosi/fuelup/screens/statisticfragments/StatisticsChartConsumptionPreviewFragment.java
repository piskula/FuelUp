package sk.momosi.fuelup.screens.statisticfragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PreviewLineChartView;
import sk.momosi.fuelup.business.FillUpService;
import sk.momosi.fuelup.databinding.FragmentStatisticsChartConsumptionPreviewBinding;
import sk.momosi.fuelup.entity.FillUp;
import sk.momosi.fuelup.entity.Vehicle;
import sk.momosi.fuelup.loaders.ConsumptionChartDataLoader;
import sk.momosi.fuelup.screens.dialog.FillUpInfoDialog;

/**
 * @author Martin Styk
 */
public class StatisticsChartConsumptionPreviewFragment extends Fragment implements LoaderManager.LoaderCallbacks<Map<String, Object>> {

    private static final String ARG_VEHICLE = "vehicleArg";

    private FragmentStatisticsChartConsumptionPreviewBinding binding;

    private LineChartView chart;
    private PreviewLineChartView previewChart;

    private List<FillUp> displayedValues = new ArrayList<>(0);

    public static StatisticsChartConsumptionPreviewFragment newInstance(Vehicle param) {
        StatisticsChartConsumptionPreviewFragment fragment = new StatisticsChartConsumptionPreviewFragment();
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
        binding = FragmentStatisticsChartConsumptionPreviewBinding.inflate(inflater);

        chart = binding.chart;
        previewChart = binding.chartPreview;
        chart.setZoomEnabled(true);
        chart.setScrollEnabled(true);
        chart.setZoomType(ZoomType.HORIZONTAL);
        chart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        chart.setViewportChangeListener(new ViewportListener());
        chart.setOnValueTouchListener(new ValueTouchListener());
        previewChart.setScrollEnabled(false);
        previewChart.setZoomEnabled(false);

        return binding.getRoot();
    }

    @Override
    public Loader<Map<String, Object>> onCreateLoader(int id, Bundle args) {
        Vehicle vehicle = args.getParcelable(ARG_VEHICLE);
        long vehicleId = vehicle.getId();
        return new ConsumptionChartDataLoader(getActivity(), vehicleId, new FillUpService());
    }

    @Override
    public void onLoadFinished(Loader<Map<String, Object>> loader, Map<String, Object> data) {
        binding.setHasData(data != null);
        if (data != null) {
            chart.setLineChartData((LineChartData) data.get(ConsumptionChartDataLoader.CHART_DATA));
            previewChart.setLineChartData((LineChartData) data.get(ConsumptionChartDataLoader.PREVIEW_DATA));

            displayedValues = (List<FillUp>) data.get(ConsumptionChartDataLoader.FILL_UPS);

            setViewport(displayedValues.size());
        }
    }

    private void setViewport(int dataSize) {
        final Viewport maxViewPort = chart.getMaximumViewport();
        final Viewport chartViewPort = new Viewport(maxViewPort);
        if (dataSize > 2) {
            chartViewPort.bottom = maxViewPort.bottom - 0.03f * maxViewPort.bottom;
            chartViewPort.top = 1.03f * maxViewPort.top;
            chartViewPort.right = 1.01f * maxViewPort.right;
            chart.setMaximumViewport(chartViewPort);
            chart.setCurrentViewport(chartViewPort);
        } else {
            chartViewPort.bottom = maxViewPort.bottom - 2f;
            chartViewPort.top = maxViewPort.top + 2;
            chartViewPort.right = maxViewPort.right + 2;
            chartViewPort.left = maxViewPort.left - 2;
            chart.setMaximumViewport(chartViewPort);
            chart.setCurrentViewport(chartViewPort);
            previewChart.setMaximumViewport(chartViewPort);
            previewChart.setCurrentViewport(chartViewPort);
        }
    }


    @Override
    public void onLoaderReset(Loader<Map<String, Object>> loader) {
        chart.setLineChartData(new LineChartData());
        previewChart.setLineChartData(new LineChartData());
    }

    /**
     * Viewport listener for preview chart(lower one). in {@link #onViewportChanged(Viewport)} method change
     * viewport of upper chart.
     */
    private class ViewportListener implements ViewportChangeListener {

        @Override
        public void onViewportChanged(Viewport newViewport) {
            previewChart.setCurrentViewport(newViewport);
        }
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

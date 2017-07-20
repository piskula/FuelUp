package sk.piskula.fuelup.screens.statisticfragments;

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
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PreviewLineChartView;
import sk.piskula.fuelup.business.FillUpService;
import sk.piskula.fuelup.databinding.FragmentStatisticsChartConsumptionPreviewBinding;
import sk.piskula.fuelup.entity.FillUp;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.loaders.ConsumptionChartDataLoader;
import sk.piskula.fuelup.loaders.FillUpLoader;

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
        getLoaderManager().initLoader(FillUpLoader.ID, getArguments(), this);
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
        previewChart.setScrollEnabled(false);
        previewChart.setZoomEnabled(false);

        return binding.getRoot();
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
            chart.setLineChartData((LineChartData) data.get(ConsumptionChartDataLoader.CHART_DATA));
            previewChart.setLineChartData((LineChartData) data.get(ConsumptionChartDataLoader.PREVIEW_DATA));

            chart.setVisibility(View.VISIBLE);
            previewChart.setVisibility(View.VISIBLE);

            displayedValues = (List<FillUp>) data.get(ConsumptionChartDataLoader.FILL_UPS);
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

}

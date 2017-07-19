package sk.piskula.fuelup.screens.statisticfragments;

import android.graphics.CornerPathEffect;
import android.graphics.PathDashPathEffect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PreviewLineChartView;
import sk.piskula.fuelup.R;
import sk.piskula.fuelup.business.FillUpService;
import sk.piskula.fuelup.business.StatisticsService;
import sk.piskula.fuelup.databinding.FragmentStatisticsChartConsumptionPreviewBinding;
import sk.piskula.fuelup.entity.FillUp;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.entity.util.DateUtil;
import sk.piskula.fuelup.loaders.FillUpLoader;

/**
 * @author Martin Styk
 */
public class StatisticsChartConsumptionPreviewFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<FillUp>> {

    private static final String ARG_VEHICLE = "vehicleArg";

    private FragmentStatisticsChartConsumptionPreviewBinding binding;

    private LineChartView chart;
    private PreviewLineChartView previewChart;

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
        chart.setZoomEnabled(false);
        chart.setScrollEnabled(false);
        previewChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        previewChart.setViewportChangeListener(new ViewportListener());

        return binding.getRoot();
    }

    @Override
    public Loader<List<FillUp>> onCreateLoader(int id, Bundle args) {
        Vehicle vehicle = args.getParcelable(ARG_VEHICLE);
        long vehicleId = vehicle.getId();
        return new FillUpLoader(getActivity(), vehicleId, new FillUpService(getActivity()));
    }

    @Override
    public void onLoadFinished(Loader<List<FillUp>> loader, List<FillUp> fillUps) {
        boolean hasDataToShow = hasSomethingToDisplay(fillUps);
        binding.setHasData(hasDataToShow);

        if (hasDataToShow) {
            LineChartData data = generateLineChartData(fillUps);
            LineChartData previewData = new LineChartData(data);
            previewData.getLines().get(0).setColor(ChartUtils.DEFAULT_DARKEN_COLOR).setHasPoints(false);
            previewData.getLines().remove(1);

            chart.setVisibility(View.VISIBLE);
            chart.setLineChartData(data);

            previewChart.setVisibility(View.VISIBLE);
            previewChart.setLineChartData(previewData);

            previewX(false);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<FillUp>> loader) {
        chart.setLineChartData(new LineChartData());
        previewChart.setLineChartData(new LineChartData());
    }

    private boolean hasSomethingToDisplay(List<FillUp> data) {
        for (FillUp fillUp : data) {
            if (fillUp.getFuelConsumption() != null) {
                return true;
            }
        }
        return false;
    }

    private LineChartData generateLineChartData(List<FillUp> fillUps) {
        List<PointValue> values = new ArrayList<>();
        List<AxisValue> axisValues = new ArrayList<>();

        long oldestFillUpTime = 0;
        long latestFillUpTime = 0;

        for (int x = 0; x < fillUps.size(); ++x) {
            FillUp dataItem = fillUps.get(x);
            BigDecimal consumption = dataItem.getFuelConsumption();

            if (consumption != null) {
                latestFillUpTime = dataItem.getDate().getTime();
                String formattedRecordedAt = DateUtil.getDateLocalized(dataItem.getDate());
                if (oldestFillUpTime == 0) {
                    oldestFillUpTime = latestFillUpTime;
                }

                values.add(new PointValue(latestFillUpTime - oldestFillUpTime, consumption.floatValue()));
                AxisValue axisValue = new AxisValue(latestFillUpTime - oldestFillUpTime);
                axisValue.setLabel(formattedRecordedAt);
                axisValues.add(axisValue);
            }
        }

        Line line = new Line(values)
                .setColor(getResources().getColor(R.color.colorLightGrey))
                .setPointColor(getResources().getColor(R.color.colorPrimary));

        List<Line> lines = new ArrayList<>(2);
        lines.add(line);

        Vehicle vehicle = getArguments().getParcelable(ARG_VEHICLE);
        BigDecimal averageConsumption = new StatisticsService(getContext(), vehicle.getId()).getAverageConsumption();
        if (averageConsumption != null) {
            List<PointValue> averageLineValues = new ArrayList<>(2);
            averageLineValues.add(new PointValue(0, averageConsumption.floatValue()));
            averageLineValues.add(new PointValue(latestFillUpTime - oldestFillUpTime, averageConsumption.floatValue()));
            Line averageLine = new Line(averageLineValues);
            averageLine.setColor(getResources().getColor(R.color.colorAccent));
            averageLine.setHasPoints(false).setHasLabels(true);

            lines.add(averageLine);
        }


        LineChartData data = new LineChartData(lines);
        data.setAxisXBottom(new Axis(axisValues)
                .setMaxLabelChars(10));
        data.setAxisYLeft(new Axis()
                .setName(getString(R.string.statistics_fuel_consumption))
                .setHasLines(true));

        return data;
    }

    private void previewX(boolean animate) {
        Viewport tempViewport = new Viewport(chart.getMaximumViewport());
        float dx = tempViewport.width() / 4;
        tempViewport.inset(dx, 0);
        if (animate) {
            previewChart.setCurrentViewportWithAnimation(tempViewport);
        } else {
            previewChart.setCurrentViewport(tempViewport);
        }
        previewChart.setZoomType(ZoomType.HORIZONTAL);
    }

    /**
     * Viewport listener for preview chart(lower one). in {@link #onViewportChanged(Viewport)} method change
     * viewport of upper chart.
     */
    private class ViewportListener implements ViewportChangeListener {

        @Override
        public void onViewportChanged(Viewport newViewport) {
            chart.setCurrentViewport(newViewport);
        }

    }

}

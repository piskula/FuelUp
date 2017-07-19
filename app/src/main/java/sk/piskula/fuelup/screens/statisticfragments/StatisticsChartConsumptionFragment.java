package sk.piskula.fuelup.screens.statisticfragments;

import android.graphics.PathDashPathEffect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;
import sk.piskula.fuelup.R;
import sk.piskula.fuelup.business.FillUpService;
import sk.piskula.fuelup.business.StatisticsService;
import sk.piskula.fuelup.databinding.FragmentStatisticsChartConsumptionBinding;
import sk.piskula.fuelup.entity.FillUp;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.entity.util.DateUtil;
import sk.piskula.fuelup.loaders.FillUpLoader;

/**
 * @author Martin Styk
 */
public class StatisticsChartConsumptionFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<FillUp>> {

    private static final String ARG_VEHICLE = "vehicleArg";

    private FragmentStatisticsChartConsumptionBinding binding;

    private LineChartView lineChartView;

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
        getLoaderManager().initLoader(FillUpLoader.ID, getArguments(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatisticsChartConsumptionBinding.inflate(inflater);
        FrameLayout layout = (FrameLayout) binding.getRoot();

        lineChartView = new LineChartView(getActivity());
        lineChartView.setZoomType(ZoomType.HORIZONTAL);
        lineChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChartView.setVisibility(View.GONE);

        layout.addView(lineChartView);

        return layout;
    }

    @Override
    public Loader<List<FillUp>> onCreateLoader(int id, Bundle args) {
        Vehicle vehicle = args.getParcelable(ARG_VEHICLE);
        long vehicleId = vehicle.getId();
        return new FillUpLoader(getActivity(), vehicleId, new FillUpService(getActivity()));
    }

    @Override
    public void onLoadFinished(Loader<List<FillUp>> loader, List<FillUp> data) {
        boolean hasDataToShow = hasSomethingToDisplay(data);
        binding.setHasData(hasDataToShow);

        if (hasDataToShow) {
            lineChartView.setVisibility(View.VISIBLE);
            lineChartView.setLineChartData(generateLineChartData(data));
        }
    }

    @Override
    public void onLoaderReset(Loader<List<FillUp>> loader) {
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

    private LineChartData generateLineChartData(List<FillUp> fillUps) {
        List<PointValue> values = new ArrayList<>();
        List<AxisValue> axisValues = new ArrayList<>();
        for (int x = 0; x < fillUps.size(); ++x) {
            FillUp dataItem = fillUps.get(x);
            Date recordedAt = dataItem.getDate();
            String formattedRecordedAt = DateUtil.getDateLocalized(recordedAt);
            BigDecimal consumption = dataItem.getFuelConsumption();
            if (consumption != null) {
                values.add(new PointValue(x, consumption.floatValue()));
            }
            AxisValue axisValue = new AxisValue(x);
            axisValue.setLabel(formattedRecordedAt);
            axisValues.add(axisValue);
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
            averageLineValues.add(new PointValue(fillUps.size() - 1, averageConsumption.floatValue()));
            Line averageLine = new Line(averageLineValues);
            averageLine.setColor(getResources().getColor(R.color.colorAccent));
            averageLine.setHasPoints(false).setHasLabels(true);

            lines.add(averageLine);
        }


        LineChartData data = new LineChartData(lines);
        data.setAxisXBottom(new Axis(axisValues)
                .setHasTiltedLabels(true)
                .setMaxLabelChars(10));
        data.setAxisYLeft(new Axis()
                .setName(getString(R.string.statistics_fuel_consumption))
                .setHasLines(true));

        return data;
    }

}

package sk.piskula.fuelup.screens.statisticfragments;

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
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;
import sk.piskula.fuelup.R;
import sk.piskula.fuelup.business.FillUpService;
import sk.piskula.fuelup.databinding.FragmentStatisticsBasicBinding;
import sk.piskula.fuelup.entity.FillUp;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.entity.util.DateUtil;
import sk.piskula.fuelup.loaders.FillUpLoader;

/**
 * @author Martin Styk
 */
public class StatisticsAverageConsumptionFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<FillUp>> {

    private static final String ARG_VEHICLE = "vehicleArg";

    private FragmentStatisticsBasicBinding binding;

    private LineChartView lineChartView;

    public static StatisticsAverageConsumptionFragment newInstance(Vehicle param) {
        StatisticsAverageConsumptionFragment fragment = new StatisticsAverageConsumptionFragment();
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
        binding = FragmentStatisticsBasicBinding.inflate(inflater);
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

    private boolean hasSomethingToDisplay(List<FillUp> data) {
        for (FillUp fillUp : data) {
            if (fillUp.getFuelConsumption() != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onLoaderReset(Loader<List<FillUp>> loader) {
        lineChartView.setLineChartData(new LineChartData());
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

        Line line = new Line(values);
        line.setColor(ChartUtils.COLOR_ORANGE);

        List<Line> lines = new ArrayList<Line>();
        lines.add(line);

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

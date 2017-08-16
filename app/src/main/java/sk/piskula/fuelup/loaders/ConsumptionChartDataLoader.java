package sk.piskula.fuelup.loaders;

import android.content.Context;
import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.util.ChartUtils;
import sk.piskula.fuelup.R;
import sk.piskula.fuelup.business.FillUpService;
import sk.piskula.fuelup.business.StatisticsService;
import sk.piskula.fuelup.entity.FillUp;
import sk.piskula.fuelup.entity.util.DateUtil;


/**
 * Loader async task for line chart with fuel consumption
 * <p>
 * Created by Martin Styk on 15.06.2017.
 */
public class ConsumptionChartDataLoader extends FuelUpAbstractAsyncLoader<Map<String, Object>> {
    public static final String FILL_UPS = "fillUps";
    public static final String CHART_DATA = "chartData";
    public static final String PREVIEW_DATA = "previewData";

    private static final String TAG = ConsumptionChartDataLoader.class.getSimpleName();
    public static final int ID = 6;

    private FillUpService fillUpService;
    private long vehicleId;

    public ConsumptionChartDataLoader(Context context, long vehicleId, FillUpService fillUpService) {
        super(context);
        this.vehicleId = vehicleId;
        this.fillUpService = fillUpService;
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public Map<String, Object> loadInBackground() {
        List<FillUp> fillUps = fillUpService.findFillUpsOfVehicleWithComputedConsumption(vehicleId);
        if (fillUps.isEmpty()) {
            return null;
        }
        Map<String, Object> result = new HashMap<>(3);
        LineChartData lineChartData = generateLineChartData(fillUps);
        LineChartData previewChartData = generatePreviewChartData(lineChartData);

        result.put(FILL_UPS, fillUps);
        result.put(CHART_DATA, lineChartData);
        result.put(PREVIEW_DATA, previewChartData);

        return result;
    }

    private LineChartData generateLineChartData(@NonNull List<FillUp> fillUps) {
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
                .setColor(getContext().getResources().getColor(R.color.colorLightGrey))
                .setPointColor(getContext().getResources().getColor(R.color.colorPrimary));

        List<Line> lines = new ArrayList<>(2);
        lines.add(line);

        BigDecimal averageConsumption = new StatisticsService().getAverageConsumption();
        if (averageConsumption != null) {
            List<PointValue> averageLineValues = new ArrayList<>(2);
            averageLineValues.add(new PointValue(0, averageConsumption.floatValue()));
            averageLineValues.add(new PointValue(latestFillUpTime - oldestFillUpTime, averageConsumption.floatValue()));
            Line averageLine = new Line(averageLineValues);
            averageLine.setColor(getContext().getResources().getColor(R.color.colorAccent));
            averageLine.setHasPoints(false).setHasLabels(true);

            lines.add(averageLine);
        }


        LineChartData data = new LineChartData(lines);
        data.setAxisXBottom(new Axis(axisValues)
                .setMaxLabelChars(10).setHasTiltedLabels(true));
        data.setAxisYLeft(new Axis()
                .setName(getContext().getString(R.string.statistics_fuel_consumption))
                .setHasLines(true));

        return data;
    }

    private LineChartData generatePreviewChartData(@NonNull LineChartData data) {
        LineChartData previewData = new LineChartData(data);

        previewData.getLines().get(0).setColor(ChartUtils.DEFAULT_DARKEN_COLOR).setHasPoints(false);
        previewData.getLines().remove(1);
        previewData.getAxisXBottom()
                .setHasTiltedLabels(false)
                .setTextSize(12);

        return previewData;
    }

}



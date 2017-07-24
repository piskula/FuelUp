package sk.piskula.fuelup.loaders;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import sk.piskula.fuelup.R;
import sk.piskula.fuelup.business.FillUpService;
import sk.piskula.fuelup.entity.FillUp;
import sk.piskula.fuelup.entity.util.TimePair;
import sk.piskula.fuelup.util.BigDecimalFormatter;


/**
 * Loader async task for line chart with fuel price
 * <p>
 * Created by Martin Styk on 15.06.2017.
 */
public class ConsumptionPerMonthChartDataLoader extends FuelUpAbstractAsyncLoader<ColumnChartData> {

    private static final String TAG = ConsumptionPerMonthChartDataLoader.class.getSimpleName();
    public static final int ID = 8;

    private FillUpService fillUpService;
    private long vehicleId;

    public ConsumptionPerMonthChartDataLoader(Context context, long vehicleId, FillUpService fillUpService) {
        super(context);
        this.vehicleId = vehicleId;
        this.fillUpService = fillUpService;
    }

    @Override
    public ColumnChartData loadInBackground() {
        List<FillUp> fillUps = fillUpService.findFillUpsOfVehicleWithComputedConsumption(vehicleId);

        return fillUps.isEmpty() ? null : generateColumnChartData(fillUps);
    }

    private ColumnChartData generateColumnChartData(@NonNull List<FillUp> fillUps) {

        //get data
        Map<TimePair, ConsumptionPair> map = initMap(fillUps);

        for (int i = 0; i < fillUps.size(); i++) {
            FillUp item = fillUps.get(i);
            TimePair time = TimePair.from(item.getDate());
            ConsumptionPair consumptionPair = map.get(time);
            consumptionPair.numerator += item.getDistanceFromLastFillUp() * item.getFuelConsumption().floatValue();
            consumptionPair.denumerator += item.getDistanceFromLastFillUp();
        }

        //transform it to chart data
        List<Column> columns = new ArrayList<>(map.size());
        List<AxisValue> axisValues = new ArrayList<>(map.size());
        List<SubcolumnValue> values;

        int i = 0;
        for (Map.Entry<TimePair, ConsumptionPair> entry : map.entrySet()) {

            values = new ArrayList<>();
            float consumption = entry.getValue().numerator / entry.getValue().denumerator;
            values.add(new SubcolumnValue(consumption, getContext().getResources().getColor(R.color.colorAccent))
                    .setLabel(BigDecimalFormatter.getCommonFormat().format(consumption)));
            Column column = new Column(values);
            column.setHasLabelsOnlyForSelected(true);
            columns.add(column);

            AxisValue axisValue = new AxisValue(i++);
            axisValue.setLabel(entry.getKey().month + "/" + entry.getKey().year);
            axisValues.add(axisValue);
        }

        ColumnChartData data = new ColumnChartData(columns);
        data.setAxisXBottom(new Axis(axisValues)
                .setMaxLabelChars(10));
        data.setAxisYLeft(new Axis()
                .setName(getContext().getString(R.string.statistics_fuel_consumption))
                .setHasLines(true));
        return data;
    }

    /**
     * Generates map of all months between oldest and most recent fill up.
     */
    private Map<TimePair, ConsumptionPair> initMap(List<FillUp> fillUps) {
        Map<TimePair, ConsumptionPair> map = new LinkedHashMap<>();
        TimePair oldest = TimePair.from(fillUps.get(fillUps.size() - 1).getDate());
        TimePair newest = TimePair.from(fillUps.get(0).getDate());
        int startMonth = oldest.month;
        for (int year = oldest.year; year <= newest.year; year++) {
            int endMonth = year == newest.year ? newest.month : 12;
            for (int month = startMonth; month <= endMonth; month++) {
                map.put(new TimePair(year, month), new ConsumptionPair());
            }
            startMonth = 1;
        }
        return map;
    }

}

class ConsumptionPair {
    long numerator;
    float denumerator;

    public ConsumptionPair() {
        this.numerator = 0;
        this.denumerator = 0;
    }

    public ConsumptionPair(long numerator, float denumerator) {
        this.numerator = numerator;
        this.denumerator = denumerator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConsumptionPair that = (ConsumptionPair) o;

        if (numerator != that.numerator) return false;
        return Float.compare(that.denumerator, denumerator) == 0;

    }

    @Override
    public int hashCode() {
        int result = (int) (numerator ^ (numerator >>> 32));
        result = 31 * result + (denumerator != +0.0f ? Float.floatToIntBits(denumerator) : 0);
        return result;
    }
}




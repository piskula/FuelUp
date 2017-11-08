package sk.momosi.fuelup.loaders;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import sk.momosi.fuelup.R;
import sk.momosi.fuelup.business.ExpenseService;
import sk.momosi.fuelup.business.FillUpService;
import sk.momosi.fuelup.entity.Expense;
import sk.momosi.fuelup.entity.FillUp;
import sk.momosi.fuelup.entity.util.TimePair;
import sk.momosi.fuelup.util.BigDecimalFormatter;


/**
 * Loader async task for line chart with costs
 * <p>
 * Created by Martin Styk on 15.06.2017.
 */
public class CostsPerMonthChartDataLoader extends FuelUpAbstractAsyncLoader<ColumnChartData> {

    private static final String TAG = CostsPerMonthChartDataLoader.class.getSimpleName();
    public static final int ID = 9;

    private final long vehicleId;

    public CostsPerMonthChartDataLoader(Context context, long vehicleId) {
        super(context);
        this.vehicleId = vehicleId;
    }

    @Override
    public ColumnChartData loadInBackground() {
        List<FillUp> fillUps = FillUpService.findFillUpsOfVehicle(vehicleId, getContext());
        List<Expense> expenses = ExpenseService.getExpensesOfVehicle(vehicleId, getContext());

        return fillUps.isEmpty() && expenses.isEmpty() ? null : generateColumnChartData(fillUps, expenses);
    }

    private ColumnChartData generateColumnChartData(@NonNull List<FillUp> fillUps, @NonNull List<Expense> expenses) {

        //get data
        Map<TimePair, ExpensePair> map = initMap(fillUps, expenses);

        for (int i = 0; i < fillUps.size(); i++) {
            FillUp item = fillUps.get(i);
            TimePair time = TimePair.from(item.getDate());
            ExpensePair expensePair = map.get(time);
            expensePair.fuel += item.getFuelPriceTotal().floatValue();
        }

        for (int i = 0; i < expenses.size(); i++) {
            Expense item = expenses.get(i);
            TimePair time = TimePair.from(item.getDate());
            ExpensePair expensePair = map.get(time);
            expensePair.expense += item.getPrice().floatValue();
        }

        //transform it to chart data
        List<Column> columns = new ArrayList<>(map.size());
        List<AxisValue> axisValues = new ArrayList<>(map.size());
        List<SubcolumnValue> values;

        int i = 0;
        for (Map.Entry<TimePair, ExpensePair> entry : map.entrySet()) {

            values = new ArrayList<>(2);
            values.add(new SubcolumnValue(entry.getValue().fuel, getContext().getResources().getColor(R.color.colorAccent))
                    .setLabel(BigDecimalFormatter.getCommonFormat().format(entry.getValue().fuel)));
            values.add(new SubcolumnValue(entry.getValue().expense, getContext().getResources().getColor(R.color.colorAppGreen))
                    .setLabel(BigDecimalFormatter.getCommonFormat().format(entry.getValue().expense)));
            Column column = new Column(values);
            column.setHasLabelsOnlyForSelected(true);
            columns.add(column);

            AxisValue axisValue = new AxisValue(i++);
            axisValue.setLabel(entry.getKey().month + "/" + entry.getKey().year);
            axisValues.add(axisValue);
        }

        ColumnChartData data = new ColumnChartData(columns)
                .setStacked(true);
        data.setAxisXBottom(new Axis(axisValues)
                .setMaxLabelChars(10));
        data.setAxisYLeft(new Axis()
                .setName(getContext().getString(R.string.statistics_costs))
                .setHasLines(true));
        return data;
    }

    /**
     * Generates map of all months between oldest and most recent fill up.
     */
    private Map<TimePair, ExpensePair> initMap(List<FillUp> fillUps, List<Expense> expenses) {
        Map<TimePair, ExpensePair> map = new LinkedHashMap<>();
        TimePair oldestFillUp = fillUps.isEmpty() ? null : TimePair.from(fillUps.get(fillUps.size() - 1).getDate());
        TimePair newestFillUp = fillUps.isEmpty() ? null : TimePair.from(fillUps.get(0).getDate());

        TimePair oldestExpense = expenses.isEmpty() ? null : TimePair.from(expenses.get(expenses.size() - 1).getDate());
        TimePair newestExpense = expenses.isEmpty() ? null : TimePair.from(expenses.get(0).getDate());

        TimePair oldest;
        if (oldestFillUp != null && oldestExpense != null) {
            oldest = oldestFillUp.isBefore(oldestExpense) ? oldestFillUp : oldestExpense;
        } else {
            oldest = oldestFillUp == null ? oldestExpense : oldestFillUp;
        }
        TimePair newest;
        if (newestFillUp != null && newestExpense != null) {
            newest = newestFillUp.isBefore(newestExpense) ? newestExpense : newestFillUp;
        } else {
            newest = newestFillUp == null ? newestExpense : newestFillUp;
        }
        int startMonth = oldest.month;
        for (int year = oldest.year; year <= newest.year; year++) {
            int endMonth = year == newest.year ? newest.month : 12;
            for (int month = startMonth; month <= endMonth; month++) {
                map.put(new TimePair(year, month), new ExpensePair());
            }
            startMonth = 1;
        }
        return map;
    }

    private class ExpensePair {
        float fuel = 0f;
        float expense = 0f;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ExpensePair that = (ExpensePair) o;

            return Float.compare(that.fuel, fuel) == 0
                    && Float.compare(that.expense, expense) == 0;
        }

        @Override
        public int hashCode() {
            int result = (fuel != +0.0f ? Float.floatToIntBits(fuel) : 0);
            result = 31 * result + (expense != +0.0f ? Float.floatToIntBits(expense) : 0);
            return result;
        }
    }

}

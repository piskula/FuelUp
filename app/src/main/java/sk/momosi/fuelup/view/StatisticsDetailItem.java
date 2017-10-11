package sk.momosi.fuelup.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import sk.momosi.fuelup.R;

/**
 * Created by Martin Styk on 10.07.2017.
 */
public class StatisticsDetailItem extends LinearLayout {

    private TextView nameView;
    private TextView valueView;
    private TextView unitView;

    private String name;
    private String value;
    private String unit;

    public StatisticsDetailItem(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.statisticsItemDetail);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StatisticsDetailItem, 0, 0);
        name = a.getString(R.styleable.StatisticsDetailItem_name);
        value = a.getString(R.styleable.StatisticsDetailItem_value);
        unit = a.getString(R.styleable.StatisticsDetailItem_unit);

        a.recycle();

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_statistics_detail_item, this, true);

        nameView = (TextView) getChildAt(0);
        nameView.setText(name);

        valueView = (TextView) getChildAt(1);
        valueView.setText(value);

        unitView = (TextView) getChildAt(2);
        unitView.setText(unit);
    }

    public StatisticsDetailItem(Context context) {
        this(context, null);
    }

    public void setName(String name) {
        this.name = name;
        nameView.setText(name);
    }

    public void setValue(String value) {
        this.value = value;
        valueView.setText(value);
    }

    public void setUnit(String unit) {
        this.unit = unit;
        unitView.setText(unit);
    }
}

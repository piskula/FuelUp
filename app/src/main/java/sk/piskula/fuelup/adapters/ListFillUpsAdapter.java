package sk.piskula.fuelup.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.entity.FillUp;

/**
 * @author Ondrej Oravcok
 * @version 17.6.2017
 */
public class ListFillUpsAdapter extends BaseAdapter {

    private static final String TAG = "ListFillUpsAdapter";

    private List<FillUp> items;
    private LayoutInflater inflater;

    public ListFillUpsAdapter(Context context, List<FillUp> listFillUps) {
        this.setItems(listFillUps);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View v = view;
        ViewHolder holder;
        if (v == null) {
            v = inflater.inflate(R.layout.list_item_fillup, parent, false);
            holder = new ViewHolder();
            //init views

            holder.txtDistanceFromLastFillUp = v.findViewById(R.id.txt_itemfillup_distance);
            holder.txtIsFullFillUp = v.findViewById(R.id.txt_itemfillup_isfullfillup);
            holder.txtFuelVolume = v.findViewById(R.id.txt_itemfillup_fuel_volume);
            holder.txtPriceTotal = v.findViewById(R.id.txt_itemfillup_price_total);
            holder.txtPricePerLitre = v.findViewById(R.id.txt_itemfillup_price_per_litre);
            holder.txtConsumptionSymbol = v.findViewById(R.id.txt_itemfillup_consumption_symbol);
            holder.txtAvgSymbol = v.findViewById(R.id.txt_itemfillup_avg_symbol);
            holder.txtConsumption = v.findViewById(R.id.txt_itemfillup_consumption);
            holder.txtDriven = v.findViewById(R.id.txt_itemfillup_driven);
            holder.txtPricePerLitreSymbol = v.findViewById(R.id.txt_itemfillup_price_per_liter_symbol);
            holder.txtPriceTotalSymbol = v.findViewById(R.id.txt_itemfillup_price_total_symbol);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        FillUp currentItem = getItem(position);
        if (currentItem != null) {
            //set views
            holder.txtDistanceFromLastFillUp.setText(currentItem.getDistanceFromLastFillUp().toString());
            holder.txtDriven.setText(currentItem.getVehicle().getUnit().toString());
            //TODO string representation of distance unit
            holder.txtConsumptionSymbol.setText("l/100" + currentItem.getVehicle().getUnit().toString());

            if (currentItem.isFullFillUp()) {
                holder.txtIsFullFillUp.setText(R.string.listFillups_isFull);
            } else {
                holder.txtIsFullFillUp.setText(R.string.listFillUps_isNotFull);
            }

//            String currency = " " + currentItem.getCar().getCurrencyFormatted();
            String currency = " USD";   //TODO
            holder.txtPriceTotalSymbol.setText(currency);
            currency += "/l";
            holder.txtPricePerLitreSymbol.setText(currency);

            DecimalFormat bddf = new DecimalFormat();
            bddf.setMaximumFractionDigits(2);
            bddf.setMinimumFractionDigits(0);
            bddf.setGroupingUsed(false);

            holder.txtFuelVolume.setText(bddf.format(currentItem.getFuelVolume()));

            bddf.setMinimumFractionDigits(2);
            String priceTotal = bddf.format(currentItem.getFuelPriceTotal().setScale(2, BigDecimal.ROUND_DOWN));
            holder.txtPriceTotal.setText(priceTotal);

            bddf.setMaximumFractionDigits(3);
            bddf.setMinimumFractionDigits(3);
            holder.txtPricePerLitre.setText(bddf.format(currentItem.getFuelPricePerLitre()));
            if (currentItem.isFullFillUp()) {
                Log.w(TAG, currentItem.getFuelVolume() + " * 100 / " + currentItem.getDistanceFromLastFillUp());
                Double consumption = currentItem.getFuelVolume() * 100 / (currentItem.getDistanceFromLastFillUp());
                bddf.setMinimumFractionDigits(2);
                bddf.setMaximumFractionDigits(2);
                holder.txtConsumption.setText(bddf.format(consumption));
            } else {
                holder.txtConsumption.setText("N/A");
            }
        }

        return v;
    }

    @Override
    public int getCount() {
        return (getItems() != null && !getItems().isEmpty()) ? getItems().size() : 0;
    }

    @Override
    public FillUp getItem(int position) {
        return (getItems() != null && !getItems().isEmpty()) ? getItems().get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return (getItems() != null && !getItems().isEmpty()) ? getItems().get(position).getId() : position;
    }

    private List<FillUp> getItems() {
        return items;
    }

    private void setItems(List<FillUp> mItems) {
        Collections.sort(mItems, new Comparator<FillUp>() {
            @Override
            public int compare(FillUp f1, FillUp f2) {
                return f2.getDate().compareTo(f1.getDate());
            }
        });
        this.items = mItems;
    }

    class ViewHolder {
        TextView txtDistanceFromLastFillUp;
        TextView txtIsFullFillUp;
        TextView txtFuelVolume;
        TextView txtPriceTotal;
        TextView txtPricePerLitre;
        TextView txtConsumption;
        TextView txtConsumptionSymbol;
        TextView txtAvgSymbol;
        TextView txtDriven;
        TextView txtPricePerLitreSymbol;
        TextView txtPriceTotalSymbol;
    }
}

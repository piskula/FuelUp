package sk.piskula.fuelup.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.entity.FillUp;
import sk.piskula.fuelup.entity.util.CurrencyUtil;
import sk.piskula.fuelup.entity.util.DateUtil;

/**
 * Created by Martin Styk on 19.06.2017.
 */
public class ListFillUpsAdapter extends RecyclerView.Adapter<ListFillUpsAdapter.ViewHolder> {

    private List<FillUp> items;
    private Callback callback;
    private Context context;

    public ListFillUpsAdapter(Callback callback) {
        super();
        this.items = new ArrayList<>();
        this.callback = callback;
    }

    public interface Callback {
        void onItemClick(View v, FillUp fillUp, int position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(this.context).inflate(R.layout.list_item_fillup, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        FillUp currentItem = items.get(position);
        if (currentItem != null) {
            //set views
            holder.txtDistanceFromLastFillUp.setText(currentItem.getDistanceFromLastFillUp().toString());
            holder.txtDriven.setText(currentItem.getVehicle().getUnit().toString());
            holder.txtConsumptionSymbol.setText(context.getString(R.string.units_litreper100_distance_units, currentItem.getVehicle().getUnit().toString()));
            holder.txtDate.setText(DateUtil.getDateLocalized(currentItem.getDate()));
            holder.imgFullnessFillUpSymbol.setImageResource(context.getResources().getIdentifier(
                    currentItem.isFullFillUp() ? "ic_gasolinedrop_full" : "ic_gasolinedrop_empty", "drawable", context.getPackageName()));
            holder.txtPriceTotal.setText(CurrencyUtil.getPrice(
                    currentItem.getVehicle().getCurrency(), currentItem.getFuelPriceTotal(), context));
            holder.txtPricePerLitre.setText(CurrencyUtil.getPricePerLitre(
                    currentItem.getVehicle().getCurrency(), currentItem.getFuelPricePerLitre(), context));
            holder.txtPricePerLitreSymbol.setText("/" + currentItem.getVehicle().getVolumeUnit());

            DecimalFormat bddf = new DecimalFormat();
            bddf.setGroupingUsed(false);
            bddf.setMaximumFractionDigits(2);
            bddf.setMinimumFractionDigits(0);

            holder.txtFuelVolume.setText(bddf.format(currentItem.getFuelVolume()));
            holder.txtFuelVolumeSymbol.setText(currentItem.getVehicle().getVolumeUnit().toString());

            if (currentItem.getFuelConsumption() != null) {
                BigDecimal consumption = currentItem.getFuelConsumption();
                bddf.setMinimumFractionDigits(2);
                bddf.setMaximumFractionDigits(2);
                holder.txtConsumption.setText(bddf.format(consumption));
            } else {
                holder.txtConsumption.setText("-");
            }
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onItemClick(v, items.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void dataChange(List<FillUp> items) {
        this.items = items;
        notifyDataSetChanged();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;

        TextView txtDistanceFromLastFillUp;
        TextView txtDate;
        TextView txtFuelVolume;
        TextView txtFuelVolumeSymbol;
        TextView txtPriceTotal;
        TextView txtPricePerLitre;
        TextView txtConsumption;
        TextView txtConsumptionSymbol;
        TextView txtAvgSymbol;
        TextView txtDriven;
        TextView txtPricePerLitreSymbol;
        ImageView imgFullnessFillUpSymbol;

        ViewHolder(View v) {
            super(v);
            mView = v;
            txtDistanceFromLastFillUp = v.findViewById(R.id.txt_itemfillup_distance);
            txtDate = v.findViewById(R.id.txt_itemfillup_date);
            txtFuelVolume = v.findViewById(R.id.txt_itemfillup_fuel_volume);
            txtFuelVolumeSymbol = v.findViewById(R.id.txt_itemfillup_fuel_volume_symbol);
            txtPriceTotal = v.findViewById(R.id.txt_itemfillup_price_total);
            txtPricePerLitre = v.findViewById(R.id.txt_itemfillup_price_per_litre);
            txtConsumptionSymbol = v.findViewById(R.id.txt_itemfillup_consumption_symbol);
            txtAvgSymbol = v.findViewById(R.id.txt_itemfillup_avg_symbol);
            txtConsumption = v.findViewById(R.id.txt_itemfillup_consumption);
            txtDriven = v.findViewById(R.id.txt_itemfillup_driven);
            txtPricePerLitreSymbol = v.findViewById(R.id.txt_itemfillup_price_per_liter_symbol);
            imgFullnessFillUpSymbol = v.findViewById(R.id.img_itemfillup_pump);
        }

    }
}
package sk.piskula.fuelup.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.entity.FillUp;

/**
 * Created by Martin Styk on 19.06.2017.
 */

public class ListFillUpsAdapter extends RecyclerView.Adapter<ListFillUpsAdapter.ViewHolder> {

    private List<FillUp> items;
    private Callback callback;


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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_fillup, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        FillUp currentItem = items.get(position);
        if (currentItem != null) {
            //set views
            holder.txtDistanceFromLastFillUp.setText(currentItem.getDistanceFromLastFillUp().toString());
            holder.txtDriven.setText(currentItem.getVehicle().getUnit().toString());
            holder.txtConsumptionSymbol.setText("l/100" + currentItem.getVehicle().getUnit().toString());

            if (currentItem.isFullFillUp()) {
                holder.txtIsFullFillUp.setText(R.string.listFillups_isFull);
            } else {
                holder.txtIsFullFillUp.setText(R.string.listFillUps_isNotFull);
            }

            String currency = " " + currentItem.getVehicle().getCurrencySymbol();
            holder.txtPriceTotalSymbol.setText(" " + currency);
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
            if (currentItem.getFuelConsumption() != null) {
                BigDecimal consumption = currentItem.getFuelConsumption();
                bddf.setMinimumFractionDigits(2);
                bddf.setMaximumFractionDigits(2);
                holder.txtConsumption.setText(bddf.format(consumption));
            } else {
                holder.txtConsumption.setText("N/A");
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

        ViewHolder(View v) {
            super(v);
            mView = v;
            txtDistanceFromLastFillUp = v.findViewById(R.id.txt_itemfillup_distance);
            txtIsFullFillUp = v.findViewById(R.id.txt_itemfillup_isfullfillup);
            txtFuelVolume = v.findViewById(R.id.txt_itemfillup_fuel_volume);
            txtPriceTotal = v.findViewById(R.id.txt_itemfillup_price_total);
            txtPricePerLitre = v.findViewById(R.id.txt_itemfillup_price_per_litre);
            txtConsumptionSymbol = v.findViewById(R.id.txt_itemfillup_consumption_symbol);
            txtAvgSymbol = v.findViewById(R.id.txt_itemfillup_avg_symbol);
            txtConsumption = v.findViewById(R.id.txt_itemfillup_consumption);
            txtDriven = v.findViewById(R.id.txt_itemfillup_driven);
            txtPricePerLitreSymbol = v.findViewById(R.id.txt_itemfillup_price_per_liter_symbol);
            txtPriceTotalSymbol = v.findViewById(R.id.txt_itemfillup_price_total_symbol);
        }

    }
}
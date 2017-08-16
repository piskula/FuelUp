package sk.piskula.fuelup.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.data.FuelUpContract.FillUpEntry;
import sk.piskula.fuelup.entity.FillUp;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.entity.enums.DistanceUnit;
import sk.piskula.fuelup.entity.util.CurrencyUtil;
import sk.piskula.fuelup.entity.util.DateUtil;
import sk.piskula.fuelup.entity.util.VolumeUtil;
import sk.piskula.fuelup.screens.MainActivity;

/**
 * @author Martin Styk
 * @version 16.08.2017
 */
public class ListFillUpsAdapter extends RecyclerViewCursorAdapter<ListFillUpsAdapter.FillUpViewHolder> {

    private Callback mCallback;
    private Vehicle mVehicle;

    private DecimalFormat consumptionFormat;

    public ListFillUpsAdapter(Callback callback, Vehicle vehicle) {
        super(null);

        this.mVehicle = vehicle;
        this.mCallback = callback;

        int consumptionFractionDigits = mVehicle.getDistanceUnit() == DistanceUnit.mi ? 1 : 2;
        consumptionFormat = new DecimalFormat();
        consumptionFormat.setGroupingUsed(false);
        consumptionFormat.setMinimumFractionDigits(consumptionFractionDigits);
        consumptionFormat.setMaximumFractionDigits(consumptionFractionDigits);
    }

    public interface Callback {
        void onItemClick(long fillUpId);
    }

    @Override
    public FillUpViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_fillup, parent, false);
        return new FillUpViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(FillUpViewHolder holder, Cursor cursor) {

        int idColumnIndex = cursor.getColumnIndexOrThrow(FillUpEntry._ID);
        int pricePerLitreColumnIndex = cursor.getColumnIndexOrThrow(FillUpEntry.COLUMN_FUEL_PRICE_PER_LITRE);
        int priceTotalColumnIndex = cursor.getColumnIndexOrThrow(FillUpEntry.COLUMN_FUEL_PRICE_TOTAL);
        int consumptionColumnIndex = cursor.getColumnIndexOrThrow(FillUpEntry.COLUMN_FUEL_CONSUMPTION);
        int fuelVolumeColumnIndex = cursor.getColumnIndexOrThrow(FillUpEntry.COLUMN_FUEL_VOLUME);
        int isFullFillUpColumnIndex = cursor.getColumnIndexOrThrow(FillUpEntry.COLUMN_IS_FULL_FILLUP);
        int distanceColumnIndex = cursor.getColumnIndexOrThrow(FillUpEntry.COLUMN_DISTANCE_FROM_LAST);
        int dateColumnIndex = cursor.getColumnIndexOrThrow(FillUpEntry.COLUMN_DATE);

        final String consumptionString = cursor.getString(consumptionColumnIndex);
        if (consumptionString == null || consumptionString.isEmpty()) {
            holder.txtConsumption.setText("-");
        } else {
            holder.txtConsumption.setText(consumptionFormat.format(cursor.getDouble(consumptionColumnIndex)));
        }

        final long fillUpId = cursor.getLong(idColumnIndex);
        holder.txtConsumptionUnit.setText(mVehicle.getConsumptionUnit());
        holder.txtDistanceFromLastFillUp.setText(cursor.getString(distanceColumnIndex));
        holder.imgFullnessFillUpSymbol.setImageResource(getImageResourceId(cursor.getInt(isFullFillUpColumnIndex) != 0));
        holder.txtDate.setText(DateUtil.getDateLocalized(new Date(cursor.getLong(dateColumnIndex))));
        holder.txtDistanceUnit.setText(mVehicle.getDistanceUnit().toString());
        holder.txtPriceTotal.setText(CurrencyUtil.getPrice(mVehicle.getCurrency(), cursor.getDouble(priceTotalColumnIndex)));
        holder.txtPricePerLitre.setText(CurrencyUtil.getPricePerLitre(mVehicle.getCurrency(), cursor.getDouble(pricePerLitreColumnIndex)));
        holder.txtPricePerLitreSymbol.setText("/" + MainActivity.getInstance().getString(R.string.unit_litre));
        holder.txtFuelVolume.setText(VolumeUtil.getFuelVolume(cursor.getDouble(fuelVolumeColumnIndex)));
        holder.txtFuelVolumeSymbol.setText(mVehicle.getVolumeUnit().toString());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onItemClick(fillUpId);
            }
        });
    }

    private int getImageResourceId(boolean isFullFillup) {
        String fileName = isFullFillup ? "ic_gasolinedrop_full" : "ic_gasolinedrop_empty";
        return MainActivity.getInstance().getResources()
                .getIdentifier(fileName, "drawable", MainActivity.getInstance().getPackageName());
    }

    class FillUpViewHolder extends RecyclerView.ViewHolder {
        TextView txtDistanceFromLastFillUp;
        TextView txtDate;
        TextView txtFuelVolume;
        TextView txtFuelVolumeSymbol;
        TextView txtPriceTotal;
        TextView txtPricePerLitre;
        TextView txtConsumption;
        TextView txtConsumptionUnit;
        TextView txtDistanceUnit;
        TextView txtPricePerLitreSymbol;
        ImageView imgFullnessFillUpSymbol;

        FillUpViewHolder(View v) {
            super(v);

            txtDistanceFromLastFillUp = v.findViewById(R.id.txt_itemfillup_distance);
            txtDate = v.findViewById(R.id.txt_itemfillup_date);
            txtFuelVolume = v.findViewById(R.id.txt_itemfillup_fuel_volume);
            txtFuelVolumeSymbol = v.findViewById(R.id.txt_itemfillup_fuel_volume_symbol);
            txtPriceTotal = v.findViewById(R.id.txt_itemfillup_price_total);
            txtPricePerLitre = v.findViewById(R.id.txt_itemfillup_price_per_litre);
            txtConsumptionUnit = v.findViewById(R.id.txt_itemfillup_consumptionUnit);
            txtConsumption = v.findViewById(R.id.txt_itemfillup_consumption);
            txtDistanceUnit = v.findViewById(R.id.txt_itemfillup_distanceUnit);
            txtPricePerLitreSymbol = v.findViewById(R.id.txt_itemfillup_price_per_liter_symbol);
            imgFullnessFillUpSymbol = v.findViewById(R.id.img_itemfillup_pump);
        }

    }
}
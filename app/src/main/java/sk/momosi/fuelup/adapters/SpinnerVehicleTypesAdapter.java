package sk.momosi.fuelup.adapters;

import android.app.Activity;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sk.momosi.fuelup.R;
import sk.momosi.fuelup.data.FuelUpContract;
import sk.momosi.fuelup.data.FuelUpContract.VehicleTypeEntry;
import sk.momosi.fuelup.entity.VehicleType;

/**
 * @author Ondrej Oravcok
 * @version 20.6.2017
 */
public class SpinnerVehicleTypesAdapter extends BaseAdapter implements SpinnerAdapter {

    private final Activity activity;
    private final List<VehicleType> vehicleTypes;

    public SpinnerVehicleTypesAdapter(Activity activity) {
        this.activity = activity;

        Cursor cursor = activity.getContentResolver().query(VehicleTypeEntry.CONTENT_URI,
                FuelUpContract.ALL_COLUMNS_VEHICLE_TYPES,
                null,
                null,
                null);

        vehicleTypes = new ArrayList<>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                VehicleType type = new VehicleType();

                type.setId(cursor.getLong(cursor.getColumnIndexOrThrow(VehicleTypeEntry._ID)));
                type.setName(cursor.getString(cursor.getColumnIndexOrThrow(VehicleTypeEntry.COLUMN_NAME)));

                vehicleTypes.add(type);
            }
            cursor.close();
        }
    }

    public List<VehicleType> getVehicleTypes() {
        return Collections.unmodifiableList(vehicleTypes);
    }

    @Override
    public int getCount() {
        return vehicleTypes.size();
    }

    @Override
    public Object getItem(int position) {
        return vehicleTypes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return vehicleTypes.get(position).getId();
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View spinView;

        if (view == null) {
            spinView = activity.getLayoutInflater().inflate(R.layout.spinner_vehicletype_item, null);
        } else {
            spinView = view;
        }
        ImageView vehiclePic = spinView.findViewById(R.id.spinner_item_vehicletype_pic);
        TextView type = spinView.findViewById(R.id.spinner_item_vehicletype_name);

        type.setText(vehicleTypes.get(position).getName());
        vehiclePic.setImageResource(activity.getResources()
                .getIdentifier("ic_type_" + vehicleTypes.get(position).getName().toLowerCase(),
                        "drawable", activity.getPackageName()));

        return spinView;
    }

}

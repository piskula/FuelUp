package sk.momosi.fuelup.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import sk.momosi.fuelup.R;
import sk.momosi.fuelup.business.StatisticsService;
import sk.momosi.fuelup.business.VehicleTypeService;
import sk.momosi.fuelup.data.FuelUpContract.VehicleEntry;
import sk.momosi.fuelup.screens.MainActivity;

/**
 * @author Ondrej Oravcok
 * @version 16.8.2017
 */
public class ListVehiclesAdapter extends RecyclerViewCursorAdapter<ListVehiclesAdapter.VehicleViewHolder> {

    private static final String LOG_TAG = ListVehiclesAdapter.class.getSimpleName();

    private Context mContext;
    private Callback mCallback;

    public interface Callback {
        void onItemClick(long vehicleId);
    }

    public ListVehiclesAdapter(Context context, Callback callback) {
        super(null);
        mContext = context;
        mCallback = callback;
    }

    @Override
    public VehicleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_vehicle, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VehicleViewHolder holder, final Cursor cursor) {

        int idColumnIndex = cursor.getColumnIndexOrThrow(VehicleEntry._ID);
        int nameColumnIndex = cursor.getColumnIndexOrThrow(VehicleEntry.COLUMN_NAME);
        int makerColumnIndex = cursor.getColumnIndexOrThrow(VehicleEntry.COLUMN_VEHICLE_MAKER);
        int pictureColumnIndex = cursor.getColumnIndexOrThrow(VehicleEntry.COLUMN_PICTURE);
        int typeColumnIndex = cursor.getColumnIndexOrThrow(VehicleEntry.COLUMN_TYPE);

        final long vehicleId = cursor.getInt(idColumnIndex);
        holder.txtName.setText(cursor.getString(nameColumnIndex));
        holder.txtMaker.setText(cursor.getString(makerColumnIndex));
        holder.txtDistance.setText(new StatisticsService(mContext, vehicleId).getActualMileageIfPossible());
        holder.thumbnail.setImageBitmap(BitmapFactory.decodeFile(cursor.getString(pictureColumnIndex)));
        holder.overflow.setImageResource(getImageResourceId(cursor.getInt(typeColumnIndex)));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onItemClick(vehicleId);
            }
        });
    }


    static class VehicleViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail, overflow;
        TextView txtName, txtMaker, txtDistance;

        VehicleViewHolder(View view) {
            super(view);

            txtName = view.findViewById(R.id.vehicle_item_title);
            txtMaker = view.findViewById(R.id.vehicle_item_count);
            txtDistance = view.findViewById(R.id.vehicle_item_vehicleDistanceDriven);
            thumbnail = view.findViewById(R.id.vehicle_item_thumbnail);
            overflow = view.findViewById(R.id.vehicle_item_vehicleType);
        }
    }

    private int getImageResourceId(int vehicleTypeId) {
        Context cxt = MainActivity.getInstance();
        return cxt.getResources().getIdentifier("ic_type_"
                        + VehicleTypeService.getVehicleTypeNameById(vehicleTypeId, cxt).toLowerCase(),
                "drawable", cxt.getPackageName());
    }

}

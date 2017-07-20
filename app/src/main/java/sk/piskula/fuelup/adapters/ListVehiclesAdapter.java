package sk.piskula.fuelup.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.entity.VehicleType;
import sk.piskula.fuelup.screens.MainActivity;

/**
 * @author Ondrej Oravcok
 * @version 16.6.2017.
 */
public class ListVehiclesAdapter extends RecyclerView.Adapter<ListVehiclesAdapter.ViewHolder> {

    private static final String TAG = ListVehiclesAdapter.class.getSimpleName();

    private List<Vehicle> mItems;
    private Callback callback;

    public ListVehiclesAdapter(Callback callback) {
        super();
        this.callback = callback;
        mItems = new ArrayList<>();
    }

    public interface Callback {
        void onItemClick(View v, Vehicle vehicle, int position);
    }

    public List<Vehicle> getItems() {
        return mItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_vehicle, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Vehicle vehicle = mItems.get(position);

        holder.title.setText(vehicle.getName());
        holder.count.setText(vehicle.getVehicleMaker());
        holder.thumbnail.setImageBitmap(vehicle.getPicture());
        holder.overflow.setImageResource(getImageResourceId(vehicle.getType()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onItemClick(v, mItems.get(position), position);
            }
        });
    }

    private int getImageResourceId(VehicleType type) {
        Context cxt = MainActivity.getInstance();
        return cxt.getResources().getIdentifier(
                "ic_type_" + type.getName().toLowerCase(), "drawable", cxt.getPackageName());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void dataChange(List<Vehicle> items) {
        this.mItems = items;
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail, overflow;
        TextView title, count;
//            TextView txtCarTypeName;
//            TextView txtMileage;
//            TextView txtConsumption;
//            TextView txtConsumptionUnit;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.vehicle_item_title);
            count = view.findViewById(R.id.vehicle_item_count);
            thumbnail = view.findViewById(R.id.vehicle_item_thumbnail);
            overflow = view.findViewById(R.id.vehicle_item_vehicleType);
        }
    }

    public Vehicle getItem(int i) {
        return (getItems() != null && !getItems().isEmpty()) ? getItems().get(i) : null;
    }
}

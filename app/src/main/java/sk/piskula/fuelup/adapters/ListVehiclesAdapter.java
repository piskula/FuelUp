package sk.piskula.fuelup.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.business.VehicleService;
import sk.piskula.fuelup.entity.Vehicle;

/**
 * @author Ondrej Oravcok
 * @version 16.6.2017.
 */
public class ListVehiclesAdapter extends BaseAdapter {

    private static final String TAG = ListVehiclesAdapter.class.getSimpleName();

    private List<Vehicle> mItems;
    private LayoutInflater mInflater;
    private TextView noVehicleText;

    private VehicleService vehicleService;

    public ListVehiclesAdapter(Context context, TextView noVehicleText) {
        vehicleService = new VehicleService(context);
        this.noVehicleText = noVehicleText;
        refreshItems();
        this.mInflater = LayoutInflater.from(context);
    }

    public void refreshItems() {

        mItems = vehicleService.findAll();

        if (mItems.isEmpty()) noVehicleText.setVisibility(View.VISIBLE);
        else noVehicleText.setVisibility(View.GONE);

        notifyDataSetChanged();
    }

    public List<Vehicle> getItems() {
        return mItems;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Vehicle getItem(int i) {
        return (getItems() != null && !getItems().isEmpty()) ? getItems().get(i) : null;
    }

    @Override
    public long getItemId(int id) {
        return (getItems() != null && !getItems().isEmpty()) ? getItems().get(id).getId() : id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;
        if (v == null) {
            v = mInflater.inflate(R.layout.vehicle_list_item, parent, false);
            holder = new ViewHolder();

            holder.txtname = v.findViewById(R.id.txt_itemcar_nick);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        Vehicle currentVehicle = getItem(position);
        if (currentVehicle != null) {
            holder.txtname.setText(currentVehicle.getName() + "-" + currentVehicle.getId());
        }

        return v;
    }

    class ViewHolder {
        //            ImageView imgPhoto;
//            ImageView imgType;
        TextView txtname;
//            TextView txtCarTypeName;
//            TextView txtMileage;
//            TextView txtConsumption;
//            TextView txtConsumptionUnit;
    }
}

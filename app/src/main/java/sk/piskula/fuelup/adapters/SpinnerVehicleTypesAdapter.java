package sk.piskula.fuelup.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.data.DatabaseHelper;
import sk.piskula.fuelup.entity.VehicleType;

/**
 * @author Ondrej Oravcok
 * @version 20.6.2017
 */
public class SpinnerVehicleTypesAdapter extends BaseAdapter implements SpinnerAdapter {

    private Activity activity;
    private List<VehicleType> vehicleTypes;

    public SpinnerVehicleTypesAdapter(Activity activity) {
        this.activity = activity;

        try {
            this.vehicleTypes = OpenHelperManager.getHelper(activity, DatabaseHelper.class).getVehicleTypeDao().queryForAll();
            OpenHelperManager.releaseHelper();
        } catch (SQLException e) {
            this.vehicleTypes = new ArrayList<>();
        }
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
        TextView currencyName = spinView.findViewById(R.id.spinner_item_vehicletype_name);

        currencyName.setText(vehicleTypes.get(position).getName());
        vehiclePic.setImageResource(activity.getResources()
                .getIdentifier("ic_type_" + vehicleTypes.get(position).getName().toLowerCase(),
                        "drawable", activity.getPackageName()));

        return spinView;
    }

}

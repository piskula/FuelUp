package sk.piskula.fuelup.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.util.Currency;
import java.util.List;
import java.util.Locale;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.data.DatabaseHelper;
import sk.piskula.fuelup.entity.Vehicle;

/**
 * @author Ondrej Oravcok
 * @version 20.6.2017
 */
public class SpinnerCurrencyAdapter extends BaseAdapter implements SpinnerAdapter {

    private Activity activity;
    private List<Currency> currencies;

    public SpinnerCurrencyAdapter(Activity activity) {
        this.activity = activity;
        this.currencies = Vehicle.getSupportedCurrencies();
    }

    @Override
    public int getCount() {
        return currencies.size();
    }

    @Override
    public Object getItem(int position) {
        return currencies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return currencies.get(position).getCurrencyCode().hashCode();
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View spinView;

        if (view == null) {
            spinView = activity.getLayoutInflater().inflate(R.layout.spinner_currency_item, null);
        } else {
            spinView = view;
        }
        TextView currencySymbol = spinView.findViewById(R.id.spinner_item_currency_symbol);
        TextView currencyName = spinView.findViewById(R.id.spinner_item_currency_name);

        //TODO getDisplayName() API level 19
        currencyName.setText(currencies.get(position).getCurrencyCode());
        currencySymbol.setText(Vehicle.getCurrencySymbol(currencies.get(position)));

        return spinView;
    }

}

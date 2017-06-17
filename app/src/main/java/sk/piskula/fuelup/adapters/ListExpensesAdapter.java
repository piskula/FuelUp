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
import sk.piskula.fuelup.entity.Expense;

/**
 * @author Ondrej Oravcok
 * @version 17.6.2017
 */
public class ListExpensesAdapter extends BaseAdapter {

    private static final String TAG = "ListExpensesAdapter";

    private List<Expense> items;
    private LayoutInflater inflater;

    public ListExpensesAdapter(Context context, List<Expense> listExpenses) {
        this.setItems(listExpenses);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View v = view;
        ViewHolder holder;
        if(v == null) {
            v = inflater.inflate(R.layout.list_item_expense, parent, false);
            holder = new ViewHolder();

            //init views
            holder.txtPriceSymbol = (TextView) v.findViewById(R.id.txt_itemexpense_price_currency);
            holder.txtInfo = (TextView) v.findViewById(R.id.txt_itemexpense_title);
            holder.txtPrice = (TextView) v.findViewById(R.id.txt_itemexpense_price);
            holder.txtDate = (TextView) v.findViewById(R.id.txt_itemexpense_date);

            v.setTag(holder);
        }
        else {
            holder = (ViewHolder) v.getTag();
        }

        Expense currentItem = getItem(position);
        if(currentItem != null) {
            DecimalFormat bddf = new DecimalFormat();

            holder.txtPriceSymbol.setText("USD");
            //TODO string representation of distance unit
//            holder.txtPriceSymbol.setText(currentItem.getVehicle().getCurrencyFormatted());
            holder.txtInfo.setText(currentItem.getInfo());
            holder.txtPrice.setText(bddf.format(currentItem.getPrice()));
            holder.txtDate.setText(android.text.format.DateFormat.getDateFormat(parent.getContext()).format(currentItem.getDate().getTime()));
        }

        return v;
    }

    @Override
    public int getCount() {
        return (getItems() != null && !getItems().isEmpty()) ? getItems().size() : 0;
    }

    @Override
    public Expense getItem(int position) {
        return (getItems() != null && !getItems().isEmpty()) ? getItems().get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return (getItems() != null && !getItems().isEmpty()) ? getItems().get(position).getId() : position;
    }

    private List<Expense> getItems() {
        return items;
    }

    private void setItems(List<Expense> mItems) {
        Collections.sort(mItems, new Comparator<Expense>() {
            @Override
            public int compare(Expense f1, Expense f2) {
                return f2.getDate().compareTo(f1.getDate());
            }
        });
        this.items = mItems;
    }

    class ViewHolder {
        TextView txtInfo;
        TextView txtPrice;
        TextView txtPriceSymbol;
        TextView txtDate;
    }
}

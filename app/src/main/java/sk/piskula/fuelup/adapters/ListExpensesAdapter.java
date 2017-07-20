package sk.piskula.fuelup.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.entity.Expense;
import sk.piskula.fuelup.entity.util.CurrencyUtil;
import sk.piskula.fuelup.entity.util.DateUtil;

/**
 * Created by Martin Styk on 19.06.2017.
 */
public class ListExpensesAdapter extends RecyclerView.Adapter<ListExpensesAdapter.ViewHolder> {

    private static final String TAG = "ListExpensesAdapter";

    private List<Expense> items;
    private Callback callback;


    public ListExpensesAdapter(ListExpensesAdapter.Callback callback) {
        super();
        this.items = new ArrayList<>();
        this.callback = callback;
    }

    public interface Callback {
        void onItemClick(View v, Expense expense, int position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Expense currentItem = items.get(position);
        if (currentItem != null) {
            holder.txtInfo.setText(currentItem.getInfo());
            holder.txtPrice.setText(CurrencyUtil.getPrice(currentItem.getVehicle().getCurrency(), currentItem.getPrice()));
            holder.txtDate.setText(DateUtil.getDateLocalized(currentItem.getDate()));
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

    public void dataChange(List<Expense> items) {
        this.items = items;
        notifyDataSetChanged();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;

        TextView txtInfo;
        TextView txtPrice;
        TextView txtDate;

        ViewHolder(View v) {
            super(v);
            mView = v;
            txtInfo = v.findViewById(R.id.txt_itemexpense_title);
            txtPrice = v.findViewById(R.id.txt_itemexpense_price);
            txtDate = v.findViewById(R.id.txt_itemexpense_date);
        }

    }
}
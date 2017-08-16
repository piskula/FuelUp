package sk.piskula.fuelup.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.data.FuelUpContract.ExpenseEntry;
import sk.piskula.fuelup.entity.Expense;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.entity.util.CurrencyUtil;
import sk.piskula.fuelup.entity.util.DateUtil;

/**
 * Created by Martin Styk on 19.06.2017.
 */
public class ListExpensesAdapter extends RecyclerViewCursorAdapter<ListExpensesAdapter.ExpenseViewHolder> {

    private static final String LOG_TAG = ListExpensesAdapter.class.getSimpleName();
    private Callback mCallback;
    private Vehicle mVehicle;

    public ListExpensesAdapter(Callback callback, Vehicle vehicle) {
        super(null);
        this.mVehicle = vehicle;
        this.mCallback = callback;
    }

    public interface Callback {
        void onItemClick(long expenseId);
    }

    @Override
    public ExpenseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExpenseViewHolder holder, final Cursor cursor) {

        int idColumnIndex = cursor.getColumnIndexOrThrow(ExpenseEntry._ID);
        int infoColumnIndex = cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_INFO);
        int dateColumnIndex = cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_DATE);
        int priceColumnIndex = cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_PRICE);

        final long expenseId = cursor.getLong(idColumnIndex);
        holder.txtInfo.setText(cursor.getString(infoColumnIndex));
        holder.txtDate.setText(DateUtil.getDateLocalized(new Date(cursor.getLong(dateColumnIndex))));
        holder.txtPrice.setText(CurrencyUtil.getPrice(mVehicle.getCurrency(), cursor.getDouble(priceColumnIndex)));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onItemClick(expenseId);
            }
        });
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder
    {
        TextView txtInfo;
        TextView txtPrice;
        TextView txtDate;

        ExpenseViewHolder(View view) {
            super(view);

            txtInfo = view.findViewById(R.id.txt_itemexpense_title);
            txtPrice = view.findViewById(R.id.txt_itemexpense_price);
            txtDate = view.findViewById(R.id.txt_itemexpense_date);
        }

    }
}
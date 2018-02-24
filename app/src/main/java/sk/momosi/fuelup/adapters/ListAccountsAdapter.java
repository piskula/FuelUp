package sk.momosi.fuelup.adapters;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sk.momosi.fuelup.FuelUp;
import sk.momosi.fuelup.R;

/**
 * @author Ondro
 * @version 13.11.2017
 */
public class ListAccountsAdapter extends RecyclerView.Adapter<ListAccountsAdapter.AccountViewHolder> {

    private final Callback mCallback;
    private final List<Account> mAccounts;

    private String chosenAccount = null;

    public ListAccountsAdapter(final Callback callback, final Context context) {
        super();
        this.mAccounts = filterOnlyGoogleAccounts(context);
        this.mCallback = callback;
    }

    @Override
    public AccountViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AccountViewHolder holder, final int position) {
        final String name = mAccounts.get(position).name;
        holder.txtAccount.setText(name);

        int color = name.equals(chosenAccount) ? R.drawable.account_item_shape : R.drawable.account_list_shape;
        holder.itemView.setBackground(FuelUp.getInstance().getResources().getDrawable(color));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseAccount(name);
            }
        });
    }

    public void setChosenAccount(String chosenAccount) {
        if (chosenAccount != null)
            chooseAccount(chosenAccount);
    }

    private void chooseAccount(final @NonNull String name) {
        if (name.equals(chosenAccount))
            chosenAccount = null;
        else
            chosenAccount = name;
        notifyDataSetChanged();
        mCallback.onItemClick(chosenAccount);
    }

    @Override
    public int getItemCount() {
        return mAccounts.size();
    }

    private List<Account> filterOnlyGoogleAccounts(final Context context) {
        List<Account> result = new ArrayList<>();

        for (Account account : AccountManager.get(context).getAccounts()) {
            if ("com.google".equals(account.type)) {
                result.add(account);
            }
        }

        return Collections.unmodifiableList(result);
    }

    public interface Callback {
        void onItemClick(String account);
    }

    class AccountViewHolder extends RecyclerView.ViewHolder
    {
        final TextView txtAccount;

        AccountViewHolder(View view) {
            super(view);
            txtAccount = view.findViewById(R.id.txt_itemaccount_name);
        }
    }

}

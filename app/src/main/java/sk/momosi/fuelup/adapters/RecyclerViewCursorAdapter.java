package sk.momosi.fuelup.adapters;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * @author slightfoot - GitHub https://github.com/slightfoot
 * @version 8.8.2017 by Ondrej Oravcok
 *          <p>
 *          RecyclerView CursorAdapter
 *          Created by Simon on 28/02/2016.
 *          <p>
 *          This code comes from
 *          https://codereview.stackexchange.com/questions/121353/recycler-view-with-cursor-adapter
 */
public abstract class RecyclerViewCursorAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private Cursor mCursor;
    private boolean mDataValid;
    private int mRowIDColumn;


    public RecyclerViewCursorAdapter() {
        setHasStableIds(true);
        swapCursor(null);
    }

    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

    protected abstract void onBindViewHolder(VH holder, Cursor cursor);

    @Override
    public void onBindViewHolder(VH holder, int position) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        onBindViewHolder(holder, mCursor);
    }

    @Override
    public long getItemId(int position) {
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIDColumn);
        }
        return RecyclerView.NO_ID;
    }

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    protected Cursor getCursor() {
        return mCursor;
    }

    public void swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return;
        }
        Cursor oldCursor = mCursor;
        if (oldCursor != null) {
            if (mDataSetObserver != null) {
                oldCursor.unregisterDataSetObserver(mDataSetObserver);
            }
        }
        mCursor = newCursor;
        if (newCursor != null) {
            if (mDataSetObserver != null) {
                newCursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            notifyDataSetChanged();
        } else {
            mRowIDColumn = -1;
            mDataValid = false;
            notifyDataSetChanged();
        }
    }


    private final DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            mDataValid = false;
            notifyDataSetChanged();
        }
    };

}


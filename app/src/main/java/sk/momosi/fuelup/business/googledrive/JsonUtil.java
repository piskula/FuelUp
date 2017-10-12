package sk.momosi.fuelup.business.googledrive;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import sk.momosi.fuelup.data.FuelUpContract;

/**
 * @author Ondrej Oravcok
 * @version 12.10.2017
 */
public class JsonUtil {

    private static final String LOG_TAG = JsonUtil.class.getSimpleName();

    public static String getWholeDbAsJson(final Context context) {
        JSONObject result = new JSONObject();

        try {
            result.put(FuelUpContract.VehicleTypeEntry.TABLE_NAME, getVehicleTypes(context));
            result.put(FuelUpContract.VehicleEntry.TABLE_NAME, getVehicles(context));
            result.put(FuelUpContract.FillUpEntry.TABLE_NAME, getFillUps(context));
            result.put(FuelUpContract.ExpenseEntry.TABLE_NAME, getExpenses(context));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException caught", e);
            return null;
        }

        return result.toString();
    }

    public static String getWholeJsonInputStreamAsString(final InputStream inputStream) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();

        String line;
        while ((line = r.readLine()) != null) {
            total.append(line).append('\n');
        }

        return total.toString();
    }

    private static JSONArray getVehicleTypes(final Context context) {
        Cursor cursor = context.getContentResolver().query(
                FuelUpContract.VehicleTypeEntry.CONTENT_URI,
                FuelUpContract.ALL_COLUMNS_VEHICLE_TYPES,
                null, null, null);

        return getJsonArrayFromCursor(cursor);
    }

    private static JSONArray getVehicles(final Context context) {
        Cursor cursor = context.getContentResolver().query(
                FuelUpContract.VehicleEntry.CONTENT_URI,
                FuelUpContract.ALL_COLUMNS_VEHICLES,
                null, null, null);

        return getJsonArrayFromCursor(cursor);
    }

    private static JSONArray getFillUps(final Context context) {
        Cursor cursor = context.getContentResolver().query(
                FuelUpContract.FillUpEntry.CONTENT_URI,
                FuelUpContract.ALL_COLUMNS_FILLUPS,
                null, null, null);

        return getJsonArrayFromCursor(cursor);
    }

    private static JSONArray getExpenses(final Context context) {
        Cursor cursor = context.getContentResolver().query(
                FuelUpContract.ExpenseEntry.CONTENT_URI,
                FuelUpContract.ALL_COLUMNS_EXPENSES,
                null, null, null);

        return getJsonArrayFromCursor(cursor);
    }

    private static JSONArray getJsonArrayFromCursor(final Cursor cursor) {
        JSONArray resultSet = new JSONArray();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                JSONObject row = new JSONObject();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    if (cursor.getColumnName(i) != null) {
                        try {
                            row.put(cursor.getColumnName(i), cursor.getString(i));
                        } catch (Exception e) {
                            Log.d(LOG_TAG, e.getMessage());
                        }
                    }
                }
                resultSet.put(row);
            }
            cursor.close();
        }

        return resultSet;
    }
}

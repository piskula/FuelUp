package sk.momosi.fuelup.business.googledrive;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.google.android.gms.iid.InstanceID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import sk.momosi.fuelup.data.FuelUpContract;
import sk.momosi.fuelup.data.FuelUpContract.ExpenseEntry;
import sk.momosi.fuelup.data.FuelUpContract.FillUpEntry;
import sk.momosi.fuelup.data.FuelUpContract.VehicleEntry;
import sk.momosi.fuelup.data.FuelUpContract.VehicleTypeEntry;

/**
 * @author Ondrej Oravcok
 * @version 12.10.2017
 */
public class JsonUtil {

    private static final String LOG_TAG = JsonUtil.class.getSimpleName();
    private static final String JSON_VEHICLE = "vehicle";
    public static final String JSON_DEVICE_APP_INSTANCE = "device";

    public static String getWholeDbAsJson(final List<Long> vehicleIds, final Context context) {

        JSONObject result = new JSONObject();
        JSONArray vehicles = new JSONArray();

        try {
            for (Long vehicleId : vehicleIds) {
                JSONObject vehicle = new JSONObject();//getVehicleAsJson(vehicleId, context);

                vehicle.put(JSON_VEHICLE, getVehicleAsJson(vehicleId, context));
                vehicle.put(FillUpEntry.TABLE_NAME, getFillUps(vehicleId, context));
                vehicle.put(ExpenseEntry.TABLE_NAME, getExpenses(vehicleId, context));

                vehicles.put(vehicle);
            }
            result.put(JSON_DEVICE_APP_INSTANCE, InstanceID.getInstance(context).getId());
            result.put(VehicleEntry.TABLE_NAME, vehicles);
            result.put(VehicleTypeEntry.TABLE_NAME, getVehicleTypes(context));

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error while creating JSON representation of DB.", e);
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
                VehicleTypeEntry.CONTENT_URI,
                FuelUpContract.ALL_COLUMNS_VEHICLE_TYPES,
                null, null, null);

        return getJsonArrayFromCursor(cursor);
    }

    private static JSONObject getVehicleAsJson(final Long id, Context context) throws JSONException {
        String[] selectionArgs = { String.valueOf(id) };
        Cursor cursor = context.getContentResolver().query(VehicleEntry.CONTENT_URI,
                FuelUpContract.ALL_COLUMNS_VEHICLES, VehicleEntry._ID + "=?",
                selectionArgs, null);

        JSONObject row = new JSONObject();
        if (cursor != null) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                if (cursor.getColumnName(i) != null && !cursor.getColumnName(i).equals(VehicleEntry._ID)) {
                    row.put(cursor.getColumnName(i), cursor.getString(i));
                }
            }
            cursor.close();
        }

        return row;
    }

    private static JSONArray getFillUps(final Long vehicleId, final Context context) {
        String[] selectionArgs = { String.valueOf(vehicleId) };
        Cursor cursor = context.getContentResolver().query(
                FillUpEntry.CONTENT_URI,
                FuelUpContract.ALL_COLUMNS_FILLUPS, FillUpEntry.COLUMN_VEHICLE + "=?",
                selectionArgs, null);

        return getJsonArrayFromCursor(cursor);
    }

    private static JSONArray getExpenses(final Long vehicleId, final Context context) {
        String[] selectionArgs = { String.valueOf(vehicleId) };
        Cursor cursor = context.getContentResolver().query(
                ExpenseEntry.CONTENT_URI,
                FuelUpContract.ALL_COLUMNS_EXPENSES, ExpenseEntry.COLUMN_VEHICLE + "=?",
                selectionArgs, null);

        return getJsonArrayFromCursor(cursor);
    }

    private static JSONArray getJsonArrayFromCursor(final Cursor cursor) {
        JSONArray resultSet = new JSONArray();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                JSONObject row = new JSONObject();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    if (cursor.getColumnName(i) != null
                            && !cursor.getColumnName(i).equals(ExpenseEntry.COLUMN_VEHICLE)
                            && !cursor.getColumnName(i).equals(FillUpEntry.COLUMN_VEHICLE)) {
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

    public static ArrayList<String> getVehicleNamesFromJson(final JSONObject json) throws JSONException {
        JSONArray vehicles = json.getJSONArray(VehicleEntry.TABLE_NAME);
        ArrayList<String> vehicleNames = new ArrayList<>();
        for (int i = 0; i < vehicles.length(); i++) {
            JSONObject vehicle = vehicles.getJSONObject(i);
            vehicleNames.add(vehicle.getJSONObject(JSON_VEHICLE).getString(VehicleEntry.COLUMN_NAME));
        }
        return vehicleNames;
    }
}

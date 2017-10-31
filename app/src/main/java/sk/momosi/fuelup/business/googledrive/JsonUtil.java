package sk.momosi.fuelup.business.googledrive;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
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
    private static final String JSON_DEVICE_APP_INSTANCE = "device";

    public static String getWholeDbAsJson(final List<Long> vehicleIds, final Context context) {

        JSONObject result = new JSONObject();
        JSONArray vehicles = new JSONArray();

        try {
            for (Long vehicleId : vehicleIds) {
                JSONObject vehicle = new JSONObject();

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
                if (cursor.getColumnName(i) != null
                        && !cursor.getColumnName(i).equals(VehicleEntry._ID)
                        && !cursor.getColumnName(i).equals(VehicleEntry.COLUMN_PICTURE)) {
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
                            && !cursor.getColumnName(i).equals(FillUpEntry.COLUMN_VEHICLE)
                            && !cursor.getColumnName(i).equals(BaseColumns._ID)) {
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

    public static JSONObject getVehicle(final JSONObject json, final String vehicleName) throws JSONException {
        JSONArray vehicles = json.getJSONArray(VehicleEntry.TABLE_NAME);
        for (int i = 0; i < vehicles.length(); i++) {
            JSONObject vehicle = vehicles.getJSONObject(i);
            if (vehicleName.equals(vehicle.getJSONObject(JSON_VEHICLE).getString(VehicleEntry.COLUMN_NAME))) {
                return vehicle;
            }
        }
        throw new JSONException("Vehicle " + vehicleName + "not found in json.");
    }

    public static long importVehicle(final JSONObject vehicleItem, final Context context) throws JSONException {
        ContentValues values = new ContentValues();
        JSONObject vehicle = vehicleItem.getJSONObject(JSON_VEHICLE);

        if (vehicle.has(VehicleEntry.COLUMN_NAME))
            values.put(VehicleEntry.COLUMN_NAME, vehicle.getString(VehicleEntry.COLUMN_NAME));

        if (vehicle.has(VehicleEntry.COLUMN_TYPE))
            values.put(VehicleEntry.COLUMN_TYPE, vehicle.getString(VehicleEntry.COLUMN_TYPE));

        if (vehicle.has(VehicleEntry.COLUMN_VOLUME_UNIT))
            values.put(VehicleEntry.COLUMN_VOLUME_UNIT, vehicle.getString(VehicleEntry.COLUMN_VOLUME_UNIT));

        if (vehicle.has(VehicleEntry.COLUMN_VEHICLE_MAKER))
            values.put(VehicleEntry.COLUMN_VEHICLE_MAKER, vehicle.getString(VehicleEntry.COLUMN_VEHICLE_MAKER));

        if (vehicle.has(VehicleEntry.COLUMN_START_MILEAGE))
            values.put(VehicleEntry.COLUMN_START_MILEAGE, vehicle.getInt(VehicleEntry.COLUMN_START_MILEAGE));

        if (vehicle.has(VehicleEntry.COLUMN_CURRENCY))
            values.put(VehicleEntry.COLUMN_CURRENCY, vehicle.getString(VehicleEntry.COLUMN_CURRENCY));

        if (vehicle.has(VehicleEntry.COLUMN_PICTURE))
            values.put(VehicleEntry.COLUMN_PICTURE, vehicle.getString(VehicleEntry.COLUMN_PICTURE));

        Uri uri = context.getContentResolver().insert(VehicleEntry.CONTENT_URI, values);
        long id = ContentUris.parseId(uri);

        JSONArray fillups = vehicleItem.getJSONArray(FillUpEntry.TABLE_NAME);
        for (int i = 0; i < fillups.length(); i++) {
            JSONObject fillUp = fillups.getJSONObject(i);
            ContentValues fillUpValues = new ContentValues();

            fillUpValues.put(FillUpEntry.COLUMN_VEHICLE, id);

            if (fillUp.has(FillUpEntry.COLUMN_DISTANCE_FROM_LAST))
                fillUpValues.put(FillUpEntry.COLUMN_DISTANCE_FROM_LAST, fillUp.getString(FillUpEntry.COLUMN_DISTANCE_FROM_LAST));

            if (fillUp.has(FillUpEntry.COLUMN_FUEL_VOLUME))
                fillUpValues.put(FillUpEntry.COLUMN_FUEL_VOLUME, fillUp.getDouble(FillUpEntry.COLUMN_FUEL_VOLUME));

            if (fillUp.has(FillUpEntry.COLUMN_FUEL_PRICE_PER_LITRE))
                fillUpValues.put(FillUpEntry.COLUMN_FUEL_PRICE_PER_LITRE, fillUp.getDouble(FillUpEntry.COLUMN_FUEL_PRICE_PER_LITRE));

            if (fillUp.has(FillUpEntry.COLUMN_FUEL_PRICE_TOTAL))
                fillUpValues.put(FillUpEntry.COLUMN_FUEL_PRICE_TOTAL, fillUp.getDouble(FillUpEntry.COLUMN_FUEL_PRICE_TOTAL));

            if (fillUp.has(FillUpEntry.COLUMN_IS_FULL_FILLUP))
                fillUpValues.put(FillUpEntry.COLUMN_IS_FULL_FILLUP, fillUp.getInt(FillUpEntry.COLUMN_IS_FULL_FILLUP));

            if (fillUp.has(FillUpEntry.COLUMN_FUEL_CONSUMPTION))
                fillUpValues.put(FillUpEntry.COLUMN_FUEL_CONSUMPTION, fillUp.getDouble(FillUpEntry.COLUMN_FUEL_CONSUMPTION));

            if (fillUp.has(FillUpEntry.COLUMN_DATE))
                fillUpValues.put(FillUpEntry.COLUMN_DATE, fillUp.getLong(FillUpEntry.COLUMN_DATE));

            if (fillUp.has(FillUpEntry.COLUMN_INFO))
                fillUpValues.put(FillUpEntry.COLUMN_INFO, fillUp.getString(FillUpEntry.COLUMN_INFO));

            context.getContentResolver().insert(FillUpEntry.CONTENT_URI, fillUpValues);
        }

        JSONArray expenses = vehicleItem.getJSONArray(ExpenseEntry.TABLE_NAME);
        for (int i = 0; i < expenses.length(); i++) {
            JSONObject expense = expenses.getJSONObject(i);
            ContentValues expenseValues = new ContentValues();

            expenseValues.put(ExpenseEntry.COLUMN_VEHICLE, id);

            if (expense.has(ExpenseEntry.COLUMN_PRICE))
                expenseValues.put(ExpenseEntry.COLUMN_PRICE, expense.getDouble(ExpenseEntry.COLUMN_PRICE));

            if (expense.has(ExpenseEntry.COLUMN_DATE))
                expenseValues.put(ExpenseEntry.COLUMN_DATE, expense.getLong(ExpenseEntry.COLUMN_DATE));

            if (expense.has(ExpenseEntry.COLUMN_INFO))
                expenseValues.put(ExpenseEntry.COLUMN_INFO, expense.getString(ExpenseEntry.COLUMN_INFO));

            context.getContentResolver().insert(ExpenseEntry.CONTENT_URI, expenseValues);
        }

        return id;
    }
}

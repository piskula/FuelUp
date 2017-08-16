package sk.piskula.fuelup.business;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.sql.SQLException;

import sk.piskula.fuelup.data.FuelUpContract;
import sk.piskula.fuelup.data.FuelUpContract.VehicleTypeEntry;

/**
 * @author Martin Styk
 * @version 16.08.2017
 */
public class VehicleTypeService {

    private static final String LOG_TAG = VehicleTypeService.class.getSimpleName();

    public static String getVehicleTypeNameById(long typeId, Context context) {
        String[] selectionArgs = { String.valueOf(typeId) };
        Cursor cursor = context.getContentResolver().query(VehicleTypeEntry.CONTENT_URI,
                FuelUpContract.ALL_COLUMNS_VEHICLE_TYPES, VehicleTypeEntry._ID + "=?",
                selectionArgs, null);

        if (cursor == null || cursor.getCount() != 1) {
            Log.e(LOG_TAG, "Cannot get VehicleType for id=" + typeId);
            return null;
        }

        cursor.moveToFirst();
        String name = cursor.getString(cursor.getColumnIndexOrThrow(VehicleTypeEntry.COLUMN_NAME));

        cursor.close();
        return name;
    }

}

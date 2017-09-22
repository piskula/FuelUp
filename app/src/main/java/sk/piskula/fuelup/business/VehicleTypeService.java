package sk.piskula.fuelup.business;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import sk.piskula.fuelup.data.FuelUpContract;
import sk.piskula.fuelup.data.FuelUpContract.VehicleTypeEntry;
import sk.piskula.fuelup.entity.VehicleType;

/**
 * @author Martin Styk, Ondrej Oravcok
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

    public static VehicleType getVehicleTypeById(long typeId, Context context) {
        VehicleType vehicleType = new VehicleType();

        vehicleType.setId(typeId);
        vehicleType.setName(getVehicleTypeNameById(typeId, context));

        return vehicleType;
    }

}

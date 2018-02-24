package sk.momosi.fuelup.business;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sk.momosi.fuelup.data.FuelUpContract;
import sk.momosi.fuelup.data.FuelUpContract.VehicleEntry;
import sk.momosi.fuelup.entity.Vehicle;
import sk.momosi.fuelup.entity.VehicleType;
import sk.momosi.fuelup.entity.enums.VolumeUnit;

/**
 * @author Martin Styk, Ondrej Oravcok
 * @version 23.06.2017
 */
public class VehicleService {

    private static final String LOG_TAG = VehicleService.class.getSimpleName();

    public static Vehicle getVehicleById(long id, Context context) {
        String[] selectionArgs = {String.valueOf(id)};
        Cursor cursor = context.getContentResolver().query(VehicleEntry.CONTENT_URI,
                FuelUpContract.ALL_COLUMNS_VEHICLES, VehicleEntry._ID + "=?",
                selectionArgs, null);

        if (cursor == null || cursor.getCount() != 1) {
            Log.e(LOG_TAG, "Cannot get Vehicle for id=" + id);
            return null;
        }

        cursor.moveToFirst();
        int typeId = cursor.getInt(cursor.getColumnIndexOrThrow(VehicleEntry.COLUMN_TYPE));

        Vehicle vehicle = new Vehicle();
        vehicle.setId(cursor.getLong(cursor.getColumnIndexOrThrow(VehicleEntry._ID)));
        vehicle.setName(cursor.getString(cursor.getColumnIndexOrThrow(VehicleEntry.COLUMN_NAME)));
        vehicle.setCurrency(Currency.getInstance(cursor.getString(cursor.getColumnIndexOrThrow(VehicleEntry.COLUMN_CURRENCY))));
        vehicle.setVehicleMaker(cursor.getString(cursor.getColumnIndexOrThrow(VehicleEntry.COLUMN_VEHICLE_MAKER)));
        vehicle.setType(VehicleTypeService.getVehicleTypeById(typeId, context));
        vehicle.setVolumeUnit(getVolumeUnitFromString(cursor.getString(cursor.getColumnIndexOrThrow(VehicleEntry.COLUMN_VOLUME_UNIT))));

        int columnPicturePath = cursor.getColumnIndexOrThrow(VehicleEntry.COLUMN_PICTURE);
        if (cursor.isNull(columnPicturePath)) {
            vehicle.setPathToPicture(null);
        } else {
            vehicle.setPathToPicture(cursor.getString(columnPicturePath));
        }


        int columnMileage = cursor.getColumnIndexOrThrow(VehicleEntry.COLUMN_START_MILEAGE);
        if (cursor.isNull(columnMileage)) {
            vehicle.setStartMileage(null);
        } else {
            vehicle.setStartMileage(cursor.getLong(columnMileage));
        }

        cursor.close();

        return vehicle;
    }

    public static boolean isVehicleNameTaken(String name, Context context) {
        String[] selectionArgs = {name};
        Cursor cursor = context.getContentResolver().query(VehicleEntry.CONTENT_URI,
                FuelUpContract.ALL_COLUMNS_VEHICLES, VehicleEntry.COLUMN_NAME + "=?",
                selectionArgs, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        return false;
    }

    public static Set<String> getAvailableVehicleNames(Context context) {
        String[] projection = {VehicleEntry.COLUMN_NAME};
        Cursor cursor = context.getContentResolver().query(
                VehicleEntry.CONTENT_URI,
                projection, null, null, null);

        Set<String> names = new HashSet<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                names.add(cursor.getString(cursor.getColumnIndex(VehicleEntry.COLUMN_NAME)));
            }
            cursor.close();
        }

        return names;
    }

    public static List<Long> getAvailableVehicleIds(Context context) {
        String[] projection = {VehicleEntry._ID};
        Cursor cursor = context.getContentResolver().query(
                VehicleEntry.CONTENT_URI,
                projection, null, null, null);

        List<Long> ids = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ids.add(cursor.getLong(cursor.getColumnIndex(VehicleEntry._ID)));
            }
            cursor.close();
        }

        return ids;
    }

    private static VehicleType getTypeFromCursor(Cursor cursor) {
        VehicleType vehicleType = new VehicleType();

        vehicleType.setId(cursor.getLong(cursor.getColumnIndexOrThrow(FuelUpContract.VehicleTypeEntry._ID)));
        vehicleType.setName(cursor.getString(cursor.getColumnIndexOrThrow(FuelUpContract.VehicleTypeEntry.COLUMN_NAME)));

        return vehicleType;
    }

    private static VolumeUnit getVolumeUnitFromString(String volumeUnitString) {
        if (VolumeUnit.LITRE.name().equals(volumeUnitString)) {
            return VolumeUnit.LITRE;

        } else if (VolumeUnit.GALLON_UK.name().equals(volumeUnitString)) {
            return VolumeUnit.GALLON_UK;

        } else if (VolumeUnit.GALLON_US.name().equals(volumeUnitString)) {
            return VolumeUnit.GALLON_US;

        } else {
            Log.e(LOG_TAG, "Cannot find correct VolumeUnit for " + volumeUnitString);
            return null;
        }
    }
}

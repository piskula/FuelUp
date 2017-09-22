package sk.piskula.fuelup.business;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.Currency;

import sk.piskula.fuelup.data.FuelUpContract;
import sk.piskula.fuelup.data.FuelUpContract.VehicleEntry;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.entity.VehicleType;
import sk.piskula.fuelup.entity.enums.VolumeUnit;

/**
 * @author Martin Styk
 * @version 23.06.2017
 */
public class VehicleService {

    private static final String LOG_TAG = VehicleService.class.getSimpleName();

    public static Vehicle getVehicleById(long id, Context context) {
        String[] selectionArgs = { String.valueOf(id) };
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
        vehicle.setStartMileage(cursor.getLong(cursor.getColumnIndexOrThrow(VehicleEntry.COLUMN_START_MILEAGE)));
        vehicle.setPathToPicture(cursor.getString(cursor.getColumnIndexOrThrow(VehicleEntry.COLUMN_PICTURE)));

        cursor.close();

        return vehicle;
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

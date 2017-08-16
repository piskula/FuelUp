package sk.piskula.fuelup.data.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Arrays;
import java.util.Currency;
import java.util.List;

import sk.piskula.fuelup.data.DatabaseHelper;
import sk.piskula.fuelup.data.FuelUpContract;
import sk.piskula.fuelup.data.FuelUpContract.ExpenseEntry;
import sk.piskula.fuelup.data.FuelUpContract.FillUpEntry;
import sk.piskula.fuelup.data.FuelUpContract.VehicleEntry;
import sk.piskula.fuelup.entity.enums.VolumeUnit;
import sk.piskula.fuelup.entity.util.CurrencyUtil;

/**
 * @author Ondrej Oravcok
 * @version 7.8.2017
 */
public class VehicleProvider extends ContentProvider {

    public static final String LOG_TAG = VehicleProvider.class.getSimpleName();

    public static final double ERROR = 0.0001d;

    private static final int VEHICLES = 100;
    private static final int VEHICLE_ID = 101;
    private static final int VEHICLE_TYPES = 200;
    private static final int VEHICLE_TYPE_ID = 201;
    private static final int EXPENSES = 300;
    private static final int EXPENSE_ID = 301;
    private static final int FILLUPS = 400;
    private static final int FILLUP_ID = 401;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(FuelUpContract.CONTENT_AUTHORITY, FuelUpContract.PATH_VEHICLES, VEHICLES);
        sUriMatcher.addURI(FuelUpContract.CONTENT_AUTHORITY, FuelUpContract.PATH_VEHICLES + "/#", VEHICLE_ID);
        sUriMatcher.addURI(FuelUpContract.CONTENT_AUTHORITY, FuelUpContract.PATH_VEHICLE_TYPES, VEHICLE_TYPES);
        sUriMatcher.addURI(FuelUpContract.CONTENT_AUTHORITY, FuelUpContract.PATH_VEHICLE_TYPES + "/#", VEHICLE_TYPE_ID);
        sUriMatcher.addURI(FuelUpContract.CONTENT_AUTHORITY, FuelUpContract.PATH_EXPENSES, EXPENSES);
        sUriMatcher.addURI(FuelUpContract.CONTENT_AUTHORITY, FuelUpContract.PATH_EXPENSES + "/#", EXPENSE_ID);
        sUriMatcher.addURI(FuelUpContract.CONTENT_AUTHORITY, FuelUpContract.PATH_FILLUPS, FILLUPS);
        sUriMatcher.addURI(FuelUpContract.CONTENT_AUTHORITY, FuelUpContract.PATH_FILLUPS + "/#", FILLUP_ID);
    }

    private DatabaseHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new DatabaseHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case VEHICLES:
                cursor = database.query(VehicleEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case VEHICLE_ID:
                selection = VehicleEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(VehicleEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case EXPENSES:
                cursor = database.query(ExpenseEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case EXPENSE_ID:
                selection = ExpenseEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(ExpenseEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case FILLUPS:
                cursor = database.query(FillUpEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case FILLUP_ID:
                selection = FillUpEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(FillUpEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case VEHICLE_TYPES:
                cursor = database.query(FuelUpContract.VehicleTypeEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case VEHICLE_TYPE_ID:
                selection = FuelUpContract.VehicleTypeEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(FuelUpContract.VehicleTypeEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case VEHICLES:
                return VehicleEntry.CONTENT_LIST_TYPE;
            case VEHICLE_ID:
                return VehicleEntry.CONTENT_ITEM_TYPE;
            case EXPENSES:
                return ExpenseEntry.CONTENT_LIST_TYPE;
            case EXPENSE_ID:
                return ExpenseEntry.CONTENT_ITEM_TYPE;
            case FILLUPS:
                return FillUpEntry.CONTENT_LIST_TYPE;
            case FILLUP_ID:
                return FillUpEntry.CONTENT_ITEM_TYPE;
            case VEHICLE_TYPES:
                return FuelUpContract.VehicleTypeEntry.CONTENT_LIST_TYPE;
            case VEHICLE_TYPE_ID:
                return FuelUpContract.VehicleTypeEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case VEHICLES:
                return validateVehicleAndInsert(uri, contentValues);
            case EXPENSES:
                return validateExpenseAndInsert(uri, contentValues);
            case FILLUPS:
                return validateFillUpAndInsert(uri, contentValues);
            case VEHICLE_TYPES:
                return validateVehicleTypeAndInsert(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        switch (match) {
            case VEHICLE_ID:
                String[] vehicleId = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return db.delete(VehicleEntry.TABLE_NAME, VehicleEntry._ID + "=?", vehicleId);
            case EXPENSE_ID:
                String[] expenseId = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return db.delete(ExpenseEntry.TABLE_NAME, ExpenseEntry._ID + "=?", expenseId);
            default:
                throw new IllegalArgumentException("Delete is not supported for " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case VEHICLE_ID:
                return validateVehicleAndUpdate(contentValues, ContentUris.parseId(uri));
            case EXPENSE_ID:
                return validateExpenseAndUpdate(contentValues, ContentUris.parseId(uri));
            case FILLUP_ID:
                return validateFillUpAndUpdate(contentValues, ContentUris.parseId(uri));
            case VEHICLE_TYPE_ID:
                return validateVehicleTypeAndInsert(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int validateVehicleTypeAndInsert(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        if (!contentValues.containsKey(FuelUpContract.VehicleTypeEntry.COLUMN_NAME)
                || !isVehicleTypeNameUnique(contentValues.getAsString(FuelUpContract.VehicleTypeEntry.COLUMN_NAME).trim())) {
            throw new IllegalArgumentException("VehicleType name must be filled and unique to update.");
        }

        return mDbHelper.getWritableDatabase().update(
                FuelUpContract.VehicleTypeEntry.TABLE_NAME, contentValues, selection, selectionArgs);
    }

    private int validateFillUpAndUpdate(ContentValues contentValues, long id) {
        
        validateFillUpBasics(contentValues, true);
        
        if (contentValues.containsKey(FillUpEntry.COLUMN_VEHICLE)) {
            throw new IllegalArgumentException("Cannot change vehicle of FillUp. Please, create a fresh new FillUp in this case.");
        }

        final String selection = FillUpEntry._ID + "=?";
        final String[] idArgument = new String[] { String.valueOf(id) };
        
        return mDbHelper.getWritableDatabase().update(
                FillUpEntry.TABLE_NAME, contentValues, selection, idArgument);
    }

    private int validateExpenseAndUpdate(ContentValues contentValues, long id) {

        if (contentValues.containsKey(ExpenseEntry.COLUMN_INFO)) {
            String info = contentValues.getAsString(ExpenseEntry.COLUMN_INFO).trim();
            if (info.isEmpty()) {
                throw new IllegalArgumentException("Expense info must be set.");
            }
        }

        if (contentValues.containsKey(ExpenseEntry.COLUMN_PRICE)) {
            Double price = contentValues.getAsDouble(ExpenseEntry.COLUMN_PRICE);
            if (price == null || price < 0) {
                throw new IllegalArgumentException("Wrong price " + price);
            }
        }

        if (contentValues.containsKey(ExpenseEntry.COLUMN_VEHICLE)) {
            Long vehicleId = contentValues.getAsLong(ExpenseEntry.COLUMN_VEHICLE);
            String selectionVehicle = VehicleEntry._ID + "=?";
            String[] selectionVehicleArgs = new String[] { String.valueOf(vehicleId) };
            Cursor cursor = mDbHelper.getReadableDatabase().query(VehicleEntry.TABLE_NAME, FuelUpContract.ALL_COLUMNS_VEHICLE_TYPES, selectionVehicle, selectionVehicleArgs, null, null, null);
            if (cursor == null || cursor.getCount() != 1) {
                throw new IllegalArgumentException("Vehicle with id=" + vehicleId + " does not exist.");
            }
        }

        final String selection = ExpenseEntry._ID + "=?";
        final String[] idArgument = new String[] { String.valueOf(id) };

        return mDbHelper.getWritableDatabase().update(
                ExpenseEntry.TABLE_NAME, contentValues, selection, idArgument);
    }

    public static final int VEHICLE_UPDATE_NAME_NOT_UNIQUE = 6325;

    private int validateVehicleAndUpdate(final ContentValues contentValues, final long id) {

        if (contentValues.containsKey(VehicleEntry.COLUMN_NAME)) {
            String name = contentValues.getAsString(VehicleEntry.COLUMN_NAME).trim();
            if (!isVehicleNameUnique(name, id)) {
                return VEHICLE_UPDATE_NAME_NOT_UNIQUE;
            }
        }

        if (contentValues.containsKey(VehicleEntry.COLUMN_TYPE)) {
            Integer typeId = contentValues.getAsInteger(VehicleEntry.COLUMN_TYPE);
            if (typeId == null) {
                throw new IllegalArgumentException("Vehicle Type must be set.");
            }
            if (!typeExists(typeId)) {
                throw new IllegalArgumentException("Vehicle Type must exists.");
            }
        }

        if (contentValues.containsKey(VehicleEntry.COLUMN_VOLUME_UNIT)) {
            String volumeUnit = contentValues.getAsString(VehicleEntry.COLUMN_VOLUME_UNIT);
            List<String> allowedVolumeUnits = Arrays.asList(VolumeUnit.LITRE.name(), VolumeUnit.GALLON_UK.name(), VolumeUnit.GALLON_US.name());
            if (volumeUnit == null) {
                throw new IllegalArgumentException("Vehicle volumeUnit must be set.");
            }
            if (!allowedVolumeUnits.contains(volumeUnit)) {
                throw new IllegalArgumentException("VolumeUnit value is not allowed.");
            }
        }

        if (contentValues.containsKey(VehicleEntry.COLUMN_CURRENCY)) {
            String currency = contentValues.getAsString(VehicleEntry.COLUMN_CURRENCY).toUpperCase();
            Currency currencyInstance = Currency.getInstance(currency);
            if (!CurrencyUtil.getSupportedCurrencies().contains(currencyInstance)) {
                throw new IllegalArgumentException("This currency is not supported by this app.");
            }
            if (currencyInstance == null) {
                throw new IllegalArgumentException("Currency '" + currency + "' is not supported by system.");
            }
        }

        final String selection = VehicleEntry._ID + "=?";
        final String[] idArgument = new String[] { String.valueOf(id) };

        return mDbHelper.getWritableDatabase().update(
                VehicleEntry.TABLE_NAME, contentValues, selection, idArgument);
    }

    private Uri validateVehicleAndInsert(Uri uri, ContentValues contentValues) {

        String name = contentValues.getAsString(VehicleEntry.COLUMN_NAME).trim();
        if (!isVehicleNameUnique(name, null)) {
            throw new IllegalArgumentException("Vehicle name must be set and unique.");
        }

        Integer typeId = contentValues.getAsInteger(VehicleEntry.COLUMN_TYPE);
        if (typeId == null) {
            throw new IllegalArgumentException("Vehicle Type must be set.");
        }
        if (!typeExists(typeId)) {
            throw new IllegalArgumentException("Vehicle Type must exists.");
        }

        String volumeUnit = contentValues.getAsString(VehicleEntry.COLUMN_VOLUME_UNIT);
        List<String> allowedVolumeUnits = Arrays.asList(VolumeUnit.LITRE.name(), VolumeUnit.GALLON_UK.name(), VolumeUnit.GALLON_US.name());
        if (volumeUnit == null) {
            throw new IllegalArgumentException("Vehicle volumeUnit must be set.");
        }
        if (!allowedVolumeUnits.contains(volumeUnit)) {
            throw new IllegalArgumentException("VolumeUnit value is not allowed.");
        }

        String currency = contentValues.getAsString(VehicleEntry.COLUMN_CURRENCY).toUpperCase();
        Currency currencyInstance = Currency.getInstance(currency);
        if (!CurrencyUtil.getSupportedCurrencies().contains(currencyInstance)) {
            throw new IllegalArgumentException("This currency is not supported by this app.");
        }
        if (currencyInstance == null) {
            throw new IllegalArgumentException("Currency '" + currency + "' is not supported by system.");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(VehicleEntry.TABLE_NAME, null, contentValues);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    private boolean isVehicleNameUnique(String name, Long ignoreId) {
        String selection = VehicleEntry.COLUMN_NAME + "=?";
        String[] selectionArgs = new String[] { name };

        Cursor cursor = mDbHelper.getReadableDatabase().query(VehicleEntry.TABLE_NAME, FuelUpContract.ALL_COLUMNS_VEHICLES, selection, selectionArgs, null, null, null);
        if (cursor == null) {
            return true;
        }

        if (cursor.getCount() == 0) {
            cursor.close();
            return true;
        } else if (cursor.getCount() == 1 && ignoreId != null) {
            cursor.moveToFirst();
            Long id = cursor.getLong(cursor.getColumnIndexOrThrow(VehicleEntry._ID));
            return ignoreId.equals(id);
        }

        return false;
    }

    private boolean isVehicleTypeNameUnique(String name) {
        String selection = FuelUpContract.VehicleTypeEntry.COLUMN_NAME + "=?";
        String[] selectionArgs = new String[] { name };

        Cursor cursor = mDbHelper.getReadableDatabase().query(FuelUpContract.VehicleTypeEntry.TABLE_NAME, FuelUpContract.ALL_COLUMNS_VEHICLES, selection, selectionArgs, null, null, null);
        return (cursor == null || cursor.getCount() == 0);
    }

    private boolean typeExists(Integer typeId) {
        String selection = FuelUpContract.VehicleTypeEntry._ID + "=?";
        String[] selectionArgs = new String[] { String.valueOf(typeId) };

        Cursor cursor = mDbHelper.getReadableDatabase().query(FuelUpContract.VehicleTypeEntry.TABLE_NAME, FuelUpContract.ALL_COLUMNS_VEHICLE_TYPES, selection, selectionArgs, null, null, null);
        return (cursor != null && cursor.getCount() == 1);
    }

    private Uri validateExpenseAndInsert(Uri uri, ContentValues contentValues) {

        String info = contentValues.getAsString(ExpenseEntry.COLUMN_INFO).trim();
        if (info.isEmpty()) {
            throw new IllegalArgumentException("Expense info must be set.");
        }

        Double price = contentValues.getAsDouble(ExpenseEntry.COLUMN_PRICE);
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Wrong price " + price);
        }

        Long vehicleId = contentValues.getAsLong(ExpenseEntry.COLUMN_VEHICLE);
        String selection = VehicleEntry._ID + "=?";
        String[] selectionArgs = new String[] { String.valueOf(vehicleId) };
        Cursor cursor = mDbHelper.getReadableDatabase().query(VehicleEntry.TABLE_NAME, FuelUpContract.ALL_COLUMNS_VEHICLES, selection, selectionArgs, null, null, null);
        if (cursor == null || cursor.getCount() != 1) {
            throw new IllegalArgumentException("Vehicle with id=" + vehicleId + " does not exist.");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(ExpenseEntry.TABLE_NAME, null, contentValues);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }
    
    private void validateFillUpBasics(ContentValues contentValues, boolean isUpdate) {

        if (contentValues.containsKey(FillUpEntry.COLUMN_DISTANCE_FROM_LAST) || !isUpdate) {
            Long distance = contentValues.getAsLong(FillUpEntry.COLUMN_DISTANCE_FROM_LAST);
            if (distance == null || distance <= 0) {
                throw new IllegalArgumentException("Wrong distance " + distance);
            }
        }

        Double fuelVolume = null;
        if (contentValues.containsKey(FillUpEntry.COLUMN_FUEL_VOLUME) || !isUpdate) {
            fuelVolume = contentValues.getAsDouble(FillUpEntry.COLUMN_FUEL_VOLUME);
            if (fuelVolume == null || fuelVolume <= 0) {
                throw new IllegalArgumentException("Wrong fuelVolume " + fuelVolume);
            }
        }

        Double fuelPricePerLitre = null;
        if (contentValues.containsKey(FillUpEntry.COLUMN_FUEL_PRICE_PER_LITRE) || !isUpdate) {
            fuelPricePerLitre = contentValues.getAsDouble(FillUpEntry.COLUMN_FUEL_PRICE_PER_LITRE);
            if (fuelPricePerLitre == null || fuelPricePerLitre < 0) {
                throw new IllegalArgumentException("Wrong fuelPricePerLitre " + fuelPricePerLitre);
            }
        }

        Double fuelPriceTotal = null;
        if (contentValues.containsKey(FillUpEntry.COLUMN_FUEL_PRICE_TOTAL) || !isUpdate) {
            fuelPriceTotal = contentValues.getAsDouble(FillUpEntry.COLUMN_FUEL_PRICE_TOTAL);
            if (fuelPriceTotal == null || fuelPriceTotal < 0) {
                throw new IllegalArgumentException("Wrong fuelPriceTotal " + fuelPriceTotal);
            }
        }

        if (fuelVolume != null || fuelPricePerLitre != null || fuelPriceTotal != null) {
            if (Math.abs(fuelPriceTotal - (fuelPricePerLitre * fuelVolume)) > ERROR) {
                throw new IllegalArgumentException("Fuel priceTotal must equals (pricePerLitre * fuelVolume)");
            }
        }

        // TODO recompute consumption

        if (contentValues.containsKey(FillUpEntry.COLUMN_IS_FULL_FILLUP) || !isUpdate) {
            Long isFullFillUp = contentValues.getAsLong(FillUpEntry.COLUMN_IS_FULL_FILLUP);
            if (isFullFillUp == null || isFullFillUp < 0 || isFullFillUp > 1) {
                throw new IllegalArgumentException("Illegal value in isFullFillUp flag - only 0 or 1 is possible");
            }
        }

        if (contentValues.containsKey(FillUpEntry.COLUMN_DATE) || !isUpdate) {
            Long timestamp = contentValues.getAsLong(FillUpEntry.COLUMN_DATE);
            if (timestamp == null) {
                throw new IllegalArgumentException("Date must be filled.");
            }
        }

    }

    private Uri validateFillUpAndInsert(Uri uri, ContentValues contentValues) {
        
        validateFillUpBasics(contentValues, false);

        Long vehicleId = contentValues.getAsLong(FillUpEntry.COLUMN_VEHICLE);
        String selection = VehicleEntry._ID + "=?";
        String[] selectionArgs = new String[] { String.valueOf(vehicleId) };
        Cursor cursor = mDbHelper.getReadableDatabase().query(VehicleEntry.TABLE_NAME, FuelUpContract.ALL_COLUMNS_VEHICLES, selection, selectionArgs, null, null, null);
        if (cursor == null || cursor.getCount() != 1) {
            throw new IllegalArgumentException("Vehicle with id=" + vehicleId + " does not exist.");
        }

        cursor.moveToFirst();
        String volumeUnit = cursor.getString(cursor.getColumnIndexOrThrow(VehicleEntry.COLUMN_VOLUME_UNIT));
        cursor.close();

        boolean isFull = 1 == contentValues.getAsLong(FillUpEntry.COLUMN_IS_FULL_FILLUP);

        Double consumption = contentValues.getAsDouble(FillUpEntry.COLUMN_FUEL_CONSUMPTION);
        if (isFull) {
            // TODO update previous fillUps
            /*if (VolumeUnit.LITRE.name().equals(volumeUnit)) {
            // control correct computing
                if (Math.abs(consumption - ((fuelVolume * 100) / distance)) > ERROR) {
                    throw new IllegalArgumentException("Fuel consumption does not fit against fuelVolume and distance!");
                }
            } else {
                if (Math.abs(consumption - (distance / fuelVolume)) > ERROR) {
                    throw new IllegalArgumentException("Fuel consumption does not fit against fuelVolume and distance!");
                }
            }*/
        } else {
            // if inserting not full, consumption must be null
            if (consumption != null) {
                throw new IllegalArgumentException("When inserting notFull FuelUp, consumption must be unknown.");
            }
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(FillUpEntry.TABLE_NAME, null, contentValues);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    private Uri validateVehicleTypeAndInsert(Uri uri, ContentValues contentValues) {
        String name = contentValues.getAsString(FuelUpContract.VehicleTypeEntry.COLUMN_NAME).trim();
        if (!isVehicleTypeNameUnique(name)) {
            long id = mDbHelper.getWritableDatabase().insert(FuelUpContract.VehicleTypeEntry.TABLE_NAME, null, contentValues);
            if (id == -1) {
                Log.e(LOG_TAG, "Failed to insert row for " + uri);
                return null;
            }
            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, id);
        } else {
            throw new IllegalArgumentException("VehicleType is not unique.");
        }
    }
}

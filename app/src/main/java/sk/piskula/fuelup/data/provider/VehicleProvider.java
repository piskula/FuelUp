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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;

import sk.piskula.fuelup.business.FillUpService;
import sk.piskula.fuelup.business.VehicleService;
import sk.piskula.fuelup.data.DatabaseHelper;
import sk.piskula.fuelup.data.FuelUpContract;
import sk.piskula.fuelup.data.FuelUpContract.ExpenseEntry;
import sk.piskula.fuelup.data.FuelUpContract.FillUpEntry;
import sk.piskula.fuelup.data.FuelUpContract.VehicleEntry;
import sk.piskula.fuelup.entity.FillUp;
import sk.piskula.fuelup.entity.enums.VolumeUnit;
import sk.piskula.fuelup.entity.util.CurrencyUtil;

/**
 * @author Ondrej Oravcok
 * @version 7.8.2017
 */
public class VehicleProvider extends ContentProvider {

    public static final String LOG_TAG = VehicleProvider.class.getSimpleName();

    public static final double ERROR = 0.005d;

    private static final int VEHICLES = 100;
    private static final int VEHICLE_ID = 101;
    private static final int VEHICLE_TYPES = 200;
    private static final int VEHICLE_TYPE_ID = 201;
    private static final int EXPENSES = 300;
    private static final int EXPENSE_ID = 301;
    private static final int FILLUPS = 400;
    private static final int FILLUP_ID = 401;

    public static final int UPDATE_NO_CHANGE = 492;

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
                return validateFillUpAndInsertInTransaction(uri, contentValues, null);
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
            case FILLUP_ID:
                return deleteFillUpInTransaction(ContentUris.parseId(uri), null);
            default:
                throw new IllegalArgumentException("Delete is not supported for " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case VEHICLE_ID:
                return validateVehicleAndUpdate(uri, contentValues, ContentUris.parseId(uri));
            case EXPENSE_ID:
                return validateExpenseAndUpdate(uri, contentValues, ContentUris.parseId(uri));
            case FILLUP_ID:
                return validateFillUpAndUpdate(uri, contentValues, ContentUris.parseId(uri));
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int validateFillUpAndUpdate(final Uri uri, final ContentValues contentValues, long id) {

        if (contentValues.size() == 0) {
            return UPDATE_NO_CHANGE;
        }
        
        validateFillUpBasics(contentValues, true, id);
        
        if (contentValues.containsKey(FillUpEntry.COLUMN_VEHICLE)) {
            throw new IllegalArgumentException("Cannot change vehicle of FillUp. Please, create a fresh new FillUp in this case.");
        }

        boolean isDeleteAndInsertNeeded = contentValues.containsKey(FillUpEntry.COLUMN_DATE)
                || contentValues.containsKey(FillUpEntry.COLUMN_FUEL_VOLUME)
                || contentValues.containsKey(FillUpEntry.COLUMN_DISTANCE_FROM_LAST)
                || contentValues.containsKey(FillUpEntry.COLUMN_IS_FULL_FILLUP);

        if (!isDeleteAndInsertNeeded) {
            // if only Not-Neighbour-Affecting values have been changed
            final String selection = FillUpEntry._ID + "=?";
            final String[] idArgument = new String[] { String.valueOf(id) };

            getContext().getContentResolver().notifyChange(uri, null);
            return mDbHelper.getWritableDatabase().update(
                    FillUpEntry.TABLE_NAME, contentValues, selection, idArgument);
        } else {
            // if date, distance or fuelVolume have been changed, it may affect neighbouring fillUps
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            db.beginTransactionNonExclusive();

            FillUp fillUp = FillUpService.getFillUpById(id, getContext());

            ContentValues recreatedValues = new ContentValues();
            recreatedValues.putAll(contentValues);
            if (!recreatedValues.containsKey(FillUpEntry.COLUMN_VEHICLE))
                recreatedValues.put(FillUpEntry.COLUMN_VEHICLE, fillUp.getVehicle().getId());
            if (!recreatedValues.containsKey(FillUpEntry.COLUMN_DISTANCE_FROM_LAST))
                recreatedValues.put(FillUpEntry.COLUMN_DISTANCE_FROM_LAST, fillUp.getDistanceFromLastFillUp());
            if (!recreatedValues.containsKey(FillUpEntry.COLUMN_FUEL_VOLUME))
                recreatedValues.put(FillUpEntry.COLUMN_FUEL_VOLUME, fillUp.getFuelVolume().doubleValue());
            if (!recreatedValues.containsKey(FillUpEntry.COLUMN_FUEL_PRICE_PER_LITRE))
                recreatedValues.put(FillUpEntry.COLUMN_FUEL_PRICE_PER_LITRE, fillUp.getFuelPricePerLitre().doubleValue());
            if (!recreatedValues.containsKey(FillUpEntry.COLUMN_FUEL_PRICE_TOTAL))
                recreatedValues.put(FillUpEntry.COLUMN_FUEL_PRICE_TOTAL, fillUp.getFuelPriceTotal().doubleValue());
            if (!recreatedValues.containsKey(FillUpEntry.COLUMN_IS_FULL_FILLUP))
                recreatedValues.put(FillUpEntry.COLUMN_IS_FULL_FILLUP, fillUp.isFullFillUp() ? 1 : 0);
            if (!recreatedValues.containsKey(FillUpEntry.COLUMN_DATE))
                recreatedValues.put(FillUpEntry.COLUMN_DATE, fillUp.getDate().getTime());
            if (!recreatedValues.containsKey(FillUpEntry.COLUMN_INFO))
                recreatedValues.put(FillUpEntry.COLUMN_INFO, fillUp.getInfo());

            deleteFillUpInTransaction(id, db);
            validateFillUpAndInsertInTransaction(uri, recreatedValues, db);

            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();

            return 1;
        }
    }

    private int validateExpenseAndUpdate(final Uri uri, final ContentValues contentValues, long id) {

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

        getContext().getContentResolver().notifyChange(uri, null);
        return mDbHelper.getWritableDatabase().update(
                ExpenseEntry.TABLE_NAME, contentValues, selection, idArgument);
    }

    public static final int VEHICLE_UPDATE_NAME_NOT_UNIQUE = 6325;

    private int validateVehicleAndUpdate(final Uri uri, final ContentValues contentValues, final long id) {

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

        getContext().getContentResolver().notifyChange(uri, null);
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
    
    private void validateFillUpBasics(ContentValues contentValues, boolean isUpdate, Long existingFillUpId) {

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


        if (isUpdate) {
            /* we must handle three types of situations while updating
             *  - updating only fuelVolume -> we must update also PriceTotal
             *  - updating only pricePerLitre -> we have to update priceTotal
             *  - updating only priceTotal -> we have to update pricePerLitre
             */
            FillUp updatingFillUp = FillUpService.getFillUpById(existingFillUpId, getContext());

            if (fuelVolume != null || fuelPricePerLitre != null || fuelPriceTotal != null) {
                // at least on of important value has changed

                // if only price changed
                if (fuelVolume == null) {
                    fuelVolume = updatingFillUp.getFuelVolume().doubleValue();

                    // we must update price total if pricePerLitre changed
                    if (fuelPriceTotal == null) {
                        fuelPriceTotal = fuelPricePerLitre * fuelVolume;
                        contentValues.put(FillUpEntry.COLUMN_FUEL_PRICE_TOTAL, fuelPriceTotal);
                    }

                    // we must update pricePerLitre if priceTotal changed
                    else if (fuelPricePerLitre == null) {
                        fuelPricePerLitre = fuelPriceTotal / fuelVolume;
                        contentValues.put(FillUpEntry.COLUMN_FUEL_PRICE_PER_LITRE, fuelPricePerLitre);
                    }

                    // we must only check correctness if both prices changed
                    else {
                        if (Math.abs(fuelPriceTotal - (fuelPricePerLitre * fuelVolume)) > ERROR) {
                            throw new IllegalArgumentException("Fuel priceTotal must equals (pricePerLitre * fuelVolume)");
                        }
                    }
                }

                else if (fuelPriceTotal == null) {
                    // fuelVolume changed and fuelPriceTotal NOT
                    fuelPriceTotal = updatingFillUp.getFuelPriceTotal().doubleValue();

                    if (fuelPricePerLitre == null) {
                        throw new IllegalArgumentException("Cannot update fuelVolume without changing priceTotal or pricePerLitre.");
                    } else {
                        if (Math.abs(fuelPriceTotal - (fuelPricePerLitre * fuelVolume)) > ERROR) {
                            throw new IllegalArgumentException("Fuel priceTotal must equals (pricePerLitre * fuelVolume)");
                        }
                    }
                }

                else {
                    // fuelVol changed and priceTotal changed also

                    if (fuelPricePerLitre == null) {
                        fuelPricePerLitre = updatingFillUp.getFuelPricePerLitre().doubleValue();
                    }

                    if (Math.abs(fuelPriceTotal - (fuelPricePerLitre * fuelVolume)) > ERROR) {
                        throw new IllegalArgumentException("Fuel priceTotal must equals (pricePerLitre * fuelVolume)");
                    }
                }
            }

        } else {
            // when inserting, all values must be filled
            if (fuelVolume != null || fuelPricePerLitre != null || fuelPriceTotal != null) {
                if (fuelVolume == null || fuelPricePerLitre == null || fuelPriceTotal == null
                        || Math.abs(fuelPriceTotal - (fuelPricePerLitre * fuelVolume)) > ERROR) {
                    throw new IllegalArgumentException("Fuel priceTotal must equals (pricePerLitre * fuelVolume)");
                }
            }
        }

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

    private Uri insertFullValidatedFillUp(Uri uri, ContentValues contentValues, SQLiteDatabase transaction) {
        long vehicleId = contentValues.getAsLong(FillUpEntry.COLUMN_VEHICLE);
        long timestamp = contentValues.getAsLong(FillUpEntry.COLUMN_DATE);

        String selectionOlder = FillUpEntry.COLUMN_VEHICLE + "=? AND " + FillUpEntry.COLUMN_DATE + "<?";
        String[] selectionOlderArgs = new String[] { String.valueOf(vehicleId), String.valueOf(timestamp) };

        Cursor cursorOlderFillUps = mDbHelper.getReadableDatabase().query(
                FillUpEntry.TABLE_NAME,
                FuelUpContract.ALL_COLUMNS_FILLUPS,
                selectionOlder,
                selectionOlderArgs,
                null,
                null,
                FillUpEntry.COLUMN_DATE + " DESC");

        // find first full older fillUp
        List<Long> olderIds = new ArrayList<>();
        BigDecimal olderFuelUpsVol = BigDecimal.valueOf(contentValues.getAsDouble(FillUpEntry.COLUMN_FUEL_VOLUME));
        Long olderFuelUpsDistance = contentValues.getAsLong(FillUpEntry.COLUMN_DISTANCE_FROM_LAST);
        boolean existsOlderFullFillUp = false;

        while (cursorOlderFillUps.moveToNext()) {
            if (1 == cursorOlderFillUps.getInt(cursorOlderFillUps.getColumnIndexOrThrow(FillUpEntry.COLUMN_IS_FULL_FILLUP))) {
                existsOlderFullFillUp = true;
                break;
            }
            olderIds.add(cursorOlderFillUps.getLong(cursorOlderFillUps.getColumnIndexOrThrow(FillUpEntry._ID)));
            olderFuelUpsDistance += cursorOlderFillUps.getLong(cursorOlderFillUps.getColumnIndexOrThrow(FillUpEntry.COLUMN_DISTANCE_FROM_LAST));
            olderFuelUpsVol = olderFuelUpsVol.add(BigDecimal.valueOf(cursorOlderFillUps.getDouble(cursorOlderFillUps.getColumnIndexOrThrow(FillUpEntry.COLUMN_FUEL_VOLUME))));
        }
        cursorOlderFillUps.close();

        VolumeUnit unit = VehicleService.getVehicleById(vehicleId, getContext()).getVolumeUnit();
        BigDecimal avgOlderConsumption = existsOlderFullFillUp ? FillUpService.getConsumptionFromVolumeDistance(olderFuelUpsVol, olderFuelUpsDistance, unit) : null;
        Double avgOlderConsumptionDouble = avgOlderConsumption == null ? null : avgOlderConsumption.doubleValue();

        // we can already be in transaction, when e.g. updating fillUp
        boolean isOutsideTransaction = transaction != null;
        SQLiteDatabase db;
        if (isOutsideTransaction) {
            db = transaction;
        } else {
            db = mDbHelper.getWritableDatabase();
            db.beginTransactionNonExclusive();
        }

        // insert new fillUp with fuel consumption
        contentValues.put(FillUpEntry.COLUMN_FUEL_CONSUMPTION, avgOlderConsumptionDouble);
        long id = db.insert(FillUpEntry.TABLE_NAME, null, contentValues);
        if (id == -1) {
            if (!isOutsideTransaction) {
                db.endTransaction();
                db.close();
            }
            Log.e(LOG_TAG, "Cannot insert FillUp.");
            throw new IllegalArgumentException("Cannot insert new FillUp.");
        }

        if (existsOlderFullFillUp) {
            // if older full fillUp exists, compute consumption for all
            ContentValues contentValuesUpdate = new ContentValues();
            contentValuesUpdate.put(FillUpEntry.COLUMN_FUEL_CONSUMPTION, avgOlderConsumptionDouble);
            String selectionUpdate = FillUpEntry._ID + "=?";
            String[] selectionUpdateArgs;

            // and update all not full until it
            for (Long fillUpId : olderIds) {
                selectionUpdateArgs = new String[] { String.valueOf(fillUpId) };
                db.update(
                        FillUpEntry.TABLE_NAME,
                        contentValuesUpdate,
                        selectionUpdate,
                        selectionUpdateArgs);
            }
        }

        // find first full newer fillUp in case not inserting the newest fillUp
        String selectionNewer = FillUpEntry.COLUMN_VEHICLE + "=? AND " + FillUpEntry.COLUMN_DATE + ">?";
        String[] selectionNewerArgs = new String[] { String.valueOf(vehicleId), String.valueOf(timestamp) };

        Cursor cursorNewerFillUps = mDbHelper.getReadableDatabase().query(
                FillUpEntry.TABLE_NAME,
                FuelUpContract.ALL_COLUMNS_FILLUPS,
                selectionNewer,
                selectionNewerArgs,
                null,
                null,
                FillUpEntry.COLUMN_DATE + " ASC");

        List<Long> newerIds = new ArrayList<>();
        BigDecimal newerFuelUpsVol = BigDecimal.ZERO;
        Long newerFuelUpsDistance = 0L;
        boolean existsNewerFullFillUp = false;

        while (cursorNewerFillUps.moveToNext()) {
            newerIds.add(cursorNewerFillUps.getLong(cursorNewerFillUps.getColumnIndexOrThrow(FillUpEntry._ID)));
            newerFuelUpsDistance += cursorNewerFillUps.getLong(cursorNewerFillUps.getColumnIndexOrThrow(FillUpEntry.COLUMN_DISTANCE_FROM_LAST));
            newerFuelUpsVol = newerFuelUpsVol.add(BigDecimal.valueOf(cursorNewerFillUps.getDouble(cursorNewerFillUps.getColumnIndexOrThrow(FillUpEntry.COLUMN_FUEL_VOLUME))));
            if (1 == cursorNewerFillUps.getInt(cursorNewerFillUps.getColumnIndexOrThrow(FillUpEntry.COLUMN_IS_FULL_FILLUP))) {
                existsNewerFullFillUp = true;
                break;
            }
        }
        cursorNewerFillUps.close();

        BigDecimal avgNewerConsumption = !existsNewerFullFillUp ? null :
                FillUpService.getConsumptionFromVolumeDistance(newerFuelUpsVol, newerFuelUpsDistance, unit);

        if (existsNewerFullFillUp) {
            // if newer full fillUp exists, compute consumption for all including that one full
            ContentValues contentValuesUpdate = new ContentValues();
            contentValuesUpdate.put(FillUpEntry.COLUMN_FUEL_CONSUMPTION, avgNewerConsumption.doubleValue());
            String selectionUpdate = FillUpEntry._ID + "=?";
            String[] selectionUpdateArgs;

            // and update all not full until it including it
            for (Long fillUpId : newerIds) {
                selectionUpdateArgs = new String[] { String.valueOf(fillUpId) };
                db.update(
                        FillUpEntry.TABLE_NAME,
                        contentValuesUpdate,
                        selectionUpdate,
                        selectionUpdateArgs);
            }
        }

        if (!isOutsideTransaction) {
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertNotFullValidatedFillUp(final Uri uri, final ContentValues contentValues,
                                             final SQLiteDatabase transaction) {

        // we can already be in transaction, when e.g. updating fillUp
        boolean isOutsideTransaction = transaction != null;
        SQLiteDatabase db;
        if (isOutsideTransaction) {
            db = transaction;
        } else {
            db = mDbHelper.getWritableDatabase();
            db.beginTransactionNonExclusive();
        }

        // first insert fillUp
        long id = db.insert(FillUpEntry.TABLE_NAME, null, contentValues);

        long vehicleId = contentValues.getAsLong(FillUpEntry.COLUMN_VEHICLE);
        long timestamp = contentValues.getAsLong(FillUpEntry.COLUMN_DATE);

        // then recalculate consumption
        // firstly count newer fillUps
        String selectionNewer = FillUpEntry.COLUMN_VEHICLE + "=? AND " + FillUpEntry.COLUMN_DATE + ">=?";
        String[] selectionNewerArgs = new String[] { String.valueOf(vehicleId), String.valueOf(timestamp) };

        Cursor cursorNewerFillUps = mDbHelper.getReadableDatabase().query(
                FillUpEntry.TABLE_NAME,
                FuelUpContract.ALL_COLUMNS_FILLUPS,
                selectionNewer,
                selectionNewerArgs,
                null,
                null,
                FillUpEntry.COLUMN_DATE + " ASC");

        // find fillups until full newer fillUp
        List<Long> newerIds = new ArrayList<>();
        BigDecimal newerFuelUpsVol = BigDecimal.ZERO;
        Long newerFuelUpsDistance = 0L;
        boolean existsNewerFullFillUp = false;

        while (cursorNewerFillUps.moveToNext()) {
            newerIds.add(cursorNewerFillUps.getLong(cursorNewerFillUps.getColumnIndexOrThrow(FillUpEntry._ID)));
            newerFuelUpsDistance += cursorNewerFillUps.getLong(cursorNewerFillUps.getColumnIndexOrThrow(FillUpEntry.COLUMN_DISTANCE_FROM_LAST));
            newerFuelUpsVol = newerFuelUpsVol.add(BigDecimal.valueOf(cursorNewerFillUps.getDouble(cursorNewerFillUps.getColumnIndexOrThrow(FillUpEntry.COLUMN_FUEL_VOLUME))));
            if (1 == cursorNewerFillUps.getInt(cursorNewerFillUps.getColumnIndexOrThrow(FillUpEntry.COLUMN_IS_FULL_FILLUP))) {
                existsNewerFullFillUp = true;
                break;
            }
        }
        cursorNewerFillUps.close();

        // if there is no newer full fill up, we do not compute consumption
        if (!existsNewerFullFillUp) {
            if (!isOutsideTransaction) {
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }

            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, id);
        }

        // secondly count older fillUps until full (respecting this)
        String selectionOlder = FillUpEntry.COLUMN_VEHICLE + "=? AND " + FillUpEntry.COLUMN_DATE + "<?";
        String[] selectionOlderArgs = new String[] { String.valueOf(vehicleId), String.valueOf(timestamp) };

        Cursor cursorOlderFillUps = mDbHelper.getReadableDatabase().query(
                FillUpEntry.TABLE_NAME,
                FuelUpContract.ALL_COLUMNS_FILLUPS,
                selectionOlder,
                selectionOlderArgs,
                null,
                null,
                FillUpEntry.COLUMN_DATE + " DESC");

        // find fillUps until first full older fillUp (without that one full)
        List<Long> olderIds = new ArrayList<>();
        BigDecimal olderFuelUpsVol = BigDecimal.ZERO;
        Long olderFuelUpsDistance = 0L;
        boolean existsOlderFullFillUp = false;

        while (cursorOlderFillUps.moveToNext()) {
            if (1 == cursorOlderFillUps.getInt(cursorOlderFillUps.getColumnIndexOrThrow(FillUpEntry.COLUMN_IS_FULL_FILLUP))) {
                existsOlderFullFillUp = true;
                break;
            }
            olderIds.add(cursorOlderFillUps.getLong(cursorOlderFillUps.getColumnIndexOrThrow(FillUpEntry._ID)));
            olderFuelUpsDistance += cursorOlderFillUps.getLong(cursorOlderFillUps.getColumnIndexOrThrow(FillUpEntry.COLUMN_DISTANCE_FROM_LAST));
            olderFuelUpsVol = olderFuelUpsVol.add(BigDecimal.valueOf(cursorOlderFillUps.getDouble(cursorOlderFillUps.getColumnIndexOrThrow(FillUpEntry.COLUMN_FUEL_VOLUME))));
        }
        cursorOlderFillUps.close();

        // if there is no older full fill up, we do not compute consumption
        if (!existsOlderFullFillUp) {
            if (!isOutsideTransaction) {
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }

            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, id);
        }

        // update all neighbouring fillUps with new consumption
        // compute consumption for all
        VolumeUnit unit = VehicleService.getVehicleById(vehicleId, getContext()).getVolumeUnit();
        BigDecimal fuelVol = newerFuelUpsVol.add(olderFuelUpsVol);
        Long distance = newerFuelUpsDistance + olderFuelUpsDistance;
        BigDecimal avgConsumption = FillUpService.getConsumptionFromVolumeDistance(fuelVol, distance, unit);

        ContentValues contentValuesUpdate = new ContentValues();
        contentValuesUpdate.put(FillUpEntry.COLUMN_FUEL_CONSUMPTION, avgConsumption.doubleValue());

        newerIds.addAll(olderIds);
        // and update all not full until it including it
        for (Long fillUpId : newerIds) {
            db.update(
                    FillUpEntry.TABLE_NAME,
                    contentValuesUpdate,
                    FillUpEntry._ID + "=?",
                    new String[] { String.valueOf(fillUpId) });
        }

        if (!isOutsideTransaction) {
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    private int deleteFillUpInTransaction(final Long fillUpId, final SQLiteDatabase transaction) {
        FillUp fillUp = FillUpService.getFillUpById(fillUpId, getContext());
        if (fillUp == null) {
            Log.e(LOG_TAG, "Cannot remove not existing fillUp (id=" + fillUpId + ")");
            return -1;
        }

        long vehicleId = fillUp.getVehicle().getId();
        long timestamp = fillUp.getDate().getTime();

        List<Long> ids = new ArrayList<>();
        BigDecimal fuelUpsVol = BigDecimal.ZERO;
        Long fuelUpsDistance = 0L;

        String selectionOlder = FillUpEntry.COLUMN_VEHICLE + "=? AND " + FillUpEntry.COLUMN_DATE + "<?";
        String[] selectionOlderArgs = new String[] { String.valueOf(vehicleId), String.valueOf(timestamp) };

        Cursor cursorOlderFillUps = mDbHelper.getReadableDatabase().query(
                FillUpEntry.TABLE_NAME,
                FuelUpContract.ALL_COLUMNS_FILLUPS,
                selectionOlder,
                selectionOlderArgs,
                null,
                null,
                FillUpEntry.COLUMN_DATE + " DESC");

        // find first full older fillUp
        boolean existsOlderFullFillUp = false;
        while (cursorOlderFillUps.moveToNext()) {
            if (1 == cursorOlderFillUps.getInt(cursorOlderFillUps.getColumnIndexOrThrow(FillUpEntry.COLUMN_IS_FULL_FILLUP))) {
                existsOlderFullFillUp = true;
                break;
            }
            ids.add(cursorOlderFillUps.getLong(cursorOlderFillUps.getColumnIndexOrThrow(FillUpEntry._ID)));
            fuelUpsDistance += cursorOlderFillUps.getLong(cursorOlderFillUps.getColumnIndexOrThrow(FillUpEntry.COLUMN_DISTANCE_FROM_LAST));
            fuelUpsVol = fuelUpsVol.add(BigDecimal.valueOf(cursorOlderFillUps.getDouble(cursorOlderFillUps.getColumnIndexOrThrow(FillUpEntry.COLUMN_FUEL_VOLUME))));
        }
        cursorOlderFillUps.close();

        String selectionNewer = FillUpEntry.COLUMN_VEHICLE + "=? AND " + FillUpEntry.COLUMN_DATE + ">?";
        String[] selectionNewerArgs = new String[] { String.valueOf(vehicleId), String.valueOf(timestamp) };

        Cursor cursorNewerFillUps = mDbHelper.getReadableDatabase().query(
                FillUpEntry.TABLE_NAME,
                FuelUpContract.ALL_COLUMNS_FILLUPS,
                selectionNewer,
                selectionNewerArgs,
                null,
                null,
                FillUpEntry.COLUMN_DATE + " ASC");

        // find first newer full FillUp
        boolean existsNewerFullFillUp = false;
        while (cursorNewerFillUps.moveToNext()) {
            ids.add(cursorNewerFillUps.getLong(cursorNewerFillUps.getColumnIndexOrThrow(FillUpEntry._ID)));
            fuelUpsDistance += cursorNewerFillUps.getLong(cursorNewerFillUps.getColumnIndexOrThrow(FillUpEntry.COLUMN_DISTANCE_FROM_LAST));
            fuelUpsVol = fuelUpsVol.add(BigDecimal.valueOf(cursorNewerFillUps.getDouble(cursorNewerFillUps.getColumnIndexOrThrow(FillUpEntry.COLUMN_FUEL_VOLUME))));
            if (1 == cursorNewerFillUps.getInt(cursorNewerFillUps.getColumnIndexOrThrow(FillUpEntry.COLUMN_IS_FULL_FILLUP))) {
                existsNewerFullFillUp = true;
                break;
            }
        }
        cursorNewerFillUps.close();

        VolumeUnit unit = fillUp.getVehicle().getVolumeUnit();
        BigDecimal avgConsumption = null;

        if (existsNewerFullFillUp && existsOlderFullFillUp) {   // when newer or older does not exist, we set null to all fillUps
            avgConsumption = FillUpService.getConsumptionFromVolumeDistance(fuelUpsVol, fuelUpsDistance, unit);
        }

        ContentValues contentValuesUpdate = new ContentValues();
        contentValuesUpdate.put(
                FillUpEntry.COLUMN_FUEL_CONSUMPTION,
                avgConsumption == null ? null : avgConsumption.doubleValue());

        boolean isOutsideTransaction = transaction != null; // we can already be in transaction, when e.g. updating fillUp
        SQLiteDatabase db;
        if (isOutsideTransaction) {
            db = transaction;
        } else {
            db = mDbHelper.getWritableDatabase();
            db.beginTransactionNonExclusive();
        }

        int result = db.delete(
                FillUpEntry.TABLE_NAME,
                FillUpEntry._ID + "=?",
                new String[] { String.valueOf(fillUpId) }
        );

        // and update all not full until it including it
        for (Long id : ids) {
            db.update(
                    FillUpEntry.TABLE_NAME,
                    contentValuesUpdate,
                    FillUpEntry._ID + "=?",
                    new String[] { String.valueOf(id) });
        }

        if (!isOutsideTransaction) {
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }

        return result;
    }

    private Uri validateFillUpAndInsertInTransaction(Uri uri, ContentValues contentValues, SQLiteDatabase transaction) {
        
        validateFillUpBasics(contentValues, false, null);

        boolean isFullFillUp = 1 == contentValues.getAsInteger(FillUpEntry.COLUMN_IS_FULL_FILLUP);
        if (isFullFillUp) {
            return insertFullValidatedFillUp(uri, contentValues, transaction);
        } else {
            return insertNotFullValidatedFillUp(uri, contentValues, transaction);
        }
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

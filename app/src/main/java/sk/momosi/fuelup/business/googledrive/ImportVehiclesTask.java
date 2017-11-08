package sk.momosi.fuelup.business.googledrive;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Set;

/**
 * @author Ondrej Oravcok
 * @version 17.10.2017
 */
public class ImportVehiclesTask extends AsyncTask<Void, Void, Integer> {

    private static final String LOG_TAG = ImportVehiclesTask.class.getSimpleName();

    private final Set<String> vehicleNames;
    private final JSONObject json;
    private Exception mLastError = null;

    private final Context context;
    private final WeakReference<Callback> callbackReference;

    public ImportVehiclesTask(@NonNull Set<String> vehicleNames, @NonNull JSONObject json, @NonNull Callback callback, @NonNull Context context) {
        super();
        this.vehicleNames = vehicleNames;
        this.json = json;
        this.context = context;
        this.callbackReference = new WeakReference<>(callback);
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        JSONObject vehicle = null;
        try {
            for (String vehicleName : vehicleNames) {
                vehicle = JsonUtil.getVehicle(json, vehicleName);
                long vehicleId = JsonUtil.importVehicle(vehicle, context);
                if (vehicleId != 0)
                    Log.i(LOG_TAG, "vehicle " + vehicleName + " imported with id=" + vehicleId);
                else
                    Log.e(LOG_TAG, "vehicle was not imported due to some SQLite error.");
            }
        } catch (JSONException e) {
            mLastError = new ImportVehicleJsonException(e, vehicle);
            cancel(true);
            return null;
        }

        return 1;
    }

    @Override
    protected void onPreExecute() {
        Callback callback = callbackReference.get();
        if (callback != null)
            callback.onImportVehiclesTaskPreExecute();
    }

    @Override
    protected void onPostExecute(Integer output) {
        Callback callback = callbackReference.get();
        if (callback != null)
            callback.onImportVehiclesTaskPostExecute(output);
    }

    @Override
    protected void onCancelled() {
        Callback callback = callbackReference.get();
        if (callback != null)
            callback.onImportVehiclesTaskCancel(mLastError);
    }

    public interface Callback {
        void onImportVehiclesTaskPreExecute();

        void onImportVehiclesTaskPostExecute(Integer output);

        void onImportVehiclesTaskCancel(Exception mLastError);
    }
}

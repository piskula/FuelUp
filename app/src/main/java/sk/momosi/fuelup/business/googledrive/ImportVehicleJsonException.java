package sk.momosi.fuelup.business.googledrive;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Ondrej Oravcok
 * @version 17.10.2017
 */
public class ImportVehicleJsonException extends JSONException {

    private JSONException e;
    private JSONObject json;

    public ImportVehicleJsonException(final String s, final JSONException e, final JSONObject json) {
        super(s);
        this.e = e;
        this.json = json;
    }

    public JSONException getException() {
        return this.e;
    }

    @Override
    public String toString() {
        return "JSON: " + json.toString() + "\nexception: " + e.toString();
    }
}

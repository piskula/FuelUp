package sk.piskula.fuelup.entity.util;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Properties;

/**
 * @author Ondrej Oravcok
 * @version 28.6.2017
 */

public class CurrencyUtil {

    private static final String TAG = "CurrencyUtil";

    private static final String PROPERTY_FILE = "currency.properties";
    private static final List<String> currenciesStrings = Arrays.asList("EUR", "CZK", "USD", "GBP", "PLN", "HUF", "BRL");

    private static Properties properties = new Properties();;

    public static String getCurrencySymbol(Currency currency, Context context) {
        if (properties.isEmpty()) {
            try {
                properties.load(context.getAssets().open(PROPERTY_FILE));
            } catch (IOException e) {
                Log.e(TAG, "Cannot load currencies from " + PROPERTY_FILE, e);
            }
        }

        if (properties.containsKey(currency.getCurrencyCode())) {
            return (String) properties.get(currency.getCurrencyCode());
        } else {
            return currency.getSymbol();
        }
    }

    public static List<Currency> getSupportedCurrencies() {
        List<Currency> currencies = new ArrayList<>();

        for (String currencyString : currenciesStrings)
            currencies.add(Currency.getInstance(currencyString));

        return currencies;
    }

}

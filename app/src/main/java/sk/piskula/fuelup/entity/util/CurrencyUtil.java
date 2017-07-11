package sk.piskula.fuelup.entity.util;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
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
    private static final List<String> currencyBefore = Arrays.asList("GBP");

    private static Properties properties = new Properties();;

    public static String getCurrencySymbol(Currency currency, Context context) {
        if (properties.isEmpty()) {
            try {
                properties.load(context.getAssets().open(PROPERTY_FILE));
            } catch (IOException e) {
                Log.e(TAG, "Cannot load currencies from " + PROPERTY_FILE, e);
            }
        }

        return getCurrencySymbol(currency);
    }

    private static String getCurrencySymbol(Currency currency) {
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

    private static String getPriceFormatted(double value, boolean isBefore, int fractionDigits, String symbol) {
        DecimalFormat bddf = new DecimalFormat();
        bddf.setMaximumFractionDigits(fractionDigits);
        bddf.setMinimumFractionDigits(fractionDigits);
        String price = bddf.format(value);

        if (isBefore) {
            return symbol + " " + price;
        } else {
            return price + " " + symbol;
        }
    }

    public static String getPrice(Currency currency, double value, Context context) {
        String symbol = getCurrencySymbol(currency, context);
        return getPriceFormatted(value, currencyBefore.contains(currency.getCurrencyCode()), 2, symbol);
    }

    public static String getPricePerLitre(Currency currency, double value, Context context) {
        String symbol = getCurrencySymbol(currency, context);
        return getPriceFormatted(value, currencyBefore.contains(currency.getCurrencyCode()), 3, symbol);
    }

    public static String getPrice(Currency currency, BigDecimal value, Context context) {
        return getPrice(currency, value.doubleValue(), context);
    }

    public static String getPricePerLitre(Currency currency, BigDecimal value, Context context) {
        return getPricePerLitre(currency, value.doubleValue(), context);
    }

}

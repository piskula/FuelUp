package sk.momosi.fuelup.entity.util;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Properties;

import sk.momosi.fuelup.screens.MainActivity;

/**
 * @author Ondrej Oravcok
 * @version 28.6.2017
 */

public class CurrencyUtil {

    private static final String TAG = "CurrencyUtil";

    private static final int CODE = 0;
    private static final int FRACTIONS_NORMAL = 1;
    private static final int FRACTIONS_PER_LITRE = 2;
    private static final int COEFFICIENT_PER_LITRE_MULTIPLY = 3;
    private static final int CODE_PER_LITRE = 4;

    private static final String PROPERTY_FILE = "currency.properties";
    private static final String DELIMETER = ",";
    private static final List<String> currencyBefore = Arrays.asList("GBP", "USD");    //TODO property file?

    private static Properties properties = new Properties();

    public static String getCurrencySymbol(Currency currency) {
        checkPropertiesAreLoaded();
        return getCurrencySymbolFromProperties(currency);
    }

    public static String getPerLitreSubcurrencySymbol(Currency currency) {
        if (!(getCoefficientPerLitreMultiply(currency).equals(BigDecimal.valueOf(1)))) {
            return properties.getProperty(currency.getCurrencyCode()).split(DELIMETER)[CODE_PER_LITRE];
        }
        return getCurrencySymbolFromProperties(currency);
    }

    public static int getPerLitreFractionDigits(Currency currency) {
        checkPropertiesAreLoaded();
        if (properties.containsKey(currency.getCurrencyCode()))
            return Integer.valueOf(properties.getProperty(currency.getCurrencyCode()).split(DELIMETER)[FRACTIONS_PER_LITRE]);
        return currency.getDefaultFractionDigits() + 1;
    }

    private static void checkPropertiesAreLoaded() {
        if (properties.isEmpty()) {
            try {
                properties.load(MainActivity.getInstance().getAssets().open(PROPERTY_FILE));
            } catch (IOException e) {
                Log.e(TAG, "Cannot load currencies from " + PROPERTY_FILE, e);
            }
        }
    }

    private static String getCurrencySymbolFromProperties(Currency currency) {
        if (properties.containsKey(currency.getCurrencyCode()))
            return properties.getProperty(currency.getCurrencyCode()).split(DELIMETER)[CODE];
        return currency.getSymbol();
    }

    public static List<Currency> getSupportedCurrencies() {
        checkPropertiesAreLoaded();

        List<Currency> currencies = new ArrayList<>();
        for (String currencyString : properties.stringPropertyNames())
            currencies.add(Currency.getInstance(currencyString));

        Collections.sort(currencies, new Comparator<Currency>() {
            @Override
            public int compare(Currency c1, Currency c2) {
                return c1.getCurrencyCode().compareTo(c2.getCurrencyCode());
            }
        });

        return currencies;
    }

    private static String getPriceFormatted(double value, int coefficient, boolean isBefore, int fractionDigits, String symbol) {
        DecimalFormat bddf = new DecimalFormat();
        bddf.setMaximumFractionDigits(fractionDigits);
        bddf.setMinimumFractionDigits(fractionDigits);
        String price = bddf.format(value * coefficient);

        if (isBefore) {
            return symbol + " " + price;
        } else {
            return price + " " + symbol;
        }
    }

    public static String getPrice(Currency currency, double value) {
        checkPropertiesAreLoaded();
        if (properties.containsKey(currency.getCurrencyCode())) {
            String[] currencyStrings = properties.getProperty(currency.getCurrencyCode()).split(DELIMETER);

            return getPriceFormatted(value,
                    1,
                    currencyBefore.contains(currency.getCurrencyCode()),
                    Integer.valueOf(currencyStrings[FRACTIONS_NORMAL]),
                    currencyStrings[CODE]);
        } else {
            return "not supported currency " + currency.getCurrencyCode();
        }
    }

    public static BigDecimal getCoefficientPerLitreMultiply(Currency currency) {
        checkPropertiesAreLoaded();
        if (properties.containsKey(currency.getCurrencyCode())) {
            String[] currencyStrings = properties.getProperty(currency.getCurrencyCode()).split(DELIMETER);
            return BigDecimal.valueOf(Integer.valueOf(currencyStrings[COEFFICIENT_PER_LITRE_MULTIPLY]));
        }
        return BigDecimal.ONE;
    }

    public static String getPricePerLitre(Currency currency, double value) {
        checkPropertiesAreLoaded();
        if (properties.containsKey(currency.getCurrencyCode())) {
            String[] currencyStrings = properties.getProperty(currency.getCurrencyCode()).split(DELIMETER);

            int coefficientMultiply = Integer.valueOf(currencyStrings[COEFFICIENT_PER_LITRE_MULTIPLY]);
            return getPriceFormatted(value,
                    coefficientMultiply,
                    currencyBefore.contains(currency.getCurrencyCode()),
                    Integer.valueOf(currencyStrings[FRACTIONS_PER_LITRE]),
                    currencyStrings[coefficientMultiply == 1 ? CODE : CODE_PER_LITRE]);
        } else {
            return "not supported currency " + currency.getCurrencyCode();
        }
    }

    public static String getPrice(Currency currency, BigDecimal value) {
        return getPrice(currency, value.doubleValue());
    }

    public static String getPricePerLitre(Currency currency, BigDecimal value) {
        return getPricePerLitre(currency, value.doubleValue());
    }

}

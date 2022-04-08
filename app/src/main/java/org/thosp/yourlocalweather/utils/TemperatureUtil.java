package org.thosp.yourlocalweather.utils;

import android.content.Context;
import android.preference.PreferenceManager;

import org.thosp.yourlocalweather.R;
import org.thosp.yourlocalweather.model.CurrentWeatherDbHelper;
import org.thosp.yourlocalweather.model.DetailedWeatherForecast;
import org.thosp.yourlocalweather.model.Weather;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Locale;

import static org.thosp.yourlocalweather.utils.LogToFile.appendLog;

public class TemperatureUtil {

    private static final String TAG = "TemperatureUtil";
    private static double SOLAR_CONSTANT = 1395; // solar constant (w/m2)
    private static double transmissionCoefficientClearDay = 0.81;
    private static double transmissionCoefficientCloudy = 0.62;

    public static float getApparentTemperature(double dryBulbTemperature,
                                               int humidity,
                                               double windSpeed,
                                               int cloudiness,
                                               double latitude,
                                               long timestamp) {
        return getApparentTemperatureWithSolarIrradiation(dryBulbTemperature, humidity, windSpeed, cloudiness, latitude, timestamp);
    }

    public static float getApparentTemperatureWithoutSolarIrradiation(double dryBulbTemperature, int humidity, double windSpeed) {
        double e = (humidity / 100f) * 6.105 * Math.exp((17.27*dryBulbTemperature) / (237.7 + dryBulbTemperature));
        double apparentTemperature = dryBulbTemperature + (0.33*e)-(0.70*windSpeed)-4.00;
        return (float)apparentTemperature;
    }

    public static float getApparentTemperatureWithSolarIrradiation(double dryBulbTemperature,
                                                                   int humidity,
                                                                   double windSpeed,
                                                                   int cloudiness,
                                                                   double latitude,
                                                                   long timestamp) {
        double e = (humidity / 100f) * 6.105 * Math.exp((17.27*dryBulbTemperature / (237.7 + dryBulbTemperature)));
        double cosOfZenithAngle = getCosOfZenithAngle(Math.toRadians(latitude), timestamp);
        double secOfZenithAngle = 1/cosOfZenithAngle;
        double transmissionCoefficient = transmissionCoefficientClearDay -
                (transmissionCoefficientClearDay - transmissionCoefficientCloudy) * (cloudiness/100f);
        double calculatedIrradiation = 0;
        if (cosOfZenithAngle > 0) {
            calculatedIrradiation = (SOLAR_CONSTANT * cosOfZenithAngle*Math.pow(transmissionCoefficient, secOfZenithAngle))/10;
        }
        double apparentTemperature = dryBulbTemperature + (0.348 * e) - (0.70 * windSpeed) + ((0.70 * calculatedIrradiation)/(windSpeed + 10)) - 4.25;
        return (float)apparentTemperature;
    }

    private static double getCosOfZenithAngle(double latitude, long timestamp) {
        Calendar measuredTime = Calendar.getInstance();
        measuredTime.setTimeInMillis(timestamp);
        double declination = Math.toRadians(-23.44 * Math.cos(Math.toRadians((360f/365f) * (9 + measuredTime.get(Calendar.DAY_OF_YEAR)))));
        double hourAngle = ((12 * 60) - (60 * measuredTime.get(Calendar.HOUR_OF_DAY) + measuredTime.get(Calendar.MINUTE))) * 0.25;
        return Math.sin(latitude)*Math.sin(declination) + (Math.cos(latitude) * Math.cos(declination) * Math.cos(Math.toRadians(hourAngle)));
    }

    public static float getCanadianStandardTemperature(double dryBulbTemperature, double windSpeed) {
        double windWithPow = Math.pow(windSpeed, 0.16);
        return (float) (13.12 + (0.6215 * dryBulbTemperature) - (13.37 * windWithPow) + (0.486 * dryBulbTemperature * windWithPow));
    }

    public static String getSecondTemperatureWithLabel(Context context, Weather weather, double latitude, long timestamp, Locale locale) {
        if (weather == null) {
            return null;
        }
        String temperatureTypeFromPreferences = PreferenceManager.getDefaultSharedPreferences(context).getString(
                Constants.KEY_PREF_TEMPERATURE_TYPE, "measured_only");
        if ("measured_only".equals(temperatureTypeFromPreferences) || "appearance_only".equals(temperatureTypeFromPreferences)) {
            return null;
        }
        int label = R.string.label_measured_temperature;
        if ("measured_appearance_primary_measured".equals(temperatureTypeFromPreferences)) {
            label = R.string.label_apparent_temperature;
        }
        return context.getString(label,
                getSecondTemperatureWithUnit(
                        context,
                        weather,
                        latitude,
                        timestamp,
                        locale));
    }

    public static String getSecondTemperatureWithUnit(Context context, Weather weather, double latitude, long timestamp, Locale locale) {
        if (weather == null) {
            return null;
        }
        String temperatureTypeFromPreferences = PreferenceManager.getDefaultSharedPreferences(context).getString(
                Constants.KEY_PREF_TEMPERATURE_TYPE, "measured_only");
        if ("measured_only".equals(temperatureTypeFromPreferences) || "appearance_only".equals(temperatureTypeFromPreferences)) {
            return null;
        }
        String apparentSign = "";
        double value = weather.getTemperature();
        if ("measured_appearance_primary_measured".equals(temperatureTypeFromPreferences)) {
            apparentSign = "~";
            value = TemperatureUtil.getApparentTemperature(
                    weather.getTemperature(),
                    weather.getHumidity(),
                    weather.getWindSpeed(),
                    weather.getClouds(),
                    latitude,
                    timestamp);
        }
        return apparentSign + String.format(locale, "%d",
                Math.round(getTemperatureInPreferredUnit(context, value))) + getTemperatureUnit(context);
    }

    public static String getMeasuredTemperatureWithUnit(Context context, double weatherTemperature, Locale locale) {
        return getMeasuredTemperatureWithUnit(context, weatherTemperature, "", locale);
    }

    public static String getMeasuredTemperatureWithUnit(Context context, double weatherTemperature, String apparentSign, Locale locale) {
        return apparentSign + String.format(locale, "%d",
                Math.round(getTemperatureInPreferredUnit(context, weatherTemperature))) + getTemperatureUnit(context);
    }

    public static String getTemperatureWithUnit(Context context, Weather weather, double latitude, long timestamp, Locale locale) {
        if (weather == null) {
            return null;
        }
        String temperatureTypeFromPreferences = PreferenceManager.getDefaultSharedPreferences(context).getString(
                Constants.KEY_PREF_TEMPERATURE_TYPE, "measured_only");
        String apparentSign = "";
        double value = weather.getTemperature();
        if ("appearance_only".equals(temperatureTypeFromPreferences) ||
                ("measured_appearance_primary_appearance".equals(temperatureTypeFromPreferences))) {
            apparentSign = "~";
            value = getApparentTemperature(
                    weather.getTemperature(),
                    weather.getHumidity(),
                    weather.getWindSpeed(),
                    weather.getClouds(),
                    latitude,
                    timestamp);
        }
        return getMeasuredTemperatureWithUnit(context, value, apparentSign, locale);
    }

    public static String getDewPointWithUnit(Context context, Weather weather, Locale locale) {
        if (weather == null) {
            return null;
        }
        double humidityLogarithm = Math.log(weather.getHumidity() / 100.0) / Math.log(Math.E);
        double dewPointPart = humidityLogarithm + ((17.67 * weather.getTemperature())/(243.5 + weather.getTemperature()));
        double dewPoint = (243.5 * dewPointPart) / (17.67 - dewPointPart);

        return String.format(locale, "%.1f",
                getTemperatureInPreferredUnit(context, dewPoint)) + getTemperatureUnit(context);
    }
    
    public static String getForecastedTemperatureWithUnit(Context context, DetailedWeatherForecast weather, Locale locale) {
        if (weather == null) {
            return null;
        }
        String apparentSign = "";
        double value = weather.getTemperature();
        if (value > 0) {
            apparentSign += "+";
        }
        return apparentSign + String.format(locale, "%.1f",
                getTemperatureInPreferredUnit(context, value)) + getTemperatureUnit(context);
    }

    public static String getForecastedApparentTemperatureWithUnit(
            Context context,
            double latitude,
            DetailedWeatherForecast weather,
            Locale locale) {

        if (weather == null) {
            return null;
        }
        String apparentSign = "";
        double value = getApparentTemperatureWithSolarIrradiation(
                    weather.getTemperature(),
                    weather.getHumidity(),
                    weather.getWindSpeed(),
                    weather.getCloudiness(),
                    latitude,
                    weather.getDateTime()
                );
        if (value > 0) {
            apparentSign += "+";
        }
        return apparentSign + String.format(locale, "%d",
                Math.round(getTemperatureInPreferredUnit(context, value))) + getTemperatureUnit(context);
    }

    public static String getTemperatureUnit(Context context) {
        String unitsFromPreferences = PreferenceManager.getDefaultSharedPreferences(context).getString(
                Constants.KEY_PREF_TEMPERATURE_UNITS, "celsius");
        if (unitsFromPreferences.contains("fahrenheit") ) {
            return context.getString(R.string.temperature_unit_fahrenheit);
        } else if (unitsFromPreferences.contains("kelvin")) {
            return context.getString(R.string.temperature_unit_kelvin);
        } else {
            return context.getString(R.string.temperature_unit_celsius);
        }
    }

    public static boolean isTemperatureUnitKelvin(Context context) {
        return "kelvin".equals(PreferenceManager.getDefaultSharedPreferences(context).getString(
                Constants.KEY_PREF_TEMPERATURE_UNITS, "celsius"));
    }

    public static double getTemperatureInPreferredUnit(Context context, double inputValue) {
        String unitsFromPreferences = PreferenceManager.getDefaultSharedPreferences(context).getString(
                Constants.KEY_PREF_TEMPERATURE_UNITS, "celsius");
        if (unitsFromPreferences.contains("fahrenheit")) {
            return (inputValue * 1.8d) + 32;
        } else if (unitsFromPreferences.contains("kelvin")) {
            return inputValue + 273.15;
        } else {
            return inputValue;
        }
    }

    public static double getTemperature(Context context, DetailedWeatherForecast weather) {
        if (weather == null) {
            return 0;
        }
        return getTemperatureInPreferredUnit(context, getTemperatureInCelsius(context, weather));
    }

    public static double getTemperature(Context context, Weather weather) {
        if (weather == null) {
            return 0;
        }
        return getTemperatureInPreferredUnit(context, getTemperatureInCelsius(context, weather));
    }

    public static double getTemperatureInCelsius(Context context, DetailedWeatherForecast weather) {
        if (weather == null) {
            return 0;
        }
        String temperatureTypeFromPreferences = PreferenceManager.getDefaultSharedPreferences(context).getString(
                Constants.KEY_PREF_TEMPERATURE_TYPE, "measured_only");
        double value = weather.getTemperature();
        if ("appearance_only".equals(temperatureTypeFromPreferences) ||
                ("measured_appearance_primary_appearance".equals(temperatureTypeFromPreferences))) {
            value = getApparentTemperatureWithoutSolarIrradiation(
                    weather.getTemperature(),
                    weather.getHumidity(),
                    weather.getWindSpeed());
        }
        return value;
    }

    public static double getTemperatureInCelsius(Context context, Weather weather) {
        if (weather == null) {
            return 0;
        }
        String temperatureTypeFromPreferences = PreferenceManager.getDefaultSharedPreferences(context).getString(
                Constants.KEY_PREF_TEMPERATURE_TYPE, "measured_only");
        double value = weather.getTemperature();
        if ("appearance_only".equals(temperatureTypeFromPreferences) ||
                ("measured_appearance_primary_appearance".equals(temperatureTypeFromPreferences))) {
            value = getApparentTemperatureWithoutSolarIrradiation(
                    weather.getTemperature(),
                    weather.getHumidity(),
                    weather.getWindSpeed());
        }
        return value;
    }

    public static int getTemperatureStatusIcon(Context context, CurrentWeatherDbHelper.WeatherRecord weatherRecord) {
        if ((weatherRecord == null) || (weatherRecord.getWeather() == null)) {
            return R.drawable.zero0;
        }
        float temperature = (float) getTemperature(context, weatherRecord.getWeather());
        return getResourceForNumber(context, temperature);
    }

    private static int getResourceForNumber(Context context, float number) {
        int roundedNumber = Math.round(number);
        if (roundedNumber == 0) {
            return R.drawable.zero0;
        } else if (roundedNumber < -60) {
            return R.drawable.less_minus60;
        } else if (roundedNumber > 120) {
            return R.drawable.more120;
        }
        try {
            String fileName;
            if (roundedNumber > 0){
                fileName = "plus" + roundedNumber;
            } else {
                fileName = "minus" + (-roundedNumber);
            }
            Field idField = R.drawable.class.getDeclaredField(fileName);
            return idField.getInt(idField);
        } catch (Exception e) {
            appendLog(context, TAG, "Error getting temperature icon", e);
            return R.drawable.small_icon;
        }
    }
}

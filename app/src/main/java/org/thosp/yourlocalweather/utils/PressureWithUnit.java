package org.thosp.yourlocalweather.utils;

import java.io.Serializable;
import java.util.Locale;

public class PressureWithUnit implements Serializable {
    private final double pressure;
    private final String pressureUnit;
    private final Locale pressureLocale;

    public PressureWithUnit(double pressure, String pressureUnit, Locale locale) {
        this.pressure = pressure;
        this.pressureUnit = pressureUnit;
        this.pressureLocale = locale;
    }

    public double getPressure() {
        return pressure;
    }

    public String getPressureUnit() {
        return pressureUnit;
    }

    public String getPressure(int decimalPlaces) {
        return String.format(pressureLocale,
                "%." + decimalPlaces + "f", pressure);
    }
}

package com.impact.realisticvitality.config;

/** How the ambient readout shows temperatures. Vitality works in °F inside. */
public enum TemperatureUnit {
    FAHRENHEIT("°F"),
    CELSIUS("°C");

    private static final double FREEZING_FAHRENHEIT = 32.0;
    private static final double FAHRENHEIT_PER_CELSIUS = 1.8;

    public final String symbol;

    TemperatureUnit(String symbol) {
        this.symbol = symbol;
    }

    public double fromFahrenheit(double fahrenheit) {
        return this == FAHRENHEIT ? fahrenheit : (fahrenheit - FREEZING_FAHRENHEIT) / FAHRENHEIT_PER_CELSIUS;
    }
}

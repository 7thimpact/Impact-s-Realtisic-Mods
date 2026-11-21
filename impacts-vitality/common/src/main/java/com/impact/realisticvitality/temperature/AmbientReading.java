package com.impact.realisticvitality.temperature;

/** One reading of the ambient temperature at a spot, in °F, with each factor's part, so the debug log can show the breakdown. */
public record AmbientReading(double biome, double dimension, double height, double time, double weather, double sun,
                             double underground, double blocks, double water) {
    public double total() {
        return biome + dimension + height + time + weather + sun + underground + blocks + water;
    }
}

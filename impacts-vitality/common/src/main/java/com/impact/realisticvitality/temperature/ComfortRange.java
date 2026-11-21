package com.impact.realisticvitality.temperature;

import com.impact.realisticvitality.config.VitalityServerConfig;

/** The ambient temperatures (°F) a body stays comfortable in: 50–80°F, with each point of insulation moving an edge 3°F. */
public record ComfortRange(double coldEdge, double hotEdge) {
    public static ComfortRange of(Insulation insulation) {
        VitalityServerConfig.TemperatureSettings settings = VitalityServerConfig.get().temperature;
        double degreesPerPoint = settings.degreesPerInsulation.get();
        return new ComfortRange(settings.comfortableFrom.get() - insulation.warmth() * degreesPerPoint,
                settings.comfortableTo.get() + insulation.cooling() * degreesPerPoint);
    }
}

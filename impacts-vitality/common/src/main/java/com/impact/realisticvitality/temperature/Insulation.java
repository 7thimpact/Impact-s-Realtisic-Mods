package com.impact.realisticvitality.temperature;

/** Points of warmth (lowering the cold edge of the comfortable range) and cooling (raising the hot edge). Negative points narrow it. */
public record Insulation(double warmth, double cooling) {
    public static final Insulation NONE = new Insulation(0.0, 0.0);

    public Insulation plus(Insulation other) {
        return new Insulation(warmth + other.warmth, cooling + other.cooling);
    }
}

package com.impact.realisticvitality.thirst;

/** Vitality's four water bottles. Each picks up its purity when it's filled; Cold Water is Purified Water chilled with ice. */
public enum BottledWater {
    UNCLEAN("unclean_water", WaterPurity.UNCLEAN, false, 0x6F8C5C),
    SALT("salt_water", WaterPurity.SALT, false, 0x5C93BF),
    PURIFIED("purified_water", WaterPurity.PURIFIED, false, 0x3F76E4),
    COLD("cold_water", WaterPurity.PURIFIED, true, 0xA6DCF7);

    public final String itemPath;
    public final WaterPurity purity;
    public final boolean isCold;
    /** The tint of the liquid in the bottle. */
    public final int liquidColor;

    BottledWater(String itemPath, WaterPurity purity, boolean isCold, int liquidColor) {
        this.itemPath = itemPath;
        this.purity = purity;
        this.isCold = isCold;
        this.liquidColor = liquidColor;
    }

    /** The bottle you get when you fill a glass bottle with water of this purity. */
    public static BottledWater filledWith(WaterPurity purity) {
        return switch (purity) {
            case PURIFIED -> PURIFIED;
            case UNCLEAN -> UNCLEAN;
            case SALT -> SALT;
        };
    }
}

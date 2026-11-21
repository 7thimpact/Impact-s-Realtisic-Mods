package com.impact.realisticvitality.air;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Each dimension's air, from data files {@code data/<namespace>/air_quality/<name>.json}:
 * {@code {"dimension": "minecraft:the_nether", "quality": "smoky"}}. Vitality ships the Nether (smoky) and the End (thin);
 * datapacks can give any dimension a quality. Reloaded with {@code /reload}.
 */
public final class DimensionAirQualities extends SimpleJsonResourceReloadListener {
    private static final ImpactsLog LOG = ImpactsLog.of(VitalityMod.MOD_ID);
    private static final Gson GSON = new GsonBuilder().create();
    private static final String FOLDER = "air_quality";
    private static final String DIMENSION_FIELD = "dimension";
    private static final String QUALITY_FIELD = "quality";
    // Built after the fields above, which its constructor uses.
    public static final DimensionAirQualities INSTANCE = new DimensionAirQualities();

    private final Map<ResourceLocation, AirQuality> qualityByDimension = new ConcurrentHashMap<>();

    private DimensionAirQualities() {
        super(GSON, FOLDER);
    }

    public Optional<AirQuality> of(ResourceLocation dimension) {
        return Optional.ofNullable(qualityByDimension.get(dimension));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resources, ProfilerFiller profiler) {
        qualityByDimension.clear();
        files.forEach(this::read);
        LOG.debug(VitalityLogCategories.AIR, "dimension air loaded", "dimensions", qualityByDimension.size());
    }

    private void read(ResourceLocation fileId, JsonElement file) {
        JsonObject json = GsonHelper.convertToJsonObject(file, fileId.toString());
        Optional<ResourceLocation> dimension = ResourceIds.tryParse(GsonHelper.getAsString(json, DIMENSION_FIELD, ""));
        Optional<AirQuality> quality = AirQuality.bySerializedName(GsonHelper.getAsString(json, QUALITY_FIELD, ""));
        if (dimension.isEmpty() || quality.isEmpty()) {
            LOG.warn(VitalityLogCategories.AIR, "air quality file skipped", "file", fileId, "reason", "needs a dimension and a quality");
            return;
        }
        qualityByDimension.put(dimension.get(), quality.get());
    }
}

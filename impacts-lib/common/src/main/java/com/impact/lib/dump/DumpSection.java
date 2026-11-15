package com.impact.lib.dump;

import java.util.Map;

/** A filled-in dump section: its name and its values in display order. */
public record DumpSection(String name, Map<String, Object> entries) {
}

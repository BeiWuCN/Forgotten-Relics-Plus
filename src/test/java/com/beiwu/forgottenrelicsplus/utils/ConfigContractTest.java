package com.beiwu.forgottenrelicsplus.utils;

import static org.junit.jupiter.api.Assertions.*;

import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.junit.jupiter.api.Test;

class ConfigContractTest {
    @Test
    void configurationPathsDefaultsRangesAndRestartRequirementsRemainCompatible() throws Exception {
        JsonObject actual = new JsonObject();
        Gson gson = new Gson();
        for (var field : FRConfig.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !(field.get(null) instanceof ModConfigSpec.ConfigValue<?> value)) continue;
            JsonObject entry = new JsonObject();
            entry.add("path", gson.toJsonTree(value.getPath()));
            entry.add("default", gson.toJsonTree(value.getDefault()));
            entry.addProperty("range", String.valueOf(value.getSpec().getRange()));
            entry.addProperty("restart", value.getSpec().restartType().name());
            entry.addProperty("comment", value.getSpec().getComment());
            actual.add(field.getName(), entry);
        }
        try (var stream = ConfigContractTest.class.getResourceAsStream("/config-contract.json")) {
            assertNotNull(stream);
            assertEquals(JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)), actual);
        }
        assertEquals(211, actual.size());
    }
}

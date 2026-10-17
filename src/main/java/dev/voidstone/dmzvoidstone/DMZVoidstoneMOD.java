
package dev.voidstone.dmzvoidstone;

import com.dragonminez.common.config.ConfigManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Mod(DMZVoidstoneMOD.MOD_ID)
public class DMZVoidstoneMOD {

    public static final String MOD_ID = "dmz_voidstone";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String RACE_NAME = "tsukisora";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String[] CONFIG_FILES = {
            "character.json",
            "stats.json",
            "forms/voidforms.json"
    };

    public DMZVoidstoneMOD() {
        installDefaultRaceConfig();

        /*
         * DragonMineZ loads its configs in its own mod constructor, which runs
         * before this dependent mod. Reload after installing/merging Tsukisora
         * files so the class is available on the same launch.
         */
        LOGGER.info("Reloading DragonMineZ configs after installing Tsukisora defaults.");
        ConfigManager.reload();
    }

    private void installDefaultRaceConfig() {
        Path raceDirectory = FMLPaths.CONFIGDIR.get()
                .resolve("dragonminez")
                .resolve("races")
                .resolve(RACE_NAME);

        try {
            Files.createDirectories(raceDirectory);

            for (String fileName : CONFIG_FILES) {
                Path target = raceDirectory.resolve(fileName);
                String resourcePath = "/dmz-voidstone/races/" + RACE_NAME + "/" + fileName;

                if (Files.exists(target)) {
                    if ("stats.json".equals(fileName)) {
                        mergeMissingStatsClasses(target, resourcePath);
                    } else {
                        LOGGER.debug("Keeping existing Tsukisora config: {}", target);
                    }
                    continue;
                }

                Files.createDirectories(target.getParent());
                try (InputStream input = DMZVoidstoneMOD.class.getResourceAsStream(resourcePath)) {
                    if (input == null) {
                        LOGGER.error("Missing bundled Tsukisora resource: {}", resourcePath);
                        continue;
                    }

                    Files.copy(input, target);
                    LOGGER.info("Installed default Tsukisora config: {}", target);
                } catch (IOException exception) {
                    LOGGER.error("Failed to install Tsukisora config: {}", target, exception);
                }
            }
        } catch (IOException exception) {
            LOGGER.error("Failed to create Tsukisora configuration directory.", exception);
        }
    }

    /**
     * Adds bundled class definitions that are absent from an existing stats.json.
     * Existing class settings and all unrelated config values are preserved.
     */
    private void mergeMissingStatsClasses(Path target, String resourcePath) {
        try (InputStream bundled = DMZVoidstoneMOD.class.getResourceAsStream(resourcePath)) {
            if (bundled == null) {
                LOGGER.error("Missing bundled Tsukisora resource: {}", resourcePath);
                return;
            }

            JsonObject existing;
            try (var reader = Files.newBufferedReader(target, StandardCharsets.UTF_8)) {
                JsonElement parsed = JsonParser.parseReader(reader);
                if (parsed == null || !parsed.isJsonObject()) {
                    LOGGER.error("Cannot merge Void class: Tsukisora stats file is not a JSON object: {}", target);
                    return;
                }
                existing = parsed.getAsJsonObject();
            }

            JsonElement bundledParsed = JsonParser.parseReader(
                    new InputStreamReader(bundled, StandardCharsets.UTF_8));
            if (bundledParsed == null || !bundledParsed.isJsonObject()) {
                LOGGER.error("Bundled Tsukisora stats file is not a JSON object: {}", resourcePath);
                return;
            }

            JsonObject bundledRoot = bundledParsed.getAsJsonObject();
            if (!bundledRoot.has("classes") || !bundledRoot.get("classes").isJsonObject()) {
                LOGGER.error("Bundled Tsukisora stats file has no classes object: {}", resourcePath);
                return;
            }

            JsonObject existingClasses;
            if (existing.has("classes") && existing.get("classes").isJsonObject()) {
                existingClasses = existing.getAsJsonObject("classes");
            } else {
                existingClasses = new JsonObject();
                existing.add("classes", existingClasses);
            }

            JsonObject bundledClasses = bundledRoot.getAsJsonObject("classes");
            boolean changed = false;
            for (var entry : bundledClasses.entrySet()) {
                boolean alreadyPresent = existingClasses.keySet().stream()
                        .anyMatch(key -> key.equalsIgnoreCase(entry.getKey()));
                if (!alreadyPresent) {
                    existingClasses.add(entry.getKey(), entry.getValue().deepCopy());
                    LOGGER.info("Added missing Tsukisora class '{}' to {}", entry.getKey(), target);
                    changed = true;
                }
            }

            if (changed) {
                Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
                Files.writeString(temporary, GSON.toJson(existing) + System.lineSeparator(), StandardCharsets.UTF_8);
                try {
                    Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException atomicMoveUnsupported) {
                    Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                LOGGER.debug("Tsukisora stats already contains all bundled classes: {}", target);
            }
        } catch (Exception exception) {
            LOGGER.error("Failed to merge bundled Tsukisora classes into {}", target, exception);
        }
    }
}

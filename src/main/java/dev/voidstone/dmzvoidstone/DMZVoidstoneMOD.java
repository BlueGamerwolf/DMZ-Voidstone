
package dev.voidstone.dmzvoidstone;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import org.slf4j.Logger;

import com.dragonminez.common.config.ConfigManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(DMZVoidstoneMOD.MOD_ID)
public class DMZVoidstoneMOD {

    public static final String MOD_ID = "dmz_voidstone";

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final String RESOURCE_ROOT = "dmz-voidstone";

    private static final String[] RESOURCE_FOLDERS = {
            "classes",
            "races"
    };

    public DMZVoidstoneMOD() {
        installBundledConfigs();

        LOGGER.info(
                "Reloading DragonMineZ configs after installing Voidstone configs."
        );

        ConfigManager.reload();
    }

    /**
     * Automatically discovers JSON files under:
     *
     * resources/dmz-voidstone/classes/
     * resources/dmz-voidstone/races/
     *
     * Supports development resource directories and packaged JAR resources.
     */
    private void installBundledConfigs() {
        List<String> resources;

        try {
            resources = discoverJsonResources();

            if (resources.isEmpty()) {
                LOGGER.warn(
                        "No JSON resources found under {}/classes or {}/races.",
                        RESOURCE_ROOT,
                        RESOURCE_ROOT
                );
                return;
            }

            int installed = 0;
            int preserved = 0;
            int merged = 0;

            for (String resourcePath : resources) {
                String relativePath = resourcePath.substring(
                        RESOURCE_ROOT.length() + 1
                );

                Path target = FMLPaths.CONFIGDIR.get()
                        .resolve("dragonminez")
                        .resolve(relativePath);

                Files.createDirectories(target.getParent());

                if (!Files.exists(target)) {
                    if (installResource(resourcePath, target)) {
                        installed++;
                    }
                    continue;
                }

                if (isRaceStatsFile(relativePath)) {
                    if (mergeMissingStatsClasses(target, resourcePath)) {
                        merged++;
                    } else {
                        preserved++;
                    }
                } else {
                    LOGGER.debug(
                            "Keeping existing config to preserve customizations: {}",
                            target
                    );
                    preserved++;
                }
            }

            LOGGER.info(
                    "Voidstone config installation finished. " +
                            "Discovered: {}, installed: {}, merged: {}, preserved: {}.",
                    resources.size(),
                    installed,
                    merged,
                    preserved
            );

        } catch (Exception exception) {
            LOGGER.error(
                    "Failed to install bundled Voidstone configs.",
                    exception
            );
        }
    }

    /**
     * Finds all supported JSON resources recursively.
     *
     * Scans the resource directory in development environments. For packaged
     * JARs, scans the JAR containing the resource root.
     */
    private List<String> discoverJsonResources() throws Exception {
        List<String> results = new ArrayList<>();

        ClassLoader classLoader = DMZVoidstoneMOD.class.getClassLoader();

        URL rootUrl = classLoader.getResource(RESOURCE_ROOT);

        if (rootUrl != null && "file".equalsIgnoreCase(rootUrl.getProtocol())) {
            Path root = Path.of(rootUrl.toURI());

            try (Stream<Path> paths = Files.walk(root)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName()
                                .toString()
                                .toLowerCase()
                                .endsWith(".json"))
                        .forEach(path -> {
                            Path relative = root.relativize(path);

                            String resourcePath = RESOURCE_ROOT + "/"
                                    + relative.toString().replace('\\', '/');

                            if (isSupportedResource(resourcePath)) {
                                results.add(resourcePath);
                            }
                        });
            }

        } else if (rootUrl != null
                && "jar".equalsIgnoreCase(rootUrl.getProtocol())) {

            JarURLConnection connection =
                    (JarURLConnection) rootUrl.openConnection();

            try (JarFile jar = connection.getJarFile()) {
                Enumeration<JarEntry> entries = jar.entries();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();

                    if (entry.isDirectory()) {
                        continue;
                    }

                    String name = entry.getName();

                    if (name.toLowerCase().endsWith(".json")
                            && isSupportedResource(name)) {
                        results.add(name);
                    }
                }
            }

        } else {
            /*
             * Some mod classloaders do not expose the resource root directory.
             * Try locating a known resource beneath the root to identify the
             * development directory or JAR.
             */
            URL classesUrl = classLoader.getResource(
                    RESOURCE_ROOT + "/classes"
            );

            if (classesUrl != null
                    && "file".equalsIgnoreCase(classesUrl.getProtocol())) {

                Path root = Path.of(classesUrl.toURI());

                try (Stream<Path> paths = Files.walk(root)) {
                    paths.filter(Files::isRegularFile)
                            .filter(path -> path.getFileName()
                                    .toString()
                                    .toLowerCase()
                                    .endsWith(".json"))
                            .forEach(path -> {
                                Path relative = root.relativize(path);

                                String resourcePath = RESOURCE_ROOT
                                        + "/classes/"
                                        + relative.toString().replace('\\', '/');

                                if (isSupportedResource(resourcePath)) {
                                    results.add(resourcePath);
                                }
                            });
                }

            } else {
                LOGGER.warn(
                        "Could not resolve the '{}' resource directory. " +
                                "The mod classloader may not expose directory entries.",
                        RESOURCE_ROOT
                );
            }
        }

        results.sort(String.CASE_INSENSITIVE_ORDER);
        return results;
    }

    /**
     * Accepts JSON resources inside classes/ or races/.
     */
    private boolean isSupportedResource(String resourcePath) {
        String prefix = RESOURCE_ROOT + "/";

        if (!resourcePath.startsWith(prefix)) {
            return false;
        }

        String relative = resourcePath.substring(prefix.length());

        for (String folder : RESOURCE_FOLDERS) {
            if (relative.startsWith(folder + "/")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Identifies race stats files such as races/tsukisora/stats.json.
     */
    private boolean isRaceStatsFile(String relativePath) {
        String normalized = relativePath.replace('\\', '/');
        String[] parts = normalized.split("/");

        return parts.length == 3
                && "races".equals(parts[0])
                && "stats.json".equals(parts[2]);
    }

    /**
     * Copies a bundled resource to the matching DragonMineZ config path.
     * Never overwrites an existing file.
     */
    private boolean installResource(String resourcePath, Path target) {
        try (InputStream input =
                     DMZVoidstoneMOD.class.getClassLoader()
                             .getResourceAsStream(resourcePath)) {

            if (input == null) {
                LOGGER.error(
                        "Bundled resource not found: {}",
                        resourcePath
                );
                return false;
            }

            Files.copy(input, target);

            LOGGER.info(
                    "Installed bundled config: {} -> {}",
                    resourcePath,
                    target
            );

            return true;

        } catch (IOException exception) {
            LOGGER.error(
                    "Failed to install bundled config: {}",
                    target,
                    exception
            );
            return false;
        }
    }

    /**
     * Merges missing class definitions from a bundled race stats.json into
     * an existing one without overwriting existing class settings.
     *
     * Returns true when the existing file was updated.
     */
    private boolean mergeMissingStatsClasses(
            Path target,
            String resourcePath
    ) {
        try (InputStream bundled =
                     DMZVoidstoneMOD.class.getClassLoader()
                             .getResourceAsStream(resourcePath)) {

            if (bundled == null) {
                LOGGER.error(
                        "Missing bundled race stats resource: {}",
                        resourcePath
                );
                return false;
            }

            JsonObject existing;

            try (var reader = Files.newBufferedReader(
                    target,
                    StandardCharsets.UTF_8
            )) {
                JsonElement parsed = JsonParser.parseReader(reader);

                if (parsed == null || !parsed.isJsonObject()) {
                    LOGGER.error(
                            "Race stats file is not a JSON object: {}",
                            target
                    );
                    return false;
                }

                existing = parsed.getAsJsonObject();
            }

            JsonElement bundledParsed = JsonParser.parseReader(
                    new InputStreamReader(bundled, StandardCharsets.UTF_8)
            );

            if (bundledParsed == null || !bundledParsed.isJsonObject()) {
                LOGGER.error(
                        "Bundled race stats is not a JSON object: {}",
                        resourcePath
                );
                return false;
            }

            JsonObject bundledRoot = bundledParsed.getAsJsonObject();

            if (!bundledRoot.has("classes")
                    || !bundledRoot.get("classes").isJsonObject()) {
                LOGGER.debug(
                        "No bundled classes object to merge in {}",
                        resourcePath
                );
                return false;
            }

            JsonObject existingClasses;

            if (existing.has("classes")
                    && existing.get("classes").isJsonObject()) {
                existingClasses = existing.getAsJsonObject("classes");
            } else {
                existingClasses = new JsonObject();
                existing.add("classes", existingClasses);
            }

            JsonObject bundledClasses =
                    bundledRoot.getAsJsonObject("classes");

            boolean changed = false;

            for (var entry : bundledClasses.entrySet()) {
                boolean alreadyPresent = existingClasses.keySet()
                        .stream()
                        .anyMatch(key ->
                                key.equalsIgnoreCase(entry.getKey())
                        );

                if (!alreadyPresent) {
                    existingClasses.add(
                            entry.getKey(),
                            entry.getValue().deepCopy()
                    );

                    LOGGER.info(
                            "Added missing class '{}' to race stats {}",
                            entry.getKey(),
                            target
                    );

                    changed = true;
                }
            }

            if (!changed) {
                return false;
            }

            Path temporary = target.resolveSibling(
                    target.getFileName() + ".tmp"
            );

            Files.writeString(
                    temporary,
                    GSON.toJson(existing) + System.lineSeparator(),
                    StandardCharsets.UTF_8
            );

            try {
                Files.move(
                        temporary,
                        target,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (IOException atomicMoveUnsupported) {
                Files.move(
                        temporary,
                        target,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            LOGGER.info("Merged bundled race stats into {}", target);
            return true;

        } catch (Exception exception) {
            LOGGER.error(
                    "Failed to merge race stats into {}",
                    target,
                    exception
            );
            return false;
        }
    }
}

package dev.voidstone.dmzvoidstone;

import com.dragonminez.common.config.ConfigManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

@Mod(DMZVoidstoneMOD.MOD_ID)
public class DMZVoidstoneMOD {

    public static final String MOD_ID = "dmz_voidstone";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String RESOURCE_ROOT = "dmz-voidstone";
    private static final String[] RESOURCE_FOLDERS = {"classes", "races"};

    public DMZVoidstoneMOD() {
        // Wait until Forge has completed mod loading before installing and reloading
        // DragonMineZ's configuration. This avoids doing config work in the constructor.
        FMLJavaModLoadingContext.get().getModEventBus()
                .addListener(this::onLoadComplete);
    }

    private void onLoadComplete(FMLLoadCompleteEvent event) {
        installBundledConfigs();
        LOGGER.info("Reloading DragonMineZ configs after installing Voidstone configs.");
        ConfigManager.reload();
    }

    /**
     * Discovers bundled JSON files under resources/dmz-voidstone/classes and
     * resources/dmz-voidstone/races. Existing files are preserved except race
     * stats files, where missing class entries are merged without overwriting
     * existing class values.
     */
    private void installBundledConfigs() {
        try {
            List<String> resources = discoverJsonResources();

            if (resources.isEmpty()) {
                LOGGER.warn("No JSON resources found under {}/classes or {}/races.", RESOURCE_ROOT, RESOURCE_ROOT);
                return;
            }

            int installed = 0;
            int preserved = 0;
            int merged = 0;

            for (String resourcePath : resources) {
                String relativePath = resourcePath.substring(RESOURCE_ROOT.length() + 1);
                Path target = FMLPaths.CONFIGDIR.get().resolve("dragonminez").resolve(relativePath);
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
                    LOGGER.debug("Keeping existing config to preserve customizations: {}", target);
                    preserved++;
                }
            }

            LOGGER.info(
                    "Voidstone config installation finished. Discovered: {}, installed: {}, merged: {}, preserved: {}.",
                    resources.size(), installed, merged, preserved
            );
        } catch (Exception exception) {
            LOGGER.error("Failed to install bundled Voidstone configs.", exception);
        }
    }

    /**
     * Scans each resource folder independently. This does not depend on the
     * parent "dmz-voidstone" directory being represented as a JAR entry.
     */
    private List<String> discoverJsonResources() throws Exception {
        Set<String> results = new LinkedHashSet<>();
        ClassLoader classLoader = DMZVoidstoneMOD.class.getClassLoader();

        // First try directory URLs. This works in common development environments.
        for (String folder : RESOURCE_FOLDERS) {
            String folderResource = RESOURCE_ROOT + "/" + folder;
            Enumeration<URL> folderUrls = classLoader.getResources(folderResource);
            while (folderUrls.hasMoreElements()) {
                URL folderUrl = folderUrls.nextElement();

                if ("file".equalsIgnoreCase(folderUrl.getProtocol())) {
                    Path folderPath = Path.of(folderUrl.toURI());
                    addDirectoryResources(folderPath, folderResource, results);
                } else if ("jar".equalsIgnoreCase(folderUrl.getProtocol())) {
                    JarURLConnection connection = (JarURLConnection) folderUrl.openConnection();
                    connection.setUseCaches(false);
                    String entryPrefix = connection.getEntryName();
                    if (entryPrefix == null || entryPrefix.isBlank()) {
                        entryPrefix = folderResource;
                    }
                    if (!entryPrefix.endsWith("/")) {
                        entryPrefix += "/";
                    }

                    try (JarFile jar = connection.getJarFile()) {
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            if (entry.isDirectory()) {
                                continue;
                            }
                            String name = entry.getName();
                            if (name.startsWith(entryPrefix)
                                    && name.toLowerCase(java.util.Locale.ROOT).endsWith(".json")
                                    && isSupportedResource(name)) {
                                results.add(name);
                            }
                        }
                    }
                } else {
                    LOGGER.debug("Unsupported resource protocol '{}' for {}", folderUrl.getProtocol(), folderUrl);
                }
            }
        }

        // Some packaged JARs omit directory entries, so getResources(folder)
        // can return nothing even though JSON files exist. Scan the code-source
        // JAR directly in that case.
        if (results.isEmpty()) {
            URL codeSource = DMZVoidstoneMOD.class.getProtectionDomain()
                    .getCodeSource() == null
                    ? null
                    : DMZVoidstoneMOD.class.getProtectionDomain().getCodeSource().getLocation();

            if (codeSource != null && "file".equalsIgnoreCase(codeSource.getProtocol())) {
                Path sourcePath = Path.of(codeSource.toURI());

                if (Files.isRegularFile(sourcePath)
                        && sourcePath.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".jar")) {
                    try (JarFile jar = new JarFile(sourcePath.toFile())) {
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            if (entry.isDirectory()) {
                                continue;
                            }
                            String name = entry.getName();
                            if (name.toLowerCase(java.util.Locale.ROOT).endsWith(".json")
                                    && isSupportedResource(name)) {
                                results.add(name);
                            }
                        }
                    }
                } else if (Files.isDirectory(sourcePath)) {
                    // Gradle development class/resource outputs are commonly siblings.
                    addDirectoryResources(sourcePath.resolve(RESOURCE_ROOT), RESOURCE_ROOT, results);
                    Path siblingResources = sourcePath.resolve("../../../resources/main")
                            .normalize().resolve(RESOURCE_ROOT);
                    addDirectoryResources(siblingResources, RESOURCE_ROOT, results);
                }
            }
        }

        List<String> sorted = new ArrayList<>(results);
        sorted.sort(String.CASE_INSENSITIVE_ORDER);
        return sorted;
    }

    private void addDirectoryResources(Path root, String resourcePrefix, Set<String> results)
            throws IOException {
        if (!Files.isDirectory(root)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString()
                            .toLowerCase(java.util.Locale.ROOT).endsWith(".json"))
                    .forEach(path -> {
                        String resourcePath = resourcePrefix + "/"
                                + root.relativize(path).toString().replace('\\\\', '/');
                        if (isSupportedResource(resourcePath)) {
                            results.add(resourcePath);
                        }
                    });
        }
    }

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

    private boolean isRaceStatsFile(String relativePath) {
        String[] parts = relativePath.replace('\\', '/').split("/");
        return parts.length == 3
                && "races".equals(parts[0])
                && "stats.json".equals(parts[2]);
    }

    private boolean installResource(String resourcePath, Path target) {
        try (InputStream input = DMZVoidstoneMOD.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                LOGGER.error("Bundled resource not found: {}", resourcePath);
                return false;
            }

            Files.copy(input, target);
            LOGGER.info("Installed bundled config: {} -> {}", resourcePath, target);
            return true;
        } catch (IOException exception) {
            LOGGER.error("Failed to install bundled config: {}", target, exception);
            return false;
        }
    }

    /**
     * Merges only missing class entries from a bundled race stats.json.
     * Existing class definitions and their values are never overwritten.
     */
    private boolean mergeMissingStatsClasses(Path target, String resourcePath) {
        try (InputStream bundled = DMZVoidstoneMOD.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (bundled == null) {
                LOGGER.error("Missing bundled race stats resource: {}", resourcePath);
                return false;
            }

            JsonObject existing;
            try (var reader = Files.newBufferedReader(target, StandardCharsets.UTF_8)) {
                JsonElement parsed = JsonParser.parseReader(reader);
                if (parsed == null || !parsed.isJsonObject()) {
                    LOGGER.error("Race stats file is not a JSON object: {}", target);
                    return false;
                }
                existing = parsed.getAsJsonObject();
            }

            JsonElement bundledParsed = JsonParser.parseReader(
                    new InputStreamReader(bundled, StandardCharsets.UTF_8)
            );
            if (bundledParsed == null || !bundledParsed.isJsonObject()) {
                LOGGER.error("Bundled race stats is not a JSON object: {}", resourcePath);
                return false;
            }

            JsonObject bundledRoot = bundledParsed.getAsJsonObject();
            if (!bundledRoot.has("classes") || !bundledRoot.get("classes").isJsonObject()) {
                LOGGER.debug("No bundled classes object to merge in {}", resourcePath);
                return false;
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
                    LOGGER.info("Added missing class '{}' to race stats {}", entry.getKey(), target);
                    changed = true;
                }
            }

            if (!changed) {
                return false;
            }

            Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
            Files.writeString(
                    temporary,
                    GSON.toJson(existing) + System.lineSeparator(),
                    StandardCharsets.UTF_8
            );

            try {
                Files.move(
                        temporary, target,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (IOException atomicMoveUnsupported) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }

            LOGGER.info("Merged bundled race stats into {}", target);
            return true;
        } catch (Exception exception) {
            LOGGER.error("Failed to merge race stats into {}", target, exception);
            return false;
        }
    }
}

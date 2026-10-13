
package dev.voidstone.dmzvoidstone;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod(DMZVoidstoneMOD.MOD_ID)
public class DMZVoidstoneMOD {

    public static final String MOD_ID = "dmz_voidstone";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String RACE_NAME = "tsukisora";

    /*
     * Files bundled with the mod and installed into DragonMineZ's
     * configuration directory.
     *
     * Add new bundled configuration files to this list.
     */
    private static final String[] CONFIG_FILES = {
            "character.json",
            "stats.json",
            "forms/voidforms.json"
    };

    public DMZVoidstoneMOD() {
        installDefaultRaceConfig();
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

                // Preserve existing configurations.
                if (Files.exists(target)) {
                    LOGGER.debug(
                            "Keeping existing Tsukisora config: {}",
                            target);
                    continue;
                }

                // Create parent folders, including the forms directory.
                Files.createDirectories(target.getParent());

                String resourcePath = "/dmz-voidstone/races/"
                        + RACE_NAME + "/" + fileName;

                try (InputStream input = DMZVoidstoneMOD.class.getResourceAsStream(
                        resourcePath)) {

                    if (input == null) {
                        LOGGER.error(
                                "Missing bundled Tsukisora resource: {}",
                                resourcePath);
                        continue;
                    }

                    Files.copy(input, target);

                    LOGGER.info(
                            "Installed default Tsukisora config: {}",
                            target);
                } catch (IOException exception) {
                    LOGGER.error(
                            "Failed to install Tsukisora config: {}",
                            target,
                            exception);
                }
            }
        } catch (IOException exception) {
            LOGGER.error(
                    "Failed to create Tsukisora configuration directory.",
                    exception);
        }
    }
}
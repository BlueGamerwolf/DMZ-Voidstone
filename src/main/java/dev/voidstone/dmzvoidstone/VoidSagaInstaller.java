package dev.voidstone.dmzvoidstone;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Installs bundled Voidstone story content into the active save.
 * Existing files are preserved so server owners can customize their sagas.
 */
@Mod.EventBusSubscriber(modid = DMZVoidstoneMOD.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class VoidSagaInstaller {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<String> STORY_FILES = List.of(
            "sagas/void_training.json",
            "sagas/voidstone_ascension.json",
            "quests/saga_void_training/01_stillness_in_the_void.json",
            "quests/saga_void_training/02_pressure_without_power.json",
            "quests/saga_void_training/03_the_unbroken_will.json",
            "quests/saga_void_training/04_edge_of_the_horizon.json",
            "quests/saga_void_training/05_the_void_master.json",
            "quests/saga_voidstone_ascension/01_trial_of_the_abyss.json",
            "quests/saga_voidstone_ascension/02_trial_ascendant.json",
            "quests/saga_voidstone_ascension/03_trial_event_horizon.json",
            "quests/saga_voidstone_ascension/04_trial_eternal_void.json",
            "quests/saga_voidstone_ascension/05_trial_voidstone_abyss.json"
    );

    private VoidSagaInstaller() {}

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        MinecraftServer server = event.getServer();
        Path dmzDirectory = server.getWorldPath(LevelResource.ROOT).resolve("dragonminez");
        for (String relativeName : STORY_FILES) {
            Path target = dmzDirectory.resolve(relativeName);
            if (Files.exists(target)) {
                LOGGER.debug("Keeping existing Voidstone story file: {}", target);
                continue;
            }
            String resource = "/dmz-voidstone/story/" + relativeName.replace('\\', '/');
            try {
                Files.createDirectories(target.getParent());
                try (InputStream input = VoidSagaInstaller.class.getResourceAsStream(resource)) {
                    if (input == null) {
                        LOGGER.error("Missing bundled Voidstone story resource: {}", resource);
                        continue;
                    }
                    Files.copy(input, target);
                    LOGGER.info("Installed Voidstone story file: {}", target);
                }
            } catch (IOException exception) {
                LOGGER.error("Failed to install Voidstone story file: {}", target, exception);
            }
        }
    }
}

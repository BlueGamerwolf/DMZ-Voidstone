package dev.voidstone.dmzvoidstone.backdoors.tsukisora;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Persists the highest manifestation reached by each player in the overworld's
 * SavedData. Progress is server-owned and survives logout and server restarts.
 */
public final class TsukisoraProgressionData extends SavedData {
    private static final String DATA_NAME = "dmz_voidstone_tsukisora_progression";
    private static final String PLAYERS_KEY = "Players";
    private final Map<UUID, Integer> stages = new HashMap<>();

    public TsukisoraProgressionData() {
    }

    private TsukisoraProgressionData(CompoundTag tag) {
        CompoundTag players = tag.getCompound(PLAYERS_KEY);
        for (String key : players.getAllKeys()) {
            try {
                UUID playerId = UUID.fromString(key);
                int stage = players.getInt(key);
                stages.put(playerId, clampStage(stage));
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed UUID entries rather than preventing world load.
            }
        }
    }

    public static TsukisoraProgressionData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(
                TsukisoraProgressionData::load,
                TsukisoraProgressionData::new,
                DATA_NAME);
    }

    public static TsukisoraProgressionData load(CompoundTag tag) {
        return new TsukisoraProgressionData(tag);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag players = new CompoundTag();
        stages.forEach((uuid, stage) -> players.putInt(uuid.toString(), clampStage(stage)));
        tag.put(PLAYERS_KEY, players);
        return tag;
    }

    public TsukisoraForm getForm(UUID playerId) {
        return TsukisoraForm.fromStage(stages.getOrDefault(
                playerId, TsukisoraProgression.getStartingForm().getStage()));
    }

    public void setForm(UUID playerId, TsukisoraForm form) {
        if (playerId == null || form == null) {
            return;
        }
        stages.put(playerId, clampStage(form.getStage()));
        setDirty();
    }

    public TsukisoraForm advance(UUID playerId) {
        TsukisoraForm next = TsukisoraProgression.advance(getForm(playerId));
        setForm(playerId, next);
        return next;
    }

    private static int clampStage(int stage) {
        return Math.max(0, Math.min(stage, TsukisoraForm.values().length - 1));
    }
}

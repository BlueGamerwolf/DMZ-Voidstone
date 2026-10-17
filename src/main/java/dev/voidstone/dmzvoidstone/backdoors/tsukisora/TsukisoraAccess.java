package dev.voidstone.dmzvoidstone.backdoors.tsukisora;

import java.util.UUID;

/**
 * Controls access to the Tsukisora race.
 *
 * Tsukisora is a unique manifestation of the Void.
 * Only the configured owner may use this race.
 *
 * All access checks must be performed on the server.
 */
public final class TsukisoraAccess {

    /**
     * UUID authorized to use the Tsukisora race.
     */
    public static final UUID OWNER_UUID = UUID.fromString(
            "a1bc8320-401b-43ff-a73f-581ea979e506");

    /**
     * Internal race identifier used by DragonMineZ.
     */
    public static final String RACE_ID = "tsukisora";

    private TsukisoraAccess() {
        // Utility class; do not instantiate.
    }

    /**
     * Returns whether the supplied player UUID belongs
     * to the authorized Tsukisora owner.
     */
    public static boolean isOwner(UUID playerUUID) {
        return OWNER_UUID.equals(playerUUID);
    }

    /**
     * Returns whether a race identifier refers to Tsukisora.
     */
    public static boolean isTsukisoraRace(String raceName) {
        return raceName != null
                && RACE_ID.equalsIgnoreCase(raceName.trim());
    }

    /**
     * Determines whether a player may select a race.
     */
    public static boolean canUseRace(UUID playerUUID, String raceName) {
        return !isTsukisoraRace(raceName) || isOwner(playerUUID);
    }
}
package dev.voidstone.dmzvoidstone.backdoors.tsukisora;

/**
 * Provides progression rules for Tsukisora's manifestations.
 *
 * This class does not yet save player data or change
 * DragonMineZ's active form. Those integrations come next.
 */
public final class TsukisoraProgression {

    private TsukisoraProgression() {
    }

    /**
     * The initial manifestation available to Tsukisora.
     */
    public static TsukisoraForm getStartingForm() {
        return TsukisoraForm.MORTAL_MANIFESTATION;
    }

    /**
     * Returns the next manifestation without skipping stages.
     */
    public static TsukisoraForm advance(TsukisoraForm current) {
        if (current == null) {
            return getStartingForm();
        }

        return current.next();
    }

    /**
     * Checks whether a requested stage is unlocked
     * from the currently attained stage.
     */
    public static boolean isUnlocked(
            TsukisoraForm attained,
            TsukisoraForm requested) {
        if (attained == null || requested == null) {
            return false;
        }

        return requested.getStage() <= attained.getStage();
    }
}
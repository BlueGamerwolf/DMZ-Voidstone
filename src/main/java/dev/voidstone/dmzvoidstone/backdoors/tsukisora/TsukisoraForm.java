package dev.voidstone.dmzvoidstone.backdoors.tsukisora;

/**
 * The manifestations of Tsukisora.
 *
 * These are expressions of the Void itself, not
 * transformations that change her underlying nature.
 */
public enum TsukisoraForm {

    /**
     * A limited manifestation capable of existing
     * within the rules of this reality.
     */
    MORTAL_MANIFESTATION(
            0,
            "Mortal Manifestation",
            "A quiet, restrained presence within reality."),

    /**
     * The surrounding reality begins to recognize
     * the Void within her.
     */
    VOID_MANIFESTATION(
            1,
            "Void Manifestation",
            "The emptiness beyond reality begins to emerge."),

    /**
     * Her existence becomes increasingly difficult
     * for the laws of reality to contain.
     */
    UNBOUND_MANIFESTATION(
            2,
            "Unbound Manifestation",
            "Reality struggles to contain her presence."),

    /**
     * A deeper expression of the primordial Void.
     */
    PRIMORDIAL_MANIFESTATION(
            3,
            "Primordial Manifestation",
            "An ancient existence beyond the boundaries of creation."),

    /**
     * The fullest currently defined manifestation.
     */
    THE_VOID_INCARNATE(
            4,
            "The Void Incarnate",
            "The Void fully expressed within existence: both nothing and everything.");

    private final int stage;
    private final String displayName;
    private final String description;

    TsukisoraForm(
            int stage,
            String displayName,
            String description) {
        this.stage = stage;
        this.displayName = displayName;
        this.description = description;
    }

    public int getStage() {
        return stage;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns the next manifestation, or this form
     * if the highest defined stage has been reached.
     */
    public TsukisoraForm next() {
        TsukisoraForm[] forms = values();
        int nextIndex = ordinal() + 1;

        if (nextIndex >= forms.length) {
            return this;
        }

        return forms[nextIndex];
    }

    public boolean hasNext() {
        return ordinal() < values().length - 1;
    }

    public static TsukisoraForm fromStage(int stage) {
        for (TsukisoraForm form : values()) {
            if (form.stage == stage) {
                return form;
            }
        }

        return MORTAL_MANIFESTATION;
    }
}
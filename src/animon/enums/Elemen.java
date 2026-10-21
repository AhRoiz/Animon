package animon.enums;

/**
 * Elemen serangan / pertahanan hewan (ala Persona).
 * Siklus kelemahan: AIR > API > ICE > EARTH > AIR. NORMAL selalu netral.
 */
public enum Elemen {
    API("Api"),
    AIR("Air"),
    EARTH("Earth"),
    ICE("Ice"),
    NORMAL("Normal");

    private final String label;

    Elemen(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Elemen fromString(String teks) {
        if (teks != null) {
            String t = teks.trim();
            for (Elemen e : values()) {
                if (e.name().equalsIgnoreCase(t) || e.label.equalsIgnoreCase(t)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Elemen tidak dikenal: '" + teks + "'");
    }
}

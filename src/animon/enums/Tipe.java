package animon.enums;

/**
 * Tipe fisik / habitat hewan.
 * Dipakai oleh {@code TypeChart} untuk kalkulasi Type Advantage (miss / crit / resist / efek khusus).
 */
public enum Tipe {
    DARAT("Darat"),
    TERBANG("Terbang"),
    AIR("Air"),
    NAGA("Naga"),
    ROBOT("Robot");

    private final String label;

    Tipe(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Parsing toleran (case-insensitive, menerima nama enum maupun label). */
    public static Tipe fromString(String teks) {
        if (teks != null) {
            String t = teks.trim();
            for (Tipe tipe : values()) {
                if (tipe.name().equalsIgnoreCase(t) || tipe.label.equalsIgnoreCase(t)) {
                    return tipe;
                }
            }
        }
        throw new IllegalArgumentException("Tipe tidak dikenal: '" + teks + "'");
    }
}

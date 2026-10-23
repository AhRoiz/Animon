package animon.enums;

/** Jenis item di katalog JSON. Menentukan child class {@code Item} yang dibuat GameSeeder. */
public enum JenisItem {
    HEAL("Heal"),
    BUFF("Buff"),
    EVOLUSI("Evolusi");

    private final String label;

    JenisItem(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static JenisItem fromString(String teks) {
        if (teks != null) {
            String t = teks.trim();
            for (JenisItem j : values()) {
                if (j.name().equalsIgnoreCase(t) || j.label.equalsIgnoreCase(t)) {
                    return j;
                }
            }
        }
        throw new IllegalArgumentException("Jenis item tidak dikenal: '" + teks + "'");
    }
}

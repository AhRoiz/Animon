package animon.enums;

/** Role tempur hewan. Menentukan child class yang dibuat oleh GameSeeder. */
public enum Role {
    DPS("DPS", "Fokus damage tinggi"),
    SUPPORT("Support", "Lifesteal / heal kawan"),
    TANK("Tank", "HP tebal, Shield / Taunt");

    private final String label;
    private final String deskripsi;

    Role(String label, String deskripsi) {
        this.label = label;
        this.deskripsi = deskripsi;
    }

    public String getLabel() {
        return label;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public static Role fromString(String teks) {
        if (teks != null) {
            String t = teks.trim();
            for (Role r : values()) {
                if (r.name().equalsIgnoreCase(t) || r.label.equalsIgnoreCase(t)) {
                    return r;
                }
            }
        }
        throw new IllegalArgumentException("Role tidak dikenal: '" + teks + "'");
    }
}

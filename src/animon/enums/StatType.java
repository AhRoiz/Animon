package animon.enums;

/**
 * Stat yang bisa dipengaruhi buff/debuff.
 * ATTACK/DEFENSE/SPEED = persentase pengali stat dasar.
 * EVASION/CRIT        = tambahan poin persen peluang.
 * TAUNT               = status khusus: hewan tidak bisa memakai skill.
 */
public enum StatType {
    ATTACK("ATK"),
    DEFENSE("DEF"),
    SPEED("SPD"),
    EVASION("EVA"),
    CRIT("CRIT"),
    TAUNT("TAUNT");

    private final String label;

    StatType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static StatType fromString(String teks) {
        if (teks != null) {
            String t = teks.trim();
            for (StatType s : values()) {
                if (s.name().equalsIgnoreCase(t) || s.label.equalsIgnoreCase(t)) {
                    return s;
                }
            }
        }
        throw new IllegalArgumentException("Stat tidak dikenal: '" + teks + "'");
    }
}

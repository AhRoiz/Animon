package animon.enums;

/**
 * Mode skill. Setiap mode hanya valid untuk satu Role
 * (divalidasi oleh GameSeeder saat membaca JSON).
 */
public enum SkillMode {
    /** DPS: damage besar + bonus peluang critical. */
    BURST(Role.DPS),
    /** DPS: damage yang menembus sebagian DEF lawan. */
    PIERCE(Role.DPS),
    /** Support: damage musuh, sebagian damage jadi HP sendiri. */
    LIFESTEAL(Role.SUPPORT),
    /** Support: heal kawan (atau diri sendiri) + hapus debuff. */
    HEAL(Role.SUPPORT),
    /** Tank: shield penyerap damage + otomatis Defend. */
    SHIELD(Role.TANK),
    /** Tank: serangan yang membuat lawan terprovokasi (tidak bisa skill). */
    TAUNT(Role.TANK);

    private final Role role;

    SkillMode(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    public static SkillMode fromString(String teks) {
        if (teks != null) {
            for (SkillMode m : values()) {
                if (m.name().equalsIgnoreCase(teks.trim())) {
                    return m;
                }
            }
        }
        throw new IllegalArgumentException("Mode skill tidak dikenal: '" + teks + "'");
    }
}

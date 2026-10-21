package animon.model;

import animon.enums.SkillMode;
import animon.enums.TargetSkill;

import java.util.Objects;

/**
 * Data skill milik seekor hewan (immutable). Arti {@code power} dan {@code nilai}
 * bergantung pada {@link SkillMode}:
 * <ul>
 *   <li>BURST     : power = pengali damage, nilai = bonus % crit</li>
 *   <li>PIERCE    : power = pengali damage, nilai = % DEF lawan yang ditembus</li>
 *   <li>LIFESTEAL : power = pengali damage, nilai = % damage yang jadi HP sendiri</li>
 *   <li>HEAL      : nilai = % maxHp kawan yang dipulihkan (+ hapus debuff)</li>
 *   <li>SHIELD    : nilai = shield sebesar % maxHp sendiri (+ otomatis Defend)</li>
 *   <li>TAUNT     : power = pengali damage, nilai = durasi provokasi (giliran)</li>
 * </ul>
 */
public final class Skill {

    private final String nama;
    private final SkillMode mode;
    private final int manaCost;
    private final double power;
    private final int nilai;
    private final String deskripsi;

    public Skill(String nama, SkillMode mode, int manaCost, double power, int nilai, String deskripsi) {
        this.nama = Objects.requireNonNull(nama, "nama skill");
        this.mode = Objects.requireNonNull(mode, "mode skill");
        if (manaCost < 0) throw new IllegalArgumentException("manaCost tidak boleh negatif");
        if (power < 0) throw new IllegalArgumentException("power tidak boleh negatif");
        if (nilai < 0) throw new IllegalArgumentException("nilai tidak boleh negatif");
        if (isMenyerang(mode) && power <= 0) {
            throw new IllegalArgumentException("Skill " + mode + " wajib punya power > 0");
        }
        if (mode == SkillMode.TAUNT && nilai < 1) {
            throw new IllegalArgumentException("Skill TAUNT wajib punya nilai (durasi) >= 1");
        }
        this.manaCost = manaCost;
        this.power = power;
        this.nilai = nilai;
        this.deskripsi = deskripsi == null ? "" : deskripsi;
    }

    private static boolean isMenyerang(SkillMode m) {
        return m != SkillMode.HEAL && m != SkillMode.SHIELD;
    }

    /** Sasaran skill, dipakai engine untuk menentukan perlu memilih target atau tidak. */
    public TargetSkill getTarget() {
        switch (mode) {
            case HEAL:   return TargetSkill.KAWAN;
            case SHIELD: return TargetSkill.DIRI;
            default:     return TargetSkill.MUSUH;
        }
    }

    /** Ringkasan efek untuk ditampilkan di menu. */
    public String getRingkasan() {
        switch (mode) {
            case BURST:     return String.format("Dmg x%.2f, +%d%% crit", power, nilai);
            case PIERCE:    return String.format("Dmg x%.2f, tembus %d%% DEF", power, nilai);
            case LIFESTEAL: return String.format("Dmg x%.2f, serap %d%% jadi HP", power, nilai);
            case HEAL:      return String.format("Heal %d%% maxHP kawan + hapus debuff", nilai);
            case SHIELD:    return String.format("Shield %d%% maxHP + Defend", nilai);
            case TAUNT:     return String.format("Dmg x%.2f, lawan tak bisa skill %d giliran", power, nilai);
            default:        return "";
        }
    }

    public String getNama()      { return nama; }
    public SkillMode getMode()   { return mode; }
    public int getManaCost()     { return manaCost; }
    public double getPower()     { return power; }
    public int getNilai()        { return nilai; }
    public String getDeskripsi() { return deskripsi; }
}

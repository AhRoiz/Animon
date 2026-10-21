package animon.battle;

import animon.enums.Elemen;
import animon.model.Hewan;
import animon.model.Skill;

/**
 * Deskripsi sebuah serangan yang akan dihitung {@link DamageCalculator}:
 * nama, elemen, power, bonus crit, dan persentase tembus DEF.
 */
public final class Serangan {

    private final String nama;
    private final Elemen elemen;
    private final double power;
    private int bonusCrit;
    private int pierce;

    private Serangan(String nama, Elemen elemen, double power) {
        this.nama = nama;
        this.elemen = elemen;
        this.power = power;
    }

    /** Serangan biasa: elemen Normal (fisik), power 1.0. */
    public static Serangan biasa() {
        return new Serangan("Serangan Biasa", Elemen.NORMAL, 1.0);
    }

    /** Serangan skill: memakai elemen hewan dan power skill-nya. */
    public static Serangan skill(Hewan penyerang) {
        Skill s = penyerang.getSkill();
        return new Serangan(s.getNama(), penyerang.getElemen(), s.getPower());
    }

    public Serangan denganBonusCrit(int persen) {
        this.bonusCrit = Math.max(0, persen);
        return this;
    }

    public Serangan denganPierce(int persen) {
        this.pierce = Math.max(0, Math.min(100, persen));
        return this;
    }

    public String getNama()   { return nama; }
    public Elemen getElemen() { return elemen; }
    public double getPower()  { return power; }
    public int getBonusCrit() { return bonusCrit; }
    public int getPierce()    { return pierce; }
}

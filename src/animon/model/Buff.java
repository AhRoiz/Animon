package animon.model;

import animon.enums.StatType;

import java.util.Objects;

/**
 * Efek sementara pada hewan (buff bila nilai positif, debuff bila negatif, atau TAUNT).
 *
 * ATTACK/DEFENSE/SPEED : nilai = persen pengali stat dasar (mis. +30 = +30%).
 * EVASION/CRIT         : nilai = tambahan poin persen peluang.
 * TAUNT                : status; hewan tidak bisa memakai skill.
 *
 * Durasi dihitung dalam giliran pemilik hewan (dikurangi di akhir giliran pemilik).
 */
public final class Buff {

    private final String sumber;
    private final StatType stat;
    private final int nilai;
    private int sisaDurasi;

    public Buff(String sumber, StatType stat, int nilai, int durasi) {
        this.sumber = Objects.requireNonNull(sumber, "sumber");
        this.stat = Objects.requireNonNull(stat, "stat");
        if (durasi < 1) throw new IllegalArgumentException("Durasi buff minimal 1 giliran");
        this.nilai = nilai;
        this.sisaDurasi = durasi;
    }

    /** Factory untuk status provokasi. */
    public static Buff taunt(String sumber, int durasi) {
        return new Buff(sumber, StatType.TAUNT, 0, durasi);
    }

    public boolean isDebuff() {
        return stat == StatType.TAUNT || nilai < 0;
    }

    /** ATTACK/DEFENSE/SPEED berupa persentase; EVASION/CRIT berupa poin. */
    public boolean isPersen() {
        return stat == StatType.ATTACK || stat == StatType.DEFENSE || stat == StatType.SPEED;
    }

    /** Label ringkas, mis. "ATK+30%(2)" atau "TAUNT(1)". */
    public String getLabel() {
        if (stat == StatType.TAUNT) {
            return "TAUNT(" + sisaDurasi + ")";
        }
        String tanda = nilai >= 0 ? "+" : "";
        return stat.getLabel() + tanda + nilai + (isPersen() ? "%" : "") + "(" + sisaDurasi + ")";
    }

    /** Dipanggil oleh {@link Hewan#tickBuff()} di akhir giliran pemilik. */
    void kurangiDurasi() {
        sisaDurasi--;
    }

    public boolean isHabis() {
        return sisaDurasi <= 0;
    }

    public String getSumber()   { return sumber; }
    public StatType getStat()   { return stat; }
    public int getNilai()       { return nilai; }
    public int getSisaDurasi()  { return sisaDurasi; }
}

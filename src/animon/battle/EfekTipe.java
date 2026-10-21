package animon.battle;

import animon.enums.StatType;
import animon.model.Buff;

import java.util.ArrayList;
import java.util.List;

/**
 * Efek Type Advantage antara tipe penyerang dan tipe bertahan (immutable).
 * Bisa menambah peluang miss, peluang crit, pengali damage, dan debuff khusus.
 */
public final class EfekTipe {

    public static final EfekTipe NETRAL = new EfekTipe(0, 0, 1.0, null);

    private final int bonusMiss;
    private final int bonusCrit;
    private final double multiplier;
    private final String keterangan;

    private final String namaDebuff;
    private final StatType debuffStat;
    private final int debuffNilai;
    private final int debuffDurasi;
    private final int debuffPeluang;

    public EfekTipe(int bonusMiss, int bonusCrit, double multiplier, String keterangan) {
        this(bonusMiss, bonusCrit, multiplier, keterangan, null, null, 0, 0, 0);
    }

    private EfekTipe(int bonusMiss, int bonusCrit, double multiplier, String keterangan,
                     String namaDebuff, StatType debuffStat, int debuffNilai, int debuffDurasi, int debuffPeluang) {
        this.bonusMiss = bonusMiss;
        this.bonusCrit = bonusCrit;
        this.multiplier = multiplier;
        this.keterangan = keterangan;
        this.namaDebuff = namaDebuff;
        this.debuffStat = debuffStat;
        this.debuffNilai = debuffNilai;
        this.debuffDurasi = debuffDurasi;
        this.debuffPeluang = debuffPeluang;
    }

    /** Salinan dengan debuff khusus yang punya {@code peluang}% untuk menempel ke target. */
    public EfekTipe denganDebuff(String nama, StatType stat, int nilai, int durasi, int peluang) {
        return new EfekTipe(bonusMiss, bonusCrit, multiplier, keterangan, nama, stat, nilai, durasi, peluang);
    }

    public boolean adaDebuff() {
        return debuffStat != null;
    }

    public Buff buatDebuff() {
        return new Buff(namaDebuff, debuffStat, debuffNilai, debuffDurasi);
    }

    public boolean isNetral() {
        return this == NETRAL;
    }

    /** Ringkasan angka, mis. "miss +35%, dmg x0.75". */
    public String ringkasanAngka() {
        List<String> bagian = new ArrayList<>();
        if (bonusMiss != 0) bagian.add("miss " + (bonusMiss > 0 ? "+" : "") + bonusMiss + "%");
        if (bonusCrit != 0) bagian.add("crit +" + bonusCrit + "%");
        if (multiplier != 1.0) bagian.add(String.format("dmg x%.2f", multiplier));
        if (adaDebuff()) {
            bagian.add(debuffPeluang + "% " + namaDebuff + " (" + debuffStat.getLabel() + debuffNilai
                    + "% " + debuffDurasi + " giliran)");
        }
        return String.join(", ", bagian);
    }

    public int getBonusMiss()      { return bonusMiss; }
    public int getBonusCrit()      { return bonusCrit; }
    public double getMultiplier()  { return multiplier; }
    public String getKeterangan()  { return keterangan; }
    public String getNamaDebuff()  { return namaDebuff; }
    public int getDebuffPeluang()  { return debuffPeluang; }
}

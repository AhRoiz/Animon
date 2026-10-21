package animon.battle;

import animon.enums.Efektivitas;
import animon.model.Hewan;
import animon.util.Acak;

/**
 * Kalkulator damage. Urutan kalkulasi:
 * <ol>
 *   <li>Type Advantage ({@link TypeChart}) -&gt; bonus miss / crit / multiplier / debuff khusus</li>
 *   <li>Cek miss: 5% + evasion target + bonus tipe + selisih speed (bila target lebih cepat)</li>
 *   <li>Elemental Weakness ({@link ElemenChart}) -&gt; x2 / x1 / x0.5</li>
 *   <li>Crit: crit hewan + bonus skill + bonus tipe -&gt; x1.5</li>
 *   <li>Rumus dasar: ATK * ATK/(ATK+DEF') * 1.6 * power * faktor level * variasi(0.9..1.1)</li>
 *   <li>Defend: x0.5 bila target {@code isDefending}</li>
 *   <li>Pasif role (DPS +10% / Tank -10%)</li>
 * </ol>
 * Mengenai kelemahan atau crit memberikan status "ONE MORE".
 */
public final class DamageCalculator {

    public static final double KONSTANTA = 1.6;
    public static final double MULT_CRIT = 1.5;
    public static final double MULT_DEFEND = 0.5;
    public static final int MISS_DASAR = 5;
    public static final int MISS_MAKS = 80;
    public static final int CRIT_MAKS = 90;

    private DamageCalculator() {
    }

    public static int peluangMiss(Hewan penyerang, Hewan target, EfekTipe efekTipe) {
        int selisihSpeed = Math.max(0, target.getSpeedEfektif() - penyerang.getSpeedEfektif()) / 10;
        int miss = MISS_DASAR + target.getEvasionEfektif() + efekTipe.getBonusMiss() + selisihSpeed;
        return batas(miss, 0, MISS_MAKS);
    }

    public static int peluangCrit(Hewan penyerang, Serangan serangan, EfekTipe efekTipe) {
        int crit = penyerang.getCritChance() + serangan.getBonusCrit() + efekTipe.getBonusCrit();
        return batas(crit, 0, CRIT_MAKS);
    }

    /** Hitung DAN terapkan damage ke target. */
    public static HasilSerangan hitung(Hewan penyerang, Hewan target, Serangan serangan) {
        HasilSerangan h = new HasilSerangan();
        h.log(penyerang.getNama() + " menggunakan " + serangan.getNama() + "!");

        EfekTipe efekTipe = TypeChart.cek(penyerang.getTipe(), target.getTipe());

        // 1. Miss
        if (Acak.peluang(peluangMiss(penyerang, target, efekTipe))) {
            h.log("Serangan meleset! " + target.getNama() + " berhasil menghindar.");
            if (efekTipe.getBonusMiss() > 0) h.log("[Tipe] " + efekTipe.getKeterangan() + ".");
            return h;
        }
        h.setKena(true);

        // 2. Elemen & crit
        Efektivitas ef = ElemenChart.cek(serangan.getElemen(), target.getElemen());
        boolean crit = Acak.peluang(peluangCrit(penyerang, serangan, efekTipe));
        h.setEfektivitas(ef);
        h.setCrit(crit);

        // 3. Rumus dasar
        double atk = penyerang.getAttackEfektif();
        double def = target.getDefenseEfektif() * (100 - serangan.getPierce()) / 100.0;
        double dasar = atk * (atk / (atk + def)) * KONSTANTA * serangan.getPower();
        double faktorLevel = 1.0 + (penyerang.getLevel() - 5) * 0.05;
        double variasi = 0.9 + Acak.desimal() * 0.2;

        double total = dasar * faktorLevel * variasi
                * ElemenChart.multiplier(ef)
                * efekTipe.getMultiplier()
                * penyerang.modifierDamageDiberikan()
                * target.modifierDamageDiterima();
        if (crit) total *= MULT_CRIT;
        if (target.isDefending()) total *= MULT_DEFEND;

        int damage = Math.max(1, (int) Math.round(total));
        h.setDamage(damage);

        // 4. Log modifier
        if (!efekTipe.isNetral() && efekTipe.getMultiplier() != 1.0 || efekTipe.getBonusCrit() > 0 && crit) {
            h.log("[Tipe] " + efekTipe.getKeterangan() + ".");
        }
        if (ef == Efektivitas.LEMAH) {
            h.log("WEAK! Serangan " + serangan.getElemen().getLabel() + " super efektif terhadap "
                    + target.getElemen().getLabel() + "! (x2)");
        } else if (ef == Efektivitas.RESIST) {
            h.log("RESIST... " + target.getNama() + " tahan terhadap " + serangan.getElemen().getLabel() + ". (x0.5)");
        }
        if (crit) h.log("CRITICAL HIT! (x1.5)");
        if (target.isDefending()) h.log(target.getNama() + " bertahan, damage dikurangi setengah.");

        // 5. Terapkan
        int shieldAwal = target.getShield();
        int hilang = target.terimaDamage(damage);
        int diserap = shieldAwal - target.getShield();
        h.setDiserapShield(diserap);
        h.setHpHilang(hilang);
        if (diserap > 0) h.log("Shield menyerap " + diserap + " damage.");
        h.log(target.getNama() + " menerima " + hilang + " damage. (HP " + target.getHp() + "/" + target.getMaxHp() + ")");

        // 6. Efek khusus tipe
        if (efekTipe.adaDebuff() && !target.isFainted() && Acak.peluang(efekTipe.getDebuffPeluang())) {
            target.tambahBuff(efekTipe.buatDebuff());
            h.log("[Tipe] " + efekTipe.getKeterangan() + ": " + target.getNama() + " "
                    + efekTipe.getNamaDebuff() + "! SPD turun.");
        }

        if (target.isFainted()) {
            h.setTargetPingsan(true);
            h.log(target.getNama() + " pingsan!");
        } else {
            h.setOneMore(ef == Efektivitas.LEMAH || crit);
        }
        return h;
    }

    private static int batas(int nilai, int min, int max) {
        return Math.max(min, Math.min(max, nilai));
    }
}

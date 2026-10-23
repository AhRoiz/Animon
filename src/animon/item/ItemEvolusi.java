package animon.item;

import animon.battle.HasilAksi;
import animon.enums.Elemen;
import animon.enums.JenisItem;
import animon.enums.Tipe;
import animon.model.Hewan;

import java.util.ArrayList;
import java.util.List;

/**
 * Child class item evolusi: mengubah elemen dan/atau tipe hewan secara permanen, naik level,
 * dan/atau menaikkan max stats. Field {@code elemenBaru} / {@code tipeBaru} boleh null
 * (artinya tidak diubah).
 */
public class ItemEvolusi extends Item {

    private final Elemen elemenBaru;
    private final Tipe tipeBaru;
    private final int naikLevel;
    private final int bonusMaxHp;
    private final int bonusAtk;
    private final int bonusDef;
    private final int bonusSpeed;

    public ItemEvolusi(String id, String nama, int hargaBeli, String deskripsi,
                       Elemen elemenBaru, Tipe tipeBaru, int naikLevel,
                       int bonusMaxHp, int bonusAtk, int bonusDef, int bonusSpeed) {
        super(id, nama, hargaBeli, deskripsi);
        if (naikLevel < 0 || bonusMaxHp < 0 || bonusAtk < 0 || bonusDef < 0 || bonusSpeed < 0) {
            throw new IllegalArgumentException("Bonus evolusi tidak boleh negatif");
        }
        if (elemenBaru == null && tipeBaru == null && naikLevel + bonusMaxHp + bonusAtk + bonusDef + bonusSpeed == 0) {
            throw new IllegalArgumentException("ItemEvolusi '" + nama + "' tidak punya efek");
        }
        this.elemenBaru = elemenBaru;
        this.tipeBaru = tipeBaru;
        this.naikLevel = naikLevel;
        this.bonusMaxHp = bonusMaxHp;
        this.bonusAtk = bonusAtk;
        this.bonusDef = bonusDef;
        this.bonusSpeed = bonusSpeed;
    }

    @Override
    public JenisItem getJenis() {
        return JenisItem.EVOLUSI;
    }

    @Override
    public String cekPenggunaan(Hewan target) {
        String alasan = cekTargetHidup(target);
        if (alasan != null) return alasan;
        boolean adaTransformasi = elemenBaru != null || tipeBaru != null;
        boolean elemenSudah = elemenBaru == null || target.getElemen() == elemenBaru;
        boolean tipeSudah = tipeBaru == null || target.getTipe() == tipeBaru;
        if (adaTransformasi && elemenSudah && tipeSudah) {
            return target.getNama() + " sudah ber" + (elemenBaru != null
                    ? "elemen " + elemenBaru.getLabel() : "tipe " + tipeBaru.getLabel()) + ".";
        }
        return null;
    }

    @Override
    public HasilAksi gunakan(Hewan target) {
        String alasan = cekPenggunaan(target);
        if (alasan != null) return HasilAksi.gagal(alasan);

        HasilAksi hasil = awalLog(target);
        if (elemenBaru != null && target.getElemen() != elemenBaru) {
            Elemen lama = target.getElemen();
            target.ubahElemen(elemenBaru);
            hasil.log("Elemen " + target.getNama() + " berubah: " + lama.getLabel() + " -> " + elemenBaru.getLabel() + "!");
        }
        if (tipeBaru != null && target.getTipe() != tipeBaru) {
            Tipe lama = target.getTipe();
            target.ubahTipe(tipeBaru);
            hasil.log("Tipe " + target.getNama() + " berubah: " + lama.getLabel() + " -> " + tipeBaru.getLabel() + "!");
        }
        if (naikLevel > 0) {
            target.naikLevel(naikLevel);
            hasil.log(target.getNama() + " naik ke Level " + target.getLevel() + "!");
        }
        List<String> stat = new ArrayList<>();
        if (bonusMaxHp > 0) { target.tambahMaxHp(bonusMaxHp); stat.add("MaxHP+" + bonusMaxHp); }
        if (bonusAtk > 0)   { target.tambahAttack(bonusAtk);  stat.add("ATK+" + bonusAtk); }
        if (bonusDef > 0)   { target.tambahDefense(bonusDef); stat.add("DEF+" + bonusDef); }
        if (bonusSpeed > 0) { target.tambahSpeed(bonusSpeed); stat.add("SPD+" + bonusSpeed); }
        if (!stat.isEmpty()) hasil.log("Stat permanen naik: " + String.join(", ", stat) + ".");
        return hasil;
    }

    @Override
    public String getRingkasan() {
        List<String> bagian = new ArrayList<>();
        if (elemenBaru != null) bagian.add("Elemen -> " + elemenBaru.getLabel());
        if (tipeBaru != null) bagian.add("Tipe -> " + tipeBaru.getLabel());
        if (naikLevel > 0) bagian.add("Lv+" + naikLevel);
        if (bonusMaxHp > 0) bagian.add("MaxHP+" + bonusMaxHp);
        if (bonusAtk > 0) bagian.add("ATK+" + bonusAtk);
        if (bonusDef > 0) bagian.add("DEF+" + bonusDef);
        if (bonusSpeed > 0) bagian.add("SPD+" + bonusSpeed);
        return String.join(", ", bagian);
    }

    public Elemen getElemenBaru() { return elemenBaru; }
    public Tipe getTipeBaru()     { return tipeBaru; }
}

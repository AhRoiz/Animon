package animon.item;

import animon.battle.HasilAksi;
import animon.enums.JenisItem;
import animon.model.Hewan;

import java.util.ArrayList;
import java.util.List;

/**
 * Child class item pemulih: menambah HP dan/atau Mana, atau membangkitkan hewan yang pingsan
 * (mode revive, mis. Bulu Phoenix).
 */
public class ItemHeal extends Item {

    private final int hp;
    private final int mana;
    private final boolean revive;
    private final int revivePersen;

    public ItemHeal(String id, String nama, int hargaBeli, String deskripsi,
                    int hp, int mana, boolean revive, int revivePersen) {
        super(id, nama, hargaBeli, deskripsi);
        if (hp < 0 || mana < 0) throw new IllegalArgumentException("hp/mana item tidak boleh negatif");
        if (revive && (revivePersen < 1 || revivePersen > 100)) {
            throw new IllegalArgumentException("revivePersen harus 1..100");
        }
        if (!revive && hp == 0 && mana == 0) {
            throw new IllegalArgumentException("ItemHeal '" + nama + "' tidak memulihkan apa pun");
        }
        this.hp = hp;
        this.mana = mana;
        this.revive = revive;
        this.revivePersen = revivePersen;
    }

    @Override
    public JenisItem getJenis() {
        return JenisItem.HEAL;
    }

    @Override
    public boolean untukHewanPingsan() {
        return revive;
    }

    @Override
    public String cekPenggunaan(Hewan target) {
        if (target == null) return "Pilih hewan target.";
        if (revive) {
            return target.isFainted() ? null : target.getNama() + " tidak sedang pingsan.";
        }
        String alasan = cekTargetHidup(target);
        if (alasan != null) return alasan;
        boolean hpBerguna = hp > 0 && target.getHp() < target.getMaxHp();
        boolean mpBerguna = mana > 0 && target.getMana() < target.getMaxMana();
        if (!hpBerguna && !mpBerguna) return target.getNama() + " sudah dalam kondisi penuh.";
        return null;
    }

    @Override
    public HasilAksi gunakan(Hewan target) {
        String alasan = cekPenggunaan(target);
        if (alasan != null) return HasilAksi.gagal(alasan);

        HasilAksi hasil = awalLog(target);
        if (revive) {
            target.bangkit(revivePersen);
            return hasil.log(target.getNama() + " bangkit kembali! (HP " + target.getHp() + "/" + target.getMaxHp()
                    + ", MP " + target.getMana() + "/" + target.getMaxMana() + ")");
        }
        int naikHp = target.pulihkanHp(hp);
        int naikMp = target.pulihkanMana(mana);
        if (naikHp > 0) hasil.log(target.getNama() + " pulih " + naikHp + " HP. (HP " + target.getHp() + "/" + target.getMaxHp() + ")");
        if (naikMp > 0) hasil.log(target.getNama() + " pulih " + naikMp + " MP. (MP " + target.getMana() + "/" + target.getMaxMana() + ")");
        return hasil;
    }

    @Override
    public String getRingkasan() {
        if (revive) return "Bangkitkan hewan pingsan (" + revivePersen + "% HP)";
        List<String> bagian = new ArrayList<>();
        if (hp > 0) bagian.add(hp >= 999 ? "HP penuh" : "HP +" + hp);
        if (mana > 0) bagian.add(mana >= 999 ? "MP penuh" : "MP +" + mana);
        return String.join(", ", bagian);
    }

    public int getHp()            { return hp; }
    public int getMana()          { return mana; }
    public boolean isRevive()     { return revive; }
    public int getRevivePersen()  { return revivePersen; }
}

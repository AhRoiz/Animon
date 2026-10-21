package animon.model;

import animon.battle.DamageCalculator;
import animon.battle.HasilAksi;
import animon.battle.HasilSerangan;
import animon.battle.Serangan;
import animon.enums.Elemen;
import animon.enums.Role;
import animon.enums.SkillMode;
import animon.enums.Tipe;

/**
 * Child class role Support.
 * Pasif "Aliran Mana": regenerasi mana 12% per giliran (normal 8%).
 * Skill: LIFESTEAL (damage musuh + HP sendiri) atau HEAL (pulihkan kawan + hapus debuff).
 */
public class HewanSupport extends Hewan {

    public static final int REGEN_MANA_SUPPORT = 12;

    public HewanSupport(String id, String nama, Tipe tipe, Elemen elemen, int level,
                        Stat stat, Skill skill, String deskripsi) {
        super(id, nama, tipe, elemen, level, stat, skill, deskripsi);
        validasiRole(Role.SUPPORT);
    }

    @Override
    public Role getRole() {
        return Role.SUPPORT;
    }

    @Override
    public int hitungRegenMana() {
        return Math.max(4, getMaxMana() * REGEN_MANA_SUPPORT / 100);
    }

    @Override
    public String getDeskripsiPasif() {
        return "Aliran Mana: regen MP 12%/giliran (normal 8%)";
    }

    @Override
    public HasilAksi gunakanSkill(Hewan target) {
        String alasan = cekSkill();
        if (alasan != null) return HasilAksi.gagal(alasan);
        return getSkill().getMode() == SkillMode.LIFESTEAL ? lifesteal(target) : heal(target);
    }

    private HasilAksi lifesteal(Hewan musuh) {
        if (musuh == null || musuh.isFainted()) return HasilAksi.gagal("Tidak ada lawan untuk diserang.");
        bayarMana();
        HasilSerangan hs = DamageCalculator.hitung(this, musuh, Serangan.skill(this));
        HasilAksi hasil = HasilAksi.dariSerangan(hs);
        if (hs.isKena()) {
            int serap = hs.getDamageMasuk() * getSkill().getNilai() / 100;
            int naik = pulihkanHp(serap);
            if (naik > 0) {
                hasil.log(getNama() + " menyerap " + naik + " HP! (HP " + getHp() + "/" + getMaxHp() + ")");
            }
        }
        return hasil;
    }

    private HasilAksi heal(Hewan kawan) {
        if (kawan == null) return HasilAksi.gagal("Pilih kawan yang ingin dipulihkan.");
        if (kawan.isFainted()) return HasilAksi.gagal(kawan.getNama() + " pingsan; gunakan item revive.");
        bayarMana();
        HasilAksi hasil = HasilAksi.baru().log(getNama() + " menggunakan " + getSkill().getNama() + "!");
        int naik = kawan.pulihkanHp(kawan.getMaxHp() * getSkill().getNilai() / 100);
        hasil.log(kawan.getNama() + " pulih " + naik + " HP. (HP " + kawan.getHp() + "/" + kawan.getMaxHp() + ")");
        int dihapus = kawan.hapusDebuff();
        if (dihapus > 0) {
            hasil.log(dihapus + " debuff pada " + kawan.getNama() + " dihapus.");
        }
        return hasil;
    }
}

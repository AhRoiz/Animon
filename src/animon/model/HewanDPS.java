package animon.model;

import animon.battle.DamageCalculator;
import animon.battle.HasilAksi;
import animon.battle.Serangan;
import animon.enums.Elemen;
import animon.enums.Role;
import animon.enums.SkillMode;
import animon.enums.Tipe;

/**
 * Child class role DPS: fokus damage tinggi.
 * Pasif "Insting Pemburu": +10% damage dan +5% crit.
 * Skill: BURST (bonus crit) atau PIERCE (menembus DEF).
 */
public class HewanDPS extends Hewan {

    public static final int BONUS_CRIT = 5;
    public static final double BONUS_DAMAGE = 1.10;

    public HewanDPS(String id, String nama, Tipe tipe, Elemen elemen, int level,
                    Stat stat, Skill skill, String deskripsi) {
        super(id, nama, tipe, elemen, level, stat, skill, deskripsi);
        validasiRole(Role.DPS);
    }

    @Override
    public Role getRole() {
        return Role.DPS;
    }

    @Override
    protected int bonusCritPasif() {
        return BONUS_CRIT;
    }

    @Override
    public double modifierDamageDiberikan() {
        return BONUS_DAMAGE;
    }

    @Override
    public String getDeskripsiPasif() {
        return "Insting Pemburu: damage +10%, crit +5%";
    }

    @Override
    public HasilAksi gunakanSkill(Hewan target) {
        String alasan = cekSkill();
        if (alasan != null) return HasilAksi.gagal(alasan);
        if (target == null || target.isFainted()) return HasilAksi.gagal("Tidak ada lawan untuk diserang.");

        bayarMana();
        Serangan serangan = Serangan.skill(this);
        if (getSkill().getMode() == SkillMode.BURST) {
            serangan.denganBonusCrit(getSkill().getNilai());
        } else {
            serangan.denganPierce(getSkill().getNilai());
        }
        return HasilAksi.dariSerangan(DamageCalculator.hitung(this, target, serangan));
    }
}

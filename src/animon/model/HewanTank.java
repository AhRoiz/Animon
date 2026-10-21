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
 * Child class role Tank.
 * Pasif "Kulit Tebal": damage yang diterima -10%.
 * Skill: SHIELD (penyerap damage + otomatis Defend) atau TAUNT (lawan tak bisa skill).
 */
public class HewanTank extends Hewan {

    public static final double REDUKSI_DAMAGE = 0.90;

    public HewanTank(String id, String nama, Tipe tipe, Elemen elemen, int level,
                     Stat stat, Skill skill, String deskripsi) {
        super(id, nama, tipe, elemen, level, stat, skill, deskripsi);
        validasiRole(Role.TANK);
    }

    @Override
    public Role getRole() {
        return Role.TANK;
    }

    @Override
    public double modifierDamageDiterima() {
        return REDUKSI_DAMAGE;
    }

    @Override
    public String getDeskripsiPasif() {
        return "Kulit Tebal: damage diterima -10%";
    }

    @Override
    public HasilAksi gunakanSkill(Hewan target) {
        String alasan = cekSkill();
        if (alasan != null) return HasilAksi.gagal(alasan);
        return getSkill().getMode() == SkillMode.SHIELD ? shield() : taunt(target);
    }

    private HasilAksi shield() {
        bayarMana();
        int jumlah = getMaxHp() * getSkill().getNilai() / 100;
        pasangShield(jumlah);
        setDefending(true);
        return HasilAksi.baru()
                .log(getNama() + " menggunakan " + getSkill().getNama() + "!")
                .log(getNama() + " memasang shield " + getShield() + " dan mengambil posisi bertahan.");
    }

    private HasilAksi taunt(Hewan musuh) {
        if (musuh == null || musuh.isFainted()) return HasilAksi.gagal("Tidak ada lawan untuk diserang.");
        bayarMana();
        HasilSerangan hs = DamageCalculator.hitung(this, musuh, Serangan.skill(this));
        HasilAksi hasil = HasilAksi.dariSerangan(hs);
        if (hs.isKena() && !musuh.isFainted()) {
            int durasi = getSkill().getNilai();
            musuh.tambahBuff(Buff.taunt(getSkill().getNama(), durasi));
            hasil.log(musuh.getNama() + " terprovokasi! Tidak bisa memakai skill selama " + durasi + " giliran.");
        }
        return hasil;
    }
}

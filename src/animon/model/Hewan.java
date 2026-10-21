package animon.model;

import animon.battle.DamageCalculator;
import animon.battle.HasilAksi;
import animon.battle.Serangan;
import animon.enums.Elemen;
import animon.enums.Role;
import animon.enums.StatType;
import animon.enums.Tipe;
import animon.kontrak.IPetarung;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * ABSTRACT CLASS karakter utama.
 *
 * <ul>
 *   <li><b>Encapsulation</b>: semua stat private; perubahan hanya lewat method yang menjaga
 *       invariant (HP tidak negatif / tidak melebihi max, dsb.).</li>
 *   <li><b>Abstraction</b>: {@link #gunakanSkill(Hewan)} dan {@link #getRole()} abstract,
 *       serta mengimplementasikan interface {@link IPetarung}.</li>
 *   <li><b>Polymorphism</b>: engine cukup memanggil {@code hewan.gunakanSkill(target)};
 *       perilaku sebenarnya ditentukan child class (DPS / Support / Tank). Hook pasif
 *       ({@link #bonusCritPasif()}, {@link #modifierDamageDiterima()}, dst.) juga di-override.</li>
 * </ul>
 */
public abstract class Hewan implements IPetarung {

    public static final int CRIT_DASAR = 5;
    public static final int REGEN_MANA_PERSEN = 8;

    private final String id;
    private final String nama;
    private final String deskripsi;
    private final Skill skill;

    private Tipe tipe;
    private Elemen elemen;
    private int level;

    private int hp;
    private int maxHp;
    private int mana;
    private int maxMana;
    private int attack;
    private int defense;
    private int speed;
    private int evasion;

    private int shield;
    private boolean isDefending;
    private final List<Buff> buffs = new ArrayList<>();

    protected Hewan(String id, String nama, Tipe tipe, Elemen elemen, int level,
                    Stat stat, Skill skill, String deskripsi) {
        this.id = Objects.requireNonNull(id, "id");
        this.nama = Objects.requireNonNull(nama, "nama");
        this.tipe = Objects.requireNonNull(tipe, "tipe");
        this.elemen = Objects.requireNonNull(elemen, "elemen");
        this.skill = Objects.requireNonNull(skill, "skill");
        Objects.requireNonNull(stat, "stat");
        if (level < 1) throw new IllegalArgumentException("Level minimal 1");
        this.level = level;
        this.deskripsi = deskripsi == null ? "" : deskripsi;

        this.maxHp = stat.getMaxHp();
        this.hp = maxHp;
        this.maxMana = stat.getMaxMana();
        this.mana = maxMana;
        this.attack = stat.getAttack();
        this.defense = stat.getDefense();
        this.speed = stat.getSpeed();
        this.evasion = stat.getEvasion();
    }

    /** Dipanggil konstruktor child class: mode skill harus cocok dengan role kelasnya. */
    protected final void validasiRole(Role roleKelas) {
        if (skill.getMode().getRole() != roleKelas) {
            throw new IllegalArgumentException("Mode skill " + skill.getMode()
                    + " tidak valid untuk role " + roleKelas.getLabel());
        }
    }

    // =================================================================== abstract

    /** Role hewan (ditentukan oleh child class). */
    public abstract Role getRole();

    /** Skill khas role. Inti Polymorphism: setiap child class punya implementasi berbeda. */
    public abstract HasilAksi gunakanSkill(Hewan target);

    /** Penjelasan kemampuan pasif role (untuk layar info). */
    public abstract String getDeskripsiPasif();

    // =================================================================== hook pasif (boleh di-override)

    protected int bonusCritPasif() {
        return 0;
    }

    public double modifierDamageDiberikan() {
        return 1.0;
    }

    public double modifierDamageDiterima() {
        return 1.0;
    }

    public int hitungRegenMana() {
        return Math.max(3, maxMana * REGEN_MANA_PERSEN / 100);
    }

    // =================================================================== aksi

    /** Serangan biasa: elemen Normal, power 1.0, tanpa biaya mana. Sama untuk semua role. */
    public final HasilAksi seranganBiasa(Hewan target) {
        return HasilAksi.dariSerangan(DamageCalculator.hitung(this, target, Serangan.biasa()));
    }

    /** @return null bila skill bisa dipakai, atau alasan bila tidak. */
    public String cekSkill() {
        if (isFainted()) return nama + " sedang pingsan.";
        if (isTaunted()) return nama + " sedang terprovokasi (TAUNT) dan tidak bisa memakai skill.";
        if (mana < skill.getManaCost()) {
            return "MP tidak cukup (" + mana + "/" + skill.getManaCost() + ").";
        }
        return null;
    }

    public boolean bisaPakaiSkill() {
        return cekSkill() == null;
    }

    /** Dipakai child class setelah validasi lolos. */
    protected final void bayarMana() {
        mana = Math.max(0, mana - skill.getManaCost());
    }

    // =================================================================== IPetarung

    @Override
    public int terimaDamage(int damage) {
        if (damage <= 0 || isFainted()) return 0;
        int serap = Math.min(shield, damage);
        shield -= serap;
        int hilang = Math.min(hp, damage - serap);
        hp -= hilang;
        if (hp <= 0) {
            hp = 0;
            onPingsan();
        }
        return hilang;
    }

    @Override
    public int pulihkanHp(int jumlah) {
        if (jumlah <= 0 || isFainted()) return 0;
        int naik = Math.min(jumlah, maxHp - hp);
        hp += naik;
        return naik;
    }

    @Override
    public int pulihkanMana(int jumlah) {
        if (jumlah <= 0 || isFainted()) return 0;
        int naik = Math.min(jumlah, maxMana - mana);
        mana += naik;
        return naik;
    }

    @Override
    public boolean isFainted() {
        return hp <= 0;
    }

    /** Status bersih saat pingsan. */
    private void onPingsan() {
        shield = 0;
        isDefending = false;
        buffs.clear();
    }

    /**
     * Membangkitkan hewan yang pingsan.
     *
     * @return HP setelah bangkit, atau 0 bila hewan tidak sedang pingsan.
     */
    public int bangkit(int persenHp) {
        if (!isFainted()) return 0;
        hp = Math.max(1, maxHp * persenHp / 100);
        mana = Math.max(mana, maxMana / 2);
        return hp;
    }

    // =================================================================== defend & shield

    public boolean isDefending() {
        return isDefending;
    }

    public void setDefending(boolean defending) {
        this.isDefending = defending && !isFainted();
    }

    public int getShield() {
        return shield;
    }

    /** Shield tidak menumpuk: diambil nilai terbesar antara shield lama dan baru. */
    public int pasangShield(int jumlah) {
        shield = Math.max(shield, Math.max(0, jumlah));
        return shield;
    }

    // =================================================================== buff

    /** Buff dari sumber dan stat yang sama akan di-refresh (tidak menumpuk). */
    public void tambahBuff(Buff buff) {
        if (isFainted()) return;
        buffs.removeIf(b -> b.getStat() == buff.getStat() && b.getSumber().equals(buff.getSumber()));
        buffs.add(buff);
    }

    /** @return jumlah debuff yang dihapus. */
    public int hapusDebuff() {
        int sebelum = buffs.size();
        buffs.removeIf(Buff::isDebuff);
        return sebelum - buffs.size();
    }

    /** Kurangi durasi semua buff; dipanggil di akhir giliran pemilik. @return buff yang habis. */
    public List<Buff> tickBuff() {
        List<Buff> habis = new ArrayList<>();
        Iterator<Buff> it = buffs.iterator();
        while (it.hasNext()) {
            Buff b = it.next();
            b.kurangiDurasi();
            if (b.isHabis()) {
                habis.add(b);
                it.remove();
            }
        }
        return habis;
    }

    public boolean isTaunted() {
        for (Buff b : buffs) {
            if (b.getStat() == StatType.TAUNT) return true;
        }
        return false;
    }

    public List<Buff> getBuffs() {
        return Collections.unmodifiableList(buffs);
    }

    private int totalBuff(StatType stat) {
        int total = 0;
        for (Buff b : buffs) {
            if (b.getStat() == stat) total += b.getNilai();
        }
        return total;
    }

    // =================================================================== stat efektif (dasar + buff)

    public int getAttackEfektif() {
        return Math.max(1, attack * (100 + totalBuff(StatType.ATTACK)) / 100);
    }

    public int getDefenseEfektif() {
        return Math.max(0, defense * (100 + totalBuff(StatType.DEFENSE)) / 100);
    }

    public int getSpeedEfektif() {
        return Math.max(1, speed * (100 + totalBuff(StatType.SPEED)) / 100);
    }

    public int getEvasionEfektif() {
        return Math.max(0, evasion + totalBuff(StatType.EVASION));
    }

    public int getCritChance() {
        return Math.max(0, CRIT_DASAR + bonusCritPasif() + totalBuff(StatType.CRIT));
    }

    // =================================================================== evolusi (dipakai ItemEvolusi)

    public void ubahElemen(Elemen baru) {
        this.elemen = Objects.requireNonNull(baru);
    }

    public void ubahTipe(Tipe baru) {
        this.tipe = Objects.requireNonNull(baru);
    }

    public void naikLevel(int jumlah) {
        if (jumlah > 0) level += jumlah;
    }

    /** Menaikkan maxHp; HP saat ini ikut naik sebesar bonus (bila tidak pingsan). */
    public void tambahMaxHp(int jumlah) {
        if (jumlah <= 0) return;
        maxHp += jumlah;
        if (!isFainted()) hp += jumlah;
    }

    public void tambahAttack(int jumlah) {
        if (jumlah > 0) attack += jumlah;
    }

    public void tambahDefense(int jumlah) {
        if (jumlah > 0) defense += jumlah;
    }

    public void tambahSpeed(int jumlah) {
        if (jumlah > 0) speed += jumlah;
    }

    // =================================================================== getter

    public String getId()        { return id; }
    public String getNama()      { return nama; }
    public String getDeskripsi() { return deskripsi; }
    public Skill getSkill()      { return skill; }
    public Tipe getTipe()        { return tipe; }
    public Elemen getElemen()    { return elemen; }
    public int getLevel()        { return level; }
    public int getHp()           { return hp; }
    public int getMaxHp()        { return maxHp; }
    public int getMana()         { return mana; }
    public int getMaxMana()      { return maxMana; }
    public int getAttack()       { return attack; }
    public int getDefense()      { return defense; }
    public int getSpeed()        { return speed; }
    public int getEvasion()      { return evasion; }

    @Override
    public String toString() {
        return nama + " [" + getRole().getLabel() + " " + tipe.getLabel() + "/" + elemen.getLabel()
                + " Lv" + level + " HP " + hp + "/" + maxHp + "]";
    }
}

package animon.battle;

import animon.enums.TargetSkill;
import animon.model.Buff;
import animon.model.Hewan;
import animon.model.Trainer;
import animon.util.Acak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Mesin pertarungan 1v1 (tanpa I/O). UI memanggil method aksi dan menampilkan {@link HasilAksi}.
 *
 * <h3>Alur satu giliran</h3>
 * <ol>
 *   <li>{@link #mulaiGiliran()}: status Defend dilepas, antrian Delayed Buff diproses,
 *       hewan aktif regenerasi mana.</li>
 *   <li>Bila hewan aktif pingsan: {@link #gantiPaksa(int)} (gratis, tidak memakan giliran).</li>
 *   <li>Satu aksi: {@link #serang()}, {@link #skill(Hewan)}, {@link #ganti(int)},
 *       {@link #pakaiItem(int, Hewan)}, atau {@link #defend()}.
 *       Bila aksi mengenai kelemahan / crit, {@link #ambilOneMore(HasilAksi)} memberi 1 aksi tambahan
 *       (maks. sekali per giliran, ala Persona).</li>
 *   <li>{@link #akhiriGiliran()}: durasi buff tim pemain berkurang, giliran pindah.</li>
 * </ol>
 *
 * <h3>Sistem Delayed Buff</h3>
 * Efek dengan {@code delayTurn = n} masuk {@link #antrian}. Di awal setiap giliran pemilik target,
 * delay berkurang 1; saat mencapai 0 buff baru diterapkan. Jadi {@code delayTurn = 1}
 * aktif tepat pada giliran berikutnya milik pemain tersebut.
 */
public final class BattleEngine {

    public static final int GOLD_PER_KILL = 60;

    private final Trainer[] pemain;
    private final int pemainPertama;
    private int giliran;
    private int ronde = 1;
    private boolean rondeBaru;
    private boolean oneMoreTerpakai;
    private Trainer menyerah;

    /** Antrian efek tertunda (FIFO). */
    private final List<EfekTertunda> antrian = new ArrayList<>();

    public BattleEngine(Trainer p1, Trainer p2) {
        Objects.requireNonNull(p1, "pemain 1");
        Objects.requireNonNull(p2, "pemain 2");
        if (!p1.masihPunyaHewan() || !p2.masihPunyaHewan()) {
            throw new IllegalStateException("Kedua trainer wajib punya minimal 1 hewan");
        }
        this.pemain = new Trainer[] {p1, p2};
        this.pemainPertama = tentukanPemainPertama(p1, p2);
        this.giliran = pemainPertama;
    }

    /** Hewan pembuka yang lebih cepat jalan duluan; bila sama, diundi. */
    private static int tentukanPemainPertama(Trainer p1, Trainer p2) {
        int s1 = p1.getHewanAktif().getSpeedEfektif();
        int s2 = p2.getHewanAktif().getSpeedEfektif();
        if (s1 != s2) return s1 > s2 ? 0 : 1;
        return Acak.peluang(50) ? 0 : 1;
    }

    // =================================================================== giliran

    public List<String> mulaiGiliran() {
        List<String> log = new ArrayList<>();
        Trainer t = getPemainAktif();
        oneMoreTerpakai = false;

        for (Hewan h : t.getTim()) h.setDefending(false);

        // Proses antrian Delayed Buff milik tim pemain ini.
        Iterator<EfekTertunda> it = antrian.iterator();
        while (it.hasNext()) {
            EfekTertunda e = it.next();
            if (!t.getTim().contains(e.getTarget())) continue;
            e.kurangiDelay();
            if (!e.isSiap()) continue;
            it.remove();
            Hewan target = e.getTarget();
            if (target.isFainted()) {
                log.add("[Delayed] Efek " + e.getBuff().getSumber() + " hilang karena " + target.getNama() + " pingsan.");
            } else {
                target.tambahBuff(e.getBuff());
                log.add("[Delayed] Efek " + e.getBuff().getSumber() + " kini aktif pada " + target.getNama()
                        + ": " + e.getBuff().getLabel() + "!");
            }
        }

        Hewan aktif = t.getHewanAktif();
        if (!aktif.isFainted()) {
            int mp = aktif.pulihkanMana(aktif.hitungRegenMana());
            if (mp > 0) log.add(aktif.getNama() + " memulihkan " + mp + " MP. (MP " + aktif.getMana() + "/" + aktif.getMaxMana() + ")");
        }
        return log;
    }

    public List<String> akhiriGiliran() {
        List<String> log = new ArrayList<>();
        for (Hewan h : getPemainAktif().getTim()) {
            for (Buff b : h.tickBuff()) {
                log.add("Efek " + b.getSumber() + " (" + b.getStat().getLabel() + ") pada " + h.getNama() + " berakhir.");
            }
        }
        giliran = 1 - giliran;
        rondeBaru = giliran == pemainPertama;
        if (rondeBaru) ronde++;
        return log;
    }

    public boolean perluGantiPaksa() {
        Trainer t = getPemainAktif();
        return t.getHewanAktif().isFainted() && t.masihPunyaHewan();
    }

    /** Mengganti hewan yang pingsan. Gratis (tidak memakan giliran). */
    public HasilAksi gantiPaksa(int index) {
        if (!perluGantiPaksa()) return HasilAksi.gagal("Hewan aktif masih bisa bertarung.");
        return getPemainAktif().switchHewan(index);
    }

    // =================================================================== aksi (memakan giliran bila berhasil)

    /** FIGHT -&gt; Serangan Biasa. */
    public HasilAksi serang() {
        String alasan = cekBisaBertindak();
        if (alasan != null) return HasilAksi.gagal(alasan);
        Hewan musuh = getLawan().getHewanAktif();
        boolean hidup = !musuh.isFainted();
        return selesaikan(getHewanAktif().seranganBiasa(musuh), hidup);
    }

    /**
     * FIGHT -&gt; Skill. Polymorphism: engine tidak tahu role hewan, cukup memanggil
     * {@code gunakanSkill(target)}; target ditentukan dari {@link TargetSkill}.
     *
     * @param kawan target bila skill bertipe {@link TargetSkill#KAWAN} (abaikan untuk tipe lain).
     */
    public HasilAksi skill(Hewan kawan) {
        String alasan = cekBisaBertindak();
        if (alasan != null) return HasilAksi.gagal(alasan);
        Hewan aktif = getHewanAktif();
        Hewan musuh = getLawan().getHewanAktif();
        boolean hidup = !musuh.isFainted();

        Hewan target;
        switch (aktif.getSkill().getTarget()) {
            case KAWAN:
                if (kawan == null || !getPemainAktif().getTim().contains(kawan)) {
                    return HasilAksi.gagal("Pilih kawan dari tim sendiri.");
                }
                target = kawan;
                break;
            case DIRI:
                target = aktif;
                break;
            default:
                target = musuh;
                break;
        }
        return selesaikan(aktif.gunakanSkill(target), hidup);
    }

    /** DEFEND: damage yang diterima dikurangi sampai giliran pemain berikutnya. */
    public HasilAksi defend() {
        String alasan = cekBisaBertindak();
        if (alasan != null) return HasilAksi.gagal(alasan);
        Hewan aktif = getHewanAktif();
        aktif.setDefending(true);
        return HasilAksi.baru().log(aktif.getNama() + " mengambil posisi bertahan! (damage diterima -"
                + Math.round((1 - DamageCalculator.MULT_DEFEND) * 100) + "%)");
    }

    /** SWITCH: menukar hewan aktif (memakan giliran). */
    public HasilAksi ganti(int index) {
        if (perluGantiPaksa()) return gantiPaksa(index);
        return getPemainAktif().switchHewan(index);
    }

    /** ITEM: memakai item dari inventory pada hewan di tim sendiri. */
    public HasilAksi pakaiItem(int slot, Hewan target) {
        if (perluGantiPaksa()) return HasilAksi.gagal("Kirim hewan pengganti terlebih dahulu.");
        Hewan musuh = getLawan().getHewanAktif();
        return selesaikan(getPemainAktif().gunakanItem(slot, target), !musuh.isFainted());
    }

    /** Pemain aktif menyerah; lawan langsung menang. */
    public void menyerah() {
        menyerah = getPemainAktif();
    }

    private String cekBisaBertindak() {
        if (isSelesai()) return "Pertarungan sudah selesai.";
        if (getHewanAktif().isFainted()) return "Kirim hewan pengganti terlebih dahulu.";
        return null;
    }

    /** Pasca-aksi: masukkan efek tertunda ke antrian dan catat Kill Reward. */
    private HasilAksi selesaikan(HasilAksi hasil, boolean musuhHidupSebelum) {
        if (!hasil.isBerhasil()) return hasil;
        antrian.addAll(hasil.getTertunda());

        Hewan musuh = getLawan().getHewanAktif();
        if (musuhHidupSebelum && musuh.isFainted()) {
            Trainer t = getPemainAktif();
            hasil.tambahKorban(musuh);
            t.catatKill();
            t.tambahGold(GOLD_PER_KILL);
            antrian.removeIf(e -> e.getTarget() == musuh);
            hasil.log(t.getNama() + " mendapat " + GOLD_PER_KILL + " gold! (Gold: " + t.getGold() + ")");
        }
        return hasil;
    }

    /**
     * Status "ONE MORE": bila aksi mengenai kelemahan elemen atau critical, pemain mendapat
     * satu aksi tambahan. Hanya sekali per giliran agar tidak berantai tanpa akhir.
     *
     * @return true bila pemain boleh beraksi lagi (sekaligus menandai One More terpakai).
     */
    public boolean ambilOneMore(HasilAksi hasil) {
        if (hasil == null || !hasil.isBerhasil() || !hasil.isOneMore() || oneMoreTerpakai || isSelesai()) return false;
        oneMoreTerpakai = true;
        return true;
    }

    // =================================================================== status

    public boolean isSelesai() {
        return menyerah != null || !pemain[0].masihPunyaHewan() || !pemain[1].masihPunyaHewan();
    }

    /** @return pemenang, atau null bila belum selesai. */
    public Trainer getPemenang() {
        if (menyerah != null) return menyerah == pemain[0] ? pemain[1] : pemain[0];
        if (!pemain[0].masihPunyaHewan()) return pemain[1];
        if (!pemain[1].masihPunyaHewan()) return pemain[0];
        return null;
    }

    public boolean isMenyerah()       { return menyerah != null; }
    public Trainer getPemainAktif()   { return pemain[giliran]; }
    public Trainer getLawan()         { return pemain[1 - giliran]; }
    public Trainer getPemain(int i)   { return pemain[i]; }
    public Hewan getHewanAktif()      { return getPemainAktif().getHewanAktif(); }
    public int getRonde()             { return ronde; }
    /** True tepat setelah {@link #akhiriGiliran()} menutup sebuah ronde (kedua pemain sudah jalan). */
    public boolean isRondeBaru()      { return rondeBaru; }
    public boolean isOneMoreTerpakai(){ return oneMoreTerpakai; }

    public List<EfekTertunda> getAntrian() {
        return Collections.unmodifiableList(antrian);
    }

    /** Efek tertunda yang menunggu untuk tim trainer tertentu. */
    public List<EfekTertunda> getAntrianUntuk(Trainer t) {
        List<EfekTertunda> hasil = new ArrayList<>();
        for (EfekTertunda e : antrian) if (t.getTim().contains(e.getTarget())) hasil.add(e);
        return hasil;
    }
}

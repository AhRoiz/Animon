package animon.gui;

import animon.battle.BattleEngine;
import animon.battle.DamageCalculator;
import animon.battle.ElemenChart;
import animon.battle.HasilAksi;
import animon.battle.TypeChart;
import animon.data.GameSeeder;
import animon.enums.TargetSkill;
import animon.game.Permainan;
import animon.gui.komponen.DialogPixel;
import animon.gui.panel.BattlePanel;
import animon.gui.riwayat.LangkahReplay;
import animon.gui.riwayat.RekamanPertandingan;
import animon.gui.riwayat.RiwayatRepository;
import animon.gui.riwayat.SimpananRepository;
import animon.gui.riwayat.SimpananRepository.SesiTersimpan;
import animon.item.Item;
import animon.model.Buff;
import animon.model.Hewan;
import animon.model.Trainer;
import animon.sistem.Gacha;
import animon.sistem.Shop;
import animon.util.Acak;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Controller utama GUI yang menghubungkan seluruh panel Swing ke {@link BattleEngine}, {@link Gacha}, dan {@link Shop}.
 * Seluruh perhitungan pertarungan tetap didelegasikan ke kelas inti tanpa mengubah logika bisnis.
 */
public final class GameManager {

    private static final DateTimeFormatter FORMAT_WAKTU = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final GameSeeder seeder;
    private final Gacha gacha;
    private final Shop shop;
    private final RiwayatRepository riwayatRepo;
    private final SimpananRepository simpananRepo;

    private MainFrame frame;
    private HistoryFrame jendelaRiwayat;

    private BattleEngine engine;
    private final List<String> logGiliran = new ArrayList<>();
    private final List<String> logPublikGiliran = new ArrayList<>();
    private List<String> logLawan = new ArrayList<>();
    private final List<LangkahReplay> langkahTerekam = new ArrayList<>();

    public GameManager(GameSeeder seeder, Path folderData) {
        this.seeder = Objects.requireNonNull(seeder, "seeder");
        Objects.requireNonNull(folderData, "folderData");
        this.gacha = new Gacha(seeder.getSemuaItem());
        this.shop = new Shop(seeder.getSemuaItem());
        this.riwayatRepo = new RiwayatRepository(folderData);
        this.simpananRepo = new SimpananRepository(folderData);
    }

    public void pasangFrame(MainFrame mainFrame) {
        this.frame = Objects.requireNonNull(mainFrame);
    }

    public int getJumlahKatalogHewan() {
        return seeder.getJumlahHewan();
    }

    public int getJumlahKatalogItem() {
        return seeder.getSemuaItem().size();
    }

    public String getRingkasanSimpanan() {
        return simpananRepo.ringkasanSimpanan();
    }

    public boolean isPertarunganAktif() {
        return engine != null && !engine.isSelesai();
    }

    /** Titik masuk event tombol GUI sesuai kontrak arsitektur. */
    public void prosesAksi(String aksi) {
        if (aksi == null || frame == null) return;
        switch (aksi) {
            case "NEW_GAME":
                frame.bukaSetup();
                break;
            case "LOAD_GAME":
                muatPermainanTersimpan();
                break;
            case "HISTORY":
                bukaRiwayat();
                break;
            case "EXIT":
                keluarAplikasi();
                break;
            case "KEMBALI_MENU":
                konfirmasiKeMenuUtama();
                break;
            case "FIGHT":
                if (pastikanSiapAksi()) frame.getBattlePanel().tampilkanMenuFight(engine);
                break;
            case "SERANG_BIASA":
                eksekusiSeranganBiasa();
                break;
            case "GUNAKAN_SKILL":
                siapkanSkill();
                break;
            case "SWITCH":
                bukaMenuSwitch();
                break;
            case "ITEM":
                bukaMenuItem();
                break;
            case "DEFEND":
                eksekusiDefend();
                break;
            case "INFO":
                tampilkanInfoPertarungan();
                break;
            case "SIMPAN":
                simpanManual();
                break;
            case "MENYERAH":
                konfirmasiMenyerah();
                break;
            default:
                break;
        }
    }

    public List<List<Hewan>> buatPasanganTimAcak(int ukuran) {
        List<Hewan> kolam = seeder.buatSemuaHewan();
        Collections.shuffle(kolam, Acak.get());
        int n = Math.max(1, Math.min(ukuran, kolam.size() / 2));
        List<Hewan> tim1 = new ArrayList<>();
        List<Hewan> tim2 = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            tim1.add(kolam.get(i * 2));
            tim2.add(kolam.get(i * 2 + 1));
        }
        return List.of(tim1, tim2);
    }

    public void mulaiDraftAwal(String nama1, String nama2,
                               List<Hewan> tim1, int pembuka1,
                               List<Hewan> tim2, int pembuka2) {
        Trainer p1 = new Trainer(nama1, Permainan.GOLD_AWAL);
        for (Hewan h : tim1) p1.tambahHewan(h);
        if (pembuka1 > 0 && pembuka1 < tim1.size()) p1.switchHewan(pembuka1);

        Trainer p2 = new Trainer(nama2, Permainan.GOLD_AWAL);
        for (Hewan h : tim2) p2.tambahHewan(h);
        if (pembuka2 > 0 && pembuka2 < tim2.size()) p2.switchHewan(pembuka2);

        frame.tampilkanTransisi(
                "PERSIAPAN " + p1.getNama().toUpperCase(),
                "Serahkan perangkat kepada " + p1.getNama() + ". " + p2.getNama() + " jangan mengintip.",
                "Fase: Draft Item Awal (4 opsi pilih 2)",
                () -> frame.bukaDraft("DRAFT ITEM AWAL", p1,
                        gacha.acakOpsi(Permainan.OPSI_GACHA), Permainan.PILIH_DRAFT_ITEM,
                        () -> lanjutKeDraftKedua(p1, p2)));
    }

    private void lanjutKeDraftKedua(Trainer p1, Trainer p2) {
        frame.tampilkanTransisi(
                "PERSIAPAN " + p1.getNama().toUpperCase() + " SELESAI.",
                "Serahkan perangkat kepada " + p2.getNama() + ". " + p1.getNama() + " jangan mengintip.",
                "Fase: Draft Item Awal (4 opsi pilih 2)",
                () -> frame.bukaDraft("DRAFT ITEM AWAL", p2,
                        gacha.acakOpsi(Permainan.OPSI_GACHA), Permainan.PILIH_DRAFT_ITEM,
                        () -> mulaiPertarungan(p1, p2)));
    }

    private void mulaiPertarungan(Trainer p1, Trainer p2) {
        this.engine = new BattleEngine(p1, p2);
        this.logGiliran.clear();
        this.logPublikGiliran.clear();
        this.logLawan = new ArrayList<>();
        this.langkahTerekam.clear();

        Trainer pertama = engine.getPemainAktif();
        frame.tampilkanTransisi(
                "PERSIAPAN SELESAI!",
                "Serahkan perangkat kepada " + pertama.getNama() + " untuk memulai ronde pertama.",
                pertama.getNama() + " bergerak lebih dulu (" + pertama.getHewanAktif().getNama() + " unggul Speed).",
                this::bukaGiliranBaru);
    }

    private void muatPermainanTersimpan() {
        if (!simpananRepo.adaSimpanan()) {
            DialogPixel.info(frame, "BELUM ADA SIMPANAN",
                    "Belum ada pertarungan yang tersimpan.\n\n"
                            + "Mulai laga lewat NEW GAME. Progres tersimpan otomatis di awal tiap giliran "
                            + "atau kapan saja lewat tombol SIMPAN di layar Battle.");
            return;
        }
        try {
            SesiTersimpan sesi = simpananRepo.muat(seeder);
            this.engine = sesi.getEngine();
            this.logLawan = new ArrayList<>(sesi.getLogLawan());
            this.langkahTerekam.clear();
            this.langkahTerekam.addAll(sesi.getLangkahTerekam());

            Trainer aktif = engine.getPemainAktif();
            frame.tampilkanTransisi(
                    "SESI PERTANDINGAN DIMUAT",
                    "Serahkan perangkat kepada " + aktif.getNama() + ".",
                    "Ronde " + engine.getRonde() + " · Disimpan pada " + sesi.getWaktuSimpan(),
                    this::bukaGiliranBaru);
        } catch (IOException | RuntimeException ex) {
            DialogPixel.info(frame, "GAGAL MEMUAT SIMPANAN",
                    "Berkas simpanan tidak dapat dibaca: " + ex.getMessage()
                            + "\nMulai NEW GAME untuk membuat sesi baru.");
        }
    }

    private void bukaGiliranBaru() {
        logGiliran.clear();
        logPublikGiliran.clear();

        Trainer aktif = engine.getPemainAktif();
        Trainer lawan = engine.getLawan();
        if (!logLawan.isEmpty()) {
            logGiliran.add("--- Giliran " + lawan.getNama() + " ---");
            logGiliran.addAll(logLawan);
        }
        logGiliran.add("--- Giliran " + aktif.getNama() + " (Ronde " + engine.getRonde() + ") ---");

        simpanOtomatis();

        List<String> awal = engine.mulaiGiliran();
        logGiliran.addAll(awal);
        logPublikGiliran.addAll(awal);

        BattlePanel bp = frame.getBattlePanel();
        frame.tampilkanLayar(MainFrame.LAYAR_BATTLE);
        bp.segarkanGiliranBaru(engine, logGiliran);

        if (engine.perluGantiPaksa()) {
            bukaGantiPaksa();
        } else {
            bp.tampilkanMenuUtama();
        }
    }

    private void bukaGantiPaksa() {
        Trainer aktif = engine.getPemainAktif();
        BattlePanel bp = frame.getBattlePanel();
        bp.setPesan(aktif.getHewanAktif().getNama() + " pingsan! Pilih Animon pengganti (gratis).");
        bp.tampilkanPemilihHewan("WAJIB GANTI ANIMON PINGSAN (GRATIS)", aktif, true, idx -> {
            Hewan lama = aktif.getHewanAktif();
            Hewan musuh = engine.getLawan().getHewanAktif();
            int hpMusuh = musuh.getHp();

            HasilAksi h = engine.gantiPaksa(idx);
            if (!h.isBerhasil()) {
                bp.setPesan(h.getAlasanGagal());
                return;
            }
            logGiliran.addAll(h.getLog());
            logPublikGiliran.addAll(h.getLog());

            Hewan baru = aktif.getHewanAktif();
            catatLangkah("GANTI PAKSA -> " + baru.getNama(),
                    aktif, baru, baru.getHp(), baru.getHp(), baru.getMaxHp(),
                    engine.getLawan(), musuh, hpMusuh, musuh.getHp(), musuh.getMaxHp(),
                    h.getLog());

            bp.segarkanSetelahAksi(engine, logGiliran, musuh, hpMusuh, lama);
            bp.setPesan(baru.getNama() + " siap bertarung! Pilih aksi giliranmu.");
            bp.tampilkanMenuUtama();
        });
    }

    private boolean pastikanSiapAksi() {
        if (engine == null || engine.isSelesai()) return false;
        if (engine.perluGantiPaksa()) {
            bukaGantiPaksa();
            return false;
        }
        return true;
    }

    private void eksekusiSeranganBiasa() {
        if (!pastikanSiapAksi()) return;
        Trainer pelaku = engine.getPemainAktif();
        Trainer lawan = engine.getLawan();
        Hewan hPelaku = pelaku.getHewanAktif();
        Hewan hMusuh = lawan.getHewanAktif();
        int hpPelakuAwal = hPelaku.getHp();
        int maxPelakuAwal = hPelaku.getMaxHp();
        int hpMusuhAwal = hMusuh.getHp();
        int maxMusuhAwal = hMusuh.getMaxHp();

        HasilAksi hasil = engine.serang();
        selesaikanAksiBattle("FIGHT: Serangan Biasa", hasil,
                pelaku, hPelaku, hpPelakuAwal, maxPelakuAwal,
                lawan, hMusuh, hpMusuhAwal, maxMusuhAwal);
    }

    private void siapkanSkill() {
        if (!pastikanSiapAksi()) return;
        Hewan aktif = engine.getHewanAktif();
        String alasan = aktif.cekSkill();
        BattlePanel bp = frame.getBattlePanel();
        if (alasan != null) {
            bp.setPesan(alasan);
            return;
        }
        if (aktif.getSkill().getTarget() == TargetSkill.KAWAN) {
            Trainer t = engine.getPemainAktif();
            bp.tampilkanPemilihHewan("PILIH KAWAN UNTUK " + aktif.getSkill().getNama().toUpperCase(),
                    t, false, idx -> eksekusiSkill(t.getTim().get(idx)));
        } else {
            eksekusiSkill(null);
        }
    }

    private void eksekusiSkill(Hewan kawan) {
        Trainer pelaku = engine.getPemainAktif();
        Trainer lawan = engine.getLawan();
        Hewan hPelaku = pelaku.getHewanAktif();
        Hewan targetRekam = kawan != null ? kawan : (hPelaku.getSkill().getTarget() == TargetSkill.DIRI
                ? hPelaku : lawan.getHewanAktif());
        Trainer pemilikTarget = kawan != null || targetRekam == hPelaku ? pelaku : lawan;

        int hpPelakuAwal = hPelaku.getHp();
        int maxPelakuAwal = hPelaku.getMaxHp();
        int hpTargetAwal = targetRekam.getHp();
        int maxTargetAwal = targetRekam.getMaxHp();
        Hewan musuhArena = lawan.getHewanAktif();
        int hpMusuhArenaAwal = musuhArena.getHp();

        String namaSkill = hPelaku.getSkill().getNama();
        HasilAksi hasil = engine.skill(kawan);
        if (!hasil.isBerhasil()) {
            frame.getBattlePanel().setPesan(hasil.getAlasanGagal());
            return;
        }

        catatLangkah("SKILL: " + namaSkill,
                pelaku, hPelaku, hpPelakuAwal, hPelaku.getHp(), maxPelakuAwal,
                pemilikTarget, targetRekam, hpTargetAwal, targetRekam.getHp(), maxTargetAwal,
                hasil.getLog());

        terapkanPascaAksi(hasil, musuhArena, hpMusuhArenaAwal, hPelaku);
    }

    private void bukaMenuSwitch() {
        if (!pastikanSiapAksi()) return;
        Trainer aktif = engine.getPemainAktif();
        BattlePanel bp = frame.getBattlePanel();
        if (aktif.getJumlahHewanHidup() <= 1) {
            bp.setPesan("Tidak ada Animon lain yang masih bisa bertarung.");
            return;
        }
        bp.tampilkanPemilihHewan("SWITCH ANIMON (MEMAKAN GILIRAN)", aktif, false, idx -> {
            Hewan sebelum = aktif.getHewanAktif();
            Hewan musuh = engine.getLawan().getHewanAktif();
            int hpMusuh = musuh.getHp();

            HasilAksi hasil = engine.ganti(idx);
            if (!hasil.isBerhasil()) {
                bp.setPesan(hasil.getAlasanGagal());
                return;
            }
            Hewan baru = aktif.getHewanAktif();
            catatLangkah("SWITCH -> " + baru.getNama(),
                    aktif, baru, baru.getHp(), baru.getHp(), baru.getMaxHp(),
                    engine.getLawan(), musuh, hpMusuh, musuh.getHp(), musuh.getMaxHp(),
                    hasil.getLog());
            terapkanPascaAksi(hasil, musuh, hpMusuh, sebelum);
        });
    }

    private void bukaMenuItem() {
        if (!pastikanSiapAksi()) return;
        Trainer aktif = engine.getPemainAktif();
        BattlePanel bp = frame.getBattlePanel();
        if (aktif.jumlahItem() == 0) {
            bp.setPesan("Tas kosong (0/6). Dapatkan item dari Kill Reward atau Fase Toko.");
            return;
        }
        bp.tampilkanPemilihItem(aktif);
    }

    public void pilihSlotItemBattle(int slot) {
        if (!pastikanSiapAksi()) return;
        Trainer aktif = engine.getPemainAktif();
        Item item = aktif.getItem(slot);
        if (item == null) return;

        if (aktif.getTim().size() == 1) {
            pakaiItemPada(slot, item, aktif.getTim().get(0));
        } else {
            String judul = "TARGET " + item.getNama().toUpperCase()
                    + (item.untukHewanPingsan() ? " (KHUSUS PINGSAN)" : "");
            frame.getBattlePanel().tampilkanPemilihHewan(judul, aktif, false,
                    idx -> pakaiItemPada(slot, item, aktif.getTim().get(idx)));
        }
    }

    private void pakaiItemPada(int slot, Item item, Hewan target) {
        Trainer pelaku = engine.getPemainAktif();
        Hewan hPelakuSebelum = pelaku.getHewanAktif();
        Hewan musuhArena = engine.getLawan().getHewanAktif();
        int hpMusuhAwal = musuhArena.getHp();

        int hpPelakuAwal = hPelakuSebelum.getHp();
        int maxPelakuAwal = hPelakuSebelum.getMaxHp();
        int hpTargetAwal = target.getHp();
        int maxTargetAwal = target.getMaxHp();

        HasilAksi hasil = engine.pakaiItem(slot, target);
        if (!hasil.isBerhasil()) {
            frame.getBattlePanel().setPesan(hasil.getAlasanGagal());
            return;
        }

        catatLangkah("ITEM: " + item.getNama() + " -> " + target.getNama(),
                pelaku, hPelakuSebelum, hpPelakuAwal, hPelakuSebelum.getHp(), maxPelakuAwal,
                pelaku, target, hpTargetAwal, target.getHp(), maxTargetAwal,
                hasil.getLog());

        terapkanPascaAksi(hasil, musuhArena, hpMusuhAwal, hPelakuSebelum);
    }

    private void eksekusiDefend() {
        if (!pastikanSiapAksi()) return;
        Trainer pelaku = engine.getPemainAktif();
        Trainer lawan = engine.getLawan();
        Hewan hPelaku = pelaku.getHewanAktif();
        Hewan hMusuh = lawan.getHewanAktif();

        HasilAksi hasil = engine.defend();
        selesaikanAksiBattle("DEFEND", hasil,
                pelaku, hPelaku, hPelaku.getHp(), hPelaku.getMaxHp(),
                lawan, hMusuh, hMusuh.getHp(), hMusuh.getMaxHp());
    }

    private void selesaikanAksiBattle(String labelAksi, HasilAksi hasil,
                                      Trainer pelaku, Hewan hPelaku, int hpPelakuAwal, int maxPelakuAwal,
                                      Trainer pemilikTarget, Hewan hTarget, int hpTargetAwal, int maxTargetAwal) {
        if (!hasil.isBerhasil()) {
            frame.getBattlePanel().setPesan(hasil.getAlasanGagal());
            return;
        }
        catatLangkah(labelAksi,
                pelaku, hPelaku, hpPelakuAwal, hPelaku.getHp(), maxPelakuAwal,
                pemilikTarget, hTarget, hpTargetAwal, hTarget.getHp(), maxTargetAwal,
                hasil.getLog());

        terapkanPascaAksi(hasil, engine.getLawan().getHewanAktif(), hpTargetAwal, hPelaku);
    }

    private void terapkanPascaAksi(HasilAksi hasil, Hewan musuhSebelum, int hpMusuhSebelum, Hewan pemainSebelum) {
        BattlePanel bp = frame.getBattlePanel();
        logGiliran.addAll(hasil.getLog());
        logPublikGiliran.addAll(hasil.getLog());

        boolean dapatOneMore = !engine.isSelesai() && engine.ambilOneMore(hasil);
        if (dapatOneMore) {
            String pesanOm = "ONE MORE! " + engine.getHewanAktif().getNama() + " mendapat 1 aksi tambahan!";
            logGiliran.add(pesanOm);
            logPublikGiliran.add(pesanOm);
        }

        bp.segarkanSetelahAksi(engine, logGiliran, musuhSebelum, hpMusuhSebelum, pemainSebelum);

        if (engine.isSelesai()) {
            rekamAkhirPertandingan();
            Trainer menang = engine.getPemenang();
            bp.setPesan("Pertarungan berakhir: " + menang.getNama() + " menang!");
            bp.tampilkanPascaAksi(
                    "Semua Animon lawan tumbang! <b>" + menang.getNama() + "</b> memenangkan laga.",
                    "LIHAT HASIL LAGA", "Buka ringkasan akhir pertandingan",
                    this::tampilkanDialogAkhir);
            return;
        }

        int jumlahKorban = hasil.getKorban().size();
        if (jumlahKorban > 0) {
            Hewan korban = hasil.getKorban().get(0);
            bp.setPesan(korban.getNama() + " tumbang! +" + BattleEngine.GOLD_PER_KILL + "G & Kill Reward.");
            bp.tampilkanPascaAksi(
                    korban.getNama() + " tumbang!<br>Klaim 1 item Kill Reward sebelum melanjutkan.",
                    "AMBIL KILL REWARD", "Pilih 1 dari 4 item gacha",
                    () -> bukaKillRewardBerantai(jumlahKorban, dapatOneMore));
            return;
        }

        lanjutSetelahKillReward(dapatOneMore);
    }

    private void bukaKillRewardBerantai(int sisaReward, boolean dapatOneMore) {
        if (sisaReward <= 0) {
            frame.tampilkanLayar(MainFrame.LAYAR_BATTLE);
            frame.getBattlePanel().segarkanGiliranBaru(engine, logGiliran);
            lanjutSetelahKillReward(dapatOneMore);
            return;
        }
        Trainer aktif = engine.getPemainAktif();
        frame.bukaDraft("KILL REWARD", aktif,
                gacha.acakOpsi(Permainan.OPSI_GACHA), Permainan.PILIH_KILL_REWARD,
                () -> bukaKillRewardBerantai(sisaReward - 1, dapatOneMore));
    }

    private void lanjutSetelahKillReward(boolean dapatOneMore) {
        BattlePanel bp = frame.getBattlePanel();
        if (dapatOneMore) {
            bp.setPesan("ONE MORE AKTIF! Pilih 1 aksi tambahan pada giliran ini.");
            bp.tampilkanMenuUtama();
        } else {
            Trainer aktif = engine.getPemainAktif();
            Trainer berikut = engine.getLawan();
            bp.setPesan("Aksi selesai. Periksa log, lalu akhiri giliran.");
            bp.tampilkanPascaAksi(
                    "Aksi giliran <b>" + aktif.getNama() + "</b> selesai.",
                    "SELESAIKAN GILIRAN", "Serahkan ke " + berikut.getNama(),
                    this::tutupGiliranDanTransisi);
        }
    }

    private void tutupGiliranDanTransisi() {
        Trainer selesai = engine.getPemainAktif();
        List<String> akhir = engine.akhiriGiliran();
        logPublikGiliran.addAll(akhir);
        logLawan = new ArrayList<>(logPublikGiliran);

        Trainer berikut = engine.getPemainAktif();
        int rondeSelesai = engine.getRonde() - 1;

        if (engine.isRondeBaru() && rondeSelesai > 0 && rondeSelesai % Permainan.SHOP_SETIAP_RONDE == 0) {
            shop.restok();
            selesai.tambahGold(Permainan.BONUS_GOLD_FASE_TOKO);
            berikut.tambahGold(Permainan.BONUS_GOLD_FASE_TOKO);
            String bonus = "Bonus Fase Toko Ronde " + rondeSelesai + ": +" + Permainan.BONUS_GOLD_FASE_TOKO + "G.";

            frame.bukaToko(selesai, shop, bonus, () ->
                    frame.tampilkanTransisi(
                            "BELANJA " + selesai.getNama().toUpperCase() + " SELESAI.",
                            "Serahkan perangkat kepada " + berikut.getNama() + ". Pemain lain jangan mengintip.",
                            "Fase Toko Ronde " + rondeSelesai + " dibuka sebelum giliran bertarung.",
                            () -> frame.bukaToko(berikut, shop, bonus, this::bukaGiliranBaru)));
        } else {
            frame.tampilkanTransisi(
                    "GILIRAN " + selesai.getNama().toUpperCase() + " SELESAI.",
                    "Serahkan perangkat kepada " + berikut.getNama() + ". Pemain lain jangan mengintip.",
                    "Selanjutnya: Ronde " + engine.getRonde(),
                    this::bukaGiliranBaru);
        }
    }

    public String beliDiToko(Trainer trainer, Shop shopAktif, int index) {
        String alasan = shopAktif.cekBeli(trainer, index);
        if (alasan != null) return alasan;
        Item dibeli = shopAktif.beli(trainer, index);
        return "Berhasil membeli " + dibeli.getNama() + " (-" + dibeli.getHargaBeli() + "G).";
    }

    public String jualDiToko(Trainer trainer, Shop shopAktif, int slot) {
        if (slot < 0 || slot >= trainer.jumlahItem()) return "Pilih slot inventory yang valid.";
        String nama = trainer.getItem(slot).getNama();
        int gold = shopAktif.jual(trainer, slot);
        return "Menjual " + nama + " (+" + gold + "G).";
    }

    private void simpanOtomatis() {
        if (engine == null || engine.isSelesai()) return;
        try {
            simpananRepo.simpan(LocalDateTime.now().format(FORMAT_WAKTU), engine, logLawan, langkahTerekam);
        } catch (IOException ignored) {
        }
    }

    private void simpanManual() {
        if (engine == null || engine.isSelesai()) return;
        try {
            String waktu = LocalDateTime.now().format(FORMAT_WAKTU);
            simpananRepo.simpan(waktu, engine, logLawan, langkahTerekam);
            frame.getBattlePanel().setPesan("Pertarungan disimpan (" + waktu + "). Dapat dilanjutkan via LOAD GAME.");
        } catch (IOException ex) {
            DialogPixel.info(frame, "GAGAL MENYIMPAN", "Tidak dapat menulis berkas simpanan: " + ex.getMessage());
        }
    }

    private void konfirmasiMenyerah() {
        if (engine == null || engine.isSelesai()) return;
        Trainer aktif = engine.getPemainAktif();
        if (!DialogPixel.konfirmasi(frame, "KONFIRMASI MENYERAH",
                aktif.getNama() + ", yakin ingin menyerah? Lawan langsung dinyatakan menang.")) {
            return;
        }
        Hewan hAktif = aktif.getHewanAktif();
        Hewan hMusuh = engine.getLawan().getHewanAktif();
        engine.menyerah();
        List<String> baris = List.of(aktif.getNama() + " menyerah dari pertandingan!");
        catatLangkah("MENYERAH",
                aktif, hAktif, hAktif.getHp(), hAktif.getHp(), hAktif.getMaxHp(),
                engine.getLawan(), hMusuh, hMusuh.getHp(), hMusuh.getHp(), hMusuh.getMaxHp(),
                baris);
        rekamAkhirPertandingan();
        tampilkanDialogAkhir();
    }

    private void catatLangkah(String ringkasan,
                              Trainer pelaku, Hewan hPelaku, int hpPAwal, int hpPAkhir, int maxP,
                              Trainer target, Hewan hTarget, int hpTAwal, int hpTAkhir, int maxT,
                              List<String> log) {
        langkahTerekam.add(new LangkahReplay(
                langkahTerekam.size() + 1,
                engine.getRonde(),
                pelaku.getNama(), hPelaku.getNama(), hpPAwal, hpPAkhir, maxP,
                target.getNama(), hTarget.getNama(), hpTAwal, hpTAkhir, maxT,
                ringkasan, log));
    }

    private void rekamAkhirPertandingan() {
        simpananRepo.hapus();
        Trainer t1 = engine.getPemain(0);
        Trainer t2 = engine.getPemain(1);
        Trainer menang = engine.getPemenang();
        RekamanPertandingan rekaman = new RekamanPertandingan(
                0L,
                LocalDateTime.now().format(FORMAT_WAKTU),
                t1.getNama(),
                t2.getNama(),
                menang != null ? menang.getNama() : "-",
                engine.isMenyerah(),
                engine.getRonde(),
                ringkasTim(t1),
                ringkasTim(t2),
                langkahTerekam);
        riwayatRepo.simpan(rekaman);
    }

    private static String ringkasTim(Trainer t) {
        List<String> nama = new ArrayList<>();
        for (Hewan h : t.getTim()) {
            nama.add(h.getNama() + "(" + h.getHp() + "/" + h.getMaxHp() + ")");
        }
        return String.join(", ", nama);
    }

    private void tampilkanDialogAkhir() {
        Trainer menang = engine.getPemenang();
        Trainer t1 = engine.getPemain(0);
        Trainer t2 = engine.getPemain(1);
        StringBuilder sb = new StringBuilder();
        sb.append("PEMENANG: ").append(menang.getNama().toUpperCase());
        if (engine.isMenyerah()) sb.append(" (lawan menyerah)");
        sb.append("\nTotal ronde: ").append(engine.getRonde())
          .append("  |  Langkah terekam: ").append(langkahTerekam.size()).append("\n\n");

        for (Trainer t : new Trainer[] {t1, t2}) {
            sb.append(t.getNama()).append(t == menang ? " [MENANG]" : "").append('\n');
            sb.append("  Kill: ").append(t.getJumlahKill())
              .append("  |  Animon hidup: ").append(t.getJumlahHewanHidup()).append('/').append(t.getTim().size())
              .append("  |  Gold: ").append(t.getGold()).append("G\n");
            sb.append("  Tim: ").append(ringkasTim(t)).append("\n\n");
        }
        sb.append("Pertandingan telah disimpan ke Riwayat & Replay.");

        int p = DialogPixel.pilih(frame, "PERTANDINGAN SELESAI", sb.toString(),
                "BUKA REPLAY", "MAIN LAGI", "MENU UTAMA");
        engine = null;
        if (p == 0) {
            frame.kembaliKeMenuUtama();
            bukaRiwayat();
        } else if (p == 1) {
            frame.bukaSetup();
        } else {
            frame.kembaliKeMenuUtama();
        }
    }

    private void tampilkanInfoPertarungan() {
        if (engine == null) return;
        Trainer aku = engine.getPemainAktif();
        Trainer lawan = engine.getLawan();
        StringBuilder sb = new StringBuilder();
        sb.append("SIKLUS ELEMEN\n  ").append(ElemenChart.ringkasan()).append("  (Normal netral)\n")
          .append("  Mengenai kelemahan = damage x2 + ONE MORE.\n")
          .append("  Serangan Biasa berelemen Normal; Skill memakai elemen Animon.\n\n");

        sb.append("INTERAKSI TIPE FISIK\n");
        for (String s : TypeChart.ringkasan()) sb.append("  ").append(s).append('\n');

        sb.append("\nATURAN TEMPUR\n")
          .append("  DEFEND: damage diterima x").append(DamageCalculator.MULT_DEFEND).append(" sampai giliranmu berikutnya.\n")
          .append("  CRITICAL: damage x").append(DamageCalculator.MULT_CRIT).append(" + ONE MORE (maks 1x per giliran).\n")
          .append("  KILL: +").append(BattleEngine.GOLD_PER_KILL).append("G + pilih 1 item Kill Reward.\n")
          .append("  TOKO: terbuka tiap ").append(Permainan.SHOP_SETIAP_RONDE)
          .append(" ronde (+").append(Permainan.BONUS_GOLD_FASE_TOKO).append("G).\n\n");

        sb.append("TIM AKTIF: ").append(aku.getNama()).append('\n');
        for (Hewan h : aku.getTim()) {
            sb.append("  - ").append(h.getNama()).append(" Lv").append(h.getLevel())
              .append(" [").append(h.getRole().getLabel()).append(" · ")
              .append(h.getTipe().getLabel()).append('/').append(h.getElemen().getLabel()).append("]")
              .append(h == aku.getHewanAktif() ? " (AKTIF)" : "").append('\n');
            sb.append(String.format("    HP %d/%d  MP %d/%d  ATK %d  DEF %d  SPD %d  EVA %d%%  CRIT %d%%%n",
                    h.getHp(), h.getMaxHp(), h.getMana(), h.getMaxMana(),
                    h.getAttackEfektif(), h.getDefenseEfektif(), h.getSpeedEfektif(),
                    h.getEvasionEfektif(), h.getCritChance()));
            sb.append("    Skill: ").append(h.getSkill().getNama()).append(" (")
              .append(h.getSkill().getManaCost()).append(" MP) - ").append(h.getSkill().getRingkasan()).append('\n');
            sb.append("    Pasif: ").append(h.getDeskripsiPasif()).append('\n');
            if (!h.getBuffs().isEmpty()) {
                List<String> bf = new ArrayList<>();
                for (Buff b : h.getBuffs()) bf.add(b.getLabel());
                sb.append("    Buff: ").append(String.join(", ", bf)).append('\n');
            }
        }

        Hewan musuh = lawan.getHewanAktif();
        sb.append("\nANIMON LAWAN DI ARENA (").append(lawan.getNama()).append(")\n");
        sb.append("  ").append(musuh.getNama()).append(" Lv").append(musuh.getLevel())
          .append(" [").append(musuh.getRole().getLabel()).append(" · ")
          .append(musuh.getTipe().getLabel()).append('/').append(musuh.getElemen().getLabel()).append("]\n");
        sb.append("  HP ").append(musuh.getHp()).append('/').append(musuh.getMaxHp())
          .append("  |  Pasif: ").append(musuh.getDeskripsiPasif()).append('\n');

        DialogPixel.teksPanjang(frame, "PANDUAN & DETAIL TIM", sb.toString());
    }

    public void bukaRiwayat() {
        if (jendelaRiwayat != null && jendelaRiwayat.isDisplayable()) {
            jendelaRiwayat.muatData();
            jendelaRiwayat.toFront();
            jendelaRiwayat.requestFocus();
            return;
        }
        jendelaRiwayat = new HistoryFrame(frame, riwayatRepo);
        jendelaRiwayat.setVisible(true);
    }

    public void konfirmasiKeMenuUtama() {
        if (isPertarunganAktif()) {
            simpanOtomatis();
            if (!DialogPixel.konfirmasi(frame, "KEMBALI KE MENU UTAMA",
                    "Pertarungan yang sedang berjalan sudah disimpan otomatis.\nKembali ke Menu Utama sekarang?")) {
                return;
            }
            engine = null;
        }
        frame.kembaliKeMenuUtama();
    }

    private void keluarAplikasi() {
        if (DialogPixel.konfirmasi(frame, "KELUAR PERMAINAN", "Tutup aplikasi ANIMON: Elemental Clash?")) {
            if (jendelaRiwayat != null) jendelaRiwayat.dispose();
            frame.dispose();
        }
    }
}

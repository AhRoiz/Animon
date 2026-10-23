package animon.game;

import animon.battle.BattleEngine;
import animon.battle.HasilAksi;
import animon.data.GameSeeder;
import animon.enums.TargetSkill;
import animon.item.Item;
import animon.kontrak.PenanganInventoryPenuh;
import animon.kontrak.PenanganInventoryPenuh.Keputusan;
import animon.model.Hewan;
import animon.model.Trainer;
import animon.sistem.Gacha;
import animon.sistem.Shop;
import animon.ui.Konsol;
import animon.ui.Tampilan;
import animon.util.Acak;
import animon.util.Teks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Pengendali alur permainan (layer UI/flow). Menghubungkan input pemain dengan
 * {@link BattleEngine}, {@link Gacha}, dan {@link Shop}.
 *
 * <pre>
 *  Setup nama -&gt; Draft tim (snake / acak) -&gt; Persiapan privat tiap pemain
 *  (pilih hewan pembuka + Drafting Item 4 pilih 2) -&gt; Battle hotseat
 *  (Kill Reward 4 pilih 1, Fase Toko tiap {@value #SHOP_SETIAP_RONDE} ronde) -&gt; Layar akhir
 * </pre>
 */
public final class Permainan {

    public static final int GOLD_AWAL = 150;
    public static final int SHOP_SETIAP_RONDE = 3;
    public static final int BONUS_GOLD_FASE_TOKO = 40;
    public static final int OPSI_GACHA = 4;
    public static final int PILIH_DRAFT_ITEM = 2;
    public static final int PILIH_KILL_REWARD = 1;
    public static final int UKURAN_TIM_BAWAAN = 3;

    private final Konsol k;
    private final Tampilan ui;
    private final GameSeeder seeder;
    private final Gacha gacha;
    private final Shop shop;

    /** Penangan inventory penuh berbasis konsol (dipakai Trainer.tambahItem). */
    private final PenanganInventoryPenuh penangan = this::putuskanInventoryPenuh;

    public Permainan(Konsol konsol, GameSeeder seeder) {
        this.k = Objects.requireNonNull(konsol);
        this.seeder = Objects.requireNonNull(seeder);
        this.ui = new Tampilan(konsol);
        this.gacha = new Gacha(seeder.getSemuaItem());
        this.shop = new Shop(seeder.getSemuaItem());
    }

    // =================================================================== alur utama

    public void mulai() {
        k.bersihkanLayar();
        ui.logo();
        k.println("  Database dimuat: " + seeder.getJumlahHewan() + " hewan, "
                + seeder.getSemuaItem().size() + " item (dari file JSON).");
        k.println();

        Trainer p1 = new Trainer(k.bacaTeks("  Nama Player 1 [Player 1]: ", "Player 1"), GOLD_AWAL);
        String nama2 = k.bacaTeks("  Nama Player 2 [Player 2]: ", "Player 2");
        if (nama2.equalsIgnoreCase(p1.getNama())) nama2 = nama2 + " (2)";
        Trainer p2 = new Trainer(nama2, GOLD_AWAL);

        int maks = Math.min(Trainer.MAKS_TIM, seeder.getJumlahHewan() / 2);
        int bawaan = Math.min(UKURAN_TIM_BAWAAN, maks);
        int ukuran = k.bacaAngka("  Jumlah hewan per tim (1-" + maks + ") [" + bawaan + "]: ", 1, maks, bawaan);
        k.println("  Mode pemilihan tim:  [1] Draft bergiliran   [2] Acak");
        int mode = k.bacaAngka("  Pilih mode [1]: ", 1, 2, 1);

        if (mode == 1) draftTim(p1, p2, ukuran);
        else acakTim(p1, p2, ukuran);

        persiapan(null, p1);
        persiapan("Persiapan " + p1.getNama() + " selesai.", p2);

        BattleEngine engine = new BattleEngine(p1, p2);
        Trainer pertama = engine.getPemainAktif();
        k.println();
        k.tungguEnter("  Persiapan " + p2.getNama() + " selesai. Tekan Enter untuk memulai pertarungan...");
        transisi(pertama, pertama.getNama() + " jalan duluan (" + pertama.getHewanAktif().getNama()
                + " lebih cepat).");

        loopBattle(engine);
        layarAkhir(engine);
    }

    // =================================================================== setup tim

    /** Snake draft: P1, P2, P2, P1, P1, P2, ... Setiap spesies hanya bisa dipilih sekali. */
    private void draftTim(Trainer p1, Trainer p2, int ukuran) {
        List<Hewan> kolam = seeder.buatSemuaHewan();
        Trainer[] urutan = {p1, p2};
        int total = ukuran * 2;
        String pesan = null;
        for (int i = 0; i < total; i++) {
            int putaran = i / 2;
            int posisi = i % 2;
            Trainer pemilih = urutan[putaran % 2 == 0 ? posisi : 1 - posisi];

            k.bersihkanLayar();
            ui.judul("DRAFT TIM  ·  Pilihan " + (i + 1) + "/" + total);
            k.println("  " + Teks.pad(Teks.tebal(p1.getNama()), 20) + ": " + namaTim(p1));
            k.println("  " + Teks.pad(Teks.tebal(p2.getNama()), 20) + ": " + namaTim(p2));
            ui.garis();
            ui.tabelHewan(kolam);
            ui.garis();
            ui.pesan(pesan);
            int p = k.bacaAngka("  " + Teks.kuning(pemilih.getNama()) + ", pilih hewan (0 = acak): ", 0, kolam.size());
            Hewan h = p == 0 ? Acak.pilih(kolam) : kolam.get(p - 1);
            kolam.remove(h);
            pemilih.tambahHewan(h);
            pesan = pemilih.getNama() + " memilih " + h.getNama() + ".";
        }
    }

    private void acakTim(Trainer p1, Trainer p2, int ukuran) {
        List<Hewan> kolam = seeder.buatSemuaHewan();
        Collections.shuffle(kolam, Acak.get());
        for (int i = 0; i < ukuran; i++) {
            p1.tambahHewan(kolam.get(i * 2));
            p2.tambahHewan(kolam.get(i * 2 + 1));
        }
        k.bersihkanLayar();
        ui.judul("TIM ACAK");
        k.println("  " + Teks.tebal(p1.getNama()) + ": " + namaTim(p1));
        k.println("  " + Teks.tebal(p2.getNama()) + ": " + namaTim(p2));
        k.tungguEnter();
    }

    private static String namaTim(Trainer t) {
        if (t.getTim().isEmpty()) return Teks.abu("-");
        List<String> nama = new ArrayList<>();
        for (Hewan h : t.getTim()) nama.add(h.getNama());
        return String.join(", ", nama);
    }

    /** Layar privat: pilih hewan pembuka lalu Drafting Item (4 opsi, pilih 2). */
    private void persiapan(String selesai, Trainer t) {
        if (selesai != null) k.tungguEnter("  " + selesai + " Tekan Enter untuk lanjut ke " + t.getNama() + "...");
        transisi(t, "Fase persiapan: pilih hewan pembuka dan item awal.");

        if (t.getTim().size() > 1) {
            ui.judul("PERSIAPAN - " + t.getNama());
            ui.daftarTim(t);
            int p = k.bacaAngka("  Pilih hewan pembuka [1]: ", 1, t.getTim().size(), 1);
            if (p - 1 != t.getIndexAktif()) t.switchHewan(p - 1);
        }
        tampilkanOpsiItem(t, OPSI_GACHA, PILIH_DRAFT_ITEM, "DRAFTING ITEM");
    }

    // =================================================================== hotseat

    /** Layar transisi antar giliran: bersihkan layar lalu tunggu pemain berikutnya siap. */
    private void transisi(Trainer berikut, String info) {
        k.bersihkanLayar();
        ui.judul("GILIRAN " + berikut.getNama().toUpperCase());
        if (info != null) k.println("  " + info);
        k.println("  Serahkan perangkat ke " + Teks.kuning(berikut.getNama()) + ". Pemain lain jangan mengintip!");
        k.println();
        k.tungguEnter("  " + berikut.getNama() + ", tekan Enter jika sudah siap...");
        k.bersihkanLayar();
    }

    // =================================================================== battle

    private void loopBattle(BattleEngine e) {
        List<String> logLawan = new ArrayList<>();
        while (true) {
            Trainer t = e.getPemainAktif();
            List<String> log = new ArrayList<>();        // tampil di layar pemain ini
            List<String> logPublik = new ArrayList<>();  // diteruskan ke lawan
            if (!logLawan.isEmpty()) {
                log.add("--- Giliran " + e.getLawan().getNama() + " ---");
                log.addAll(logLawan);
            }
            log.add("--- Giliranmu (Ronde " + e.getRonde() + ") ---");
            List<String> awal = e.mulaiGiliran();
            log.addAll(awal);
            logPublik.addAll(awal);

            if (e.perluGantiPaksa()) {
                HasilAksi h = menuGantiPaksa(e, log);
                log.addAll(h.getLog());
                logPublik.addAll(h.getLog());
            }

            String pesan = null;
            while (true) {
                k.bersihkanLayar();
                ui.layarBattle(e, log, pesan);
                pesan = null;
                HasilAksi h = menuAksi(e);
                if (h == null) continue;
                if (!h.isBerhasil()) {
                    pesan = h.getAlasanGagal();
                    continue;
                }
                log.addAll(h.getLog());
                logPublik.addAll(h.getLog());
                if (e.isSelesai()) break;

                for (Hewan korban : h.getKorban()) {
                    k.bersihkanLayar();
                    ui.layarBattle(e, log, null);
                    k.tungguEnter("  " + Teks.merah(korban.getNama() + " tumbang!")
                            + " Tekan Enter untuk memilih KILL REWARD...");
                    tampilkanOpsiItem(t, OPSI_GACHA, PILIH_KILL_REWARD, "KILL REWARD");
                }
                if (e.ambilOneMore(h)) {
                    String om = "ONE MORE! " + e.getHewanAktif().getNama() + " mendapat satu aksi tambahan!";
                    log.add(om);
                    logPublik.add(om);
                    continue;
                }
                break;
            }
            if (e.isSelesai()) return;

            // Tampilkan hasil akhir giliran dari sudut pandang pemain ini, lalu akhiri giliran.
            k.bersihkanLayar();
            ui.layarBattle(e, log, null);
            List<String> akhir = e.akhiriGiliran();
            for (String s : akhir) k.println("  " + Teks.abu(s));
            logPublik.addAll(akhir);
            logLawan = logPublik;

            Trainer berikut = e.getPemainAktif();
            int rondeSelesai = e.getRonde() - 1;
            if (e.isRondeBaru() && rondeSelesai % SHOP_SETIAP_RONDE == 0) {
                k.tungguEnter("  Giliran " + t.getNama() + " selesai. Ronde " + rondeSelesai
                        + " berakhir - " + Teks.kuning("FASE TOKO") + " dibuka! Tekan Enter...");
                shop.restok();
                faseToko(t);
                k.tungguEnter("  Belanja " + t.getNama() + " selesai. Tekan Enter untuk lanjut ke " + berikut.getNama() + "...");
                transisi(berikut, "FASE TOKO dibuka untukmu sebelum giliran bertarung.");
                faseToko(berikut);
            } else {
                k.tungguEnter("  Giliran " + t.getNama() + " selesai. Tekan Enter untuk lanjut ke " + berikut.getNama() + "...");
                transisi(berikut, null);
            }
        }
    }

    private HasilAksi menuAksi(BattleEngine e) {
        k.println("  " + Teks.tebal("[1] FIGHT") + "   [2] SWITCH   [3] ITEM   [4] DEFEND   " + Teks.abu("[5] INFO   [0] MENYERAH"));
        int p = k.bacaAngka("  Pilih aksi: ", 0, 5);
        switch (p) {
            case 1: return menuFight(e);
            case 2: return menuSwitch(e);
            case 3: return menuItem(e);
            case 4: return e.defend();
            case 5:
                k.bersihkanLayar();
                ui.layarInfo(e);
                k.tungguEnter();
                return null;
            default:
                if (!k.konfirmasi("  Yakin ingin menyerah?")) return null;
                Trainer t = e.getPemainAktif();
                e.menyerah();
                return HasilAksi.baru().log(t.getNama() + " menyerah!");
        }
    }

    private HasilAksi menuFight(BattleEngine e) {
        Trainer t = e.getPemainAktif();
        Hewan a = e.getHewanAktif();
        Hewan musuh = e.getLawan().getHewanAktif();
        String alasan = a.cekSkill();

        ui.subjudul("FIGHT");
        k.println("  [1] Serangan Biasa (0 MP)   " + Tampilan.prakiraan(a, musuh, false));
        String baris = "  [2] " + Teks.tebal(a.getSkill().getNama()) + " (" + a.getSkill().getManaCost() + " MP) - "
                + a.getSkill().getRingkasan();
        k.println(alasan == null ? baris : Teks.abu(Teks.strip(baris)) + " " + Teks.merah("[" + alasan + "]"));
        if (a.getSkill().getTarget() == TargetSkill.MUSUH) {
            k.println("      " + Tampilan.prakiraan(a, musuh, true));
        }
        k.println("  [0] Kembali");
        int p = k.bacaAngka("  Pilih serangan: ", 0, 2);
        if (p == 0) return null;
        if (p == 1) return e.serang();
        if (alasan != null) return HasilAksi.gagal(alasan);

        if (a.getSkill().getTarget() == TargetSkill.KAWAN) {
            ui.subjudul("Pilih kawan untuk " + a.getSkill().getNama());
            ui.daftarTim(t);
            k.println("  [0] Kembali");
            int idx = k.bacaAngka("  Target: ", 0, t.getTim().size());
            if (idx == 0) return null;
            return e.skill(t.getTim().get(idx - 1));
        }
        return e.skill(null);
    }

    private HasilAksi menuSwitch(BattleEngine e) {
        Trainer t = e.getPemainAktif();
        if (t.getJumlahHewanHidup() <= 1) return HasilAksi.gagal("Tidak ada hewan lain yang bisa bertarung.");
        ui.subjudul("SWITCH (memakan giliran)");
        ui.daftarTim(t);
        k.println("  [0] Kembali");
        int p = k.bacaAngka("  Kirim hewan nomor: ", 0, t.getTim().size());
        return p == 0 ? null : e.ganti(p - 1);
    }

    private HasilAksi menuItem(BattleEngine e) {
        Trainer t = e.getPemainAktif();
        if (t.jumlahItem() == 0) return HasilAksi.gagal("Inventory kosong.");
        ui.subjudul("ITEM");
        ui.daftarInventory(t, null);
        k.println("  [0] Kembali");
        int slot = k.bacaAngka("  Gunakan item nomor: ", 0, t.jumlahItem());
        if (slot == 0) return null;
        Item item = t.getItem(slot - 1);

        Hewan target;
        if (t.getTim().size() == 1) {
            target = t.getTim().get(0);
        } else {
            ui.subjudul("Target " + item.getNama() + (item.untukHewanPingsan() ? " (hewan pingsan)" : ""));
            ui.daftarTim(t);
            k.println("  [0] Kembali");
            int idx = k.bacaAngka("  Gunakan pada hewan nomor: ", 0, t.getTim().size());
            if (idx == 0) return null;
            target = t.getTim().get(idx - 1);
        }
        return e.pakaiItem(slot - 1, target);
    }

    private HasilAksi menuGantiPaksa(BattleEngine e, List<String> log) {
        Trainer t = e.getPemainAktif();
        String pesan = null;
        while (true) {
            k.bersihkanLayar();
            ui.layarBattle(e, log, pesan);
            k.println("  " + Teks.merah(t.getHewanAktif().getNama() + " pingsan!") + " Pilih hewan pengganti (gratis):");
            ui.daftarTim(t);
            int p = k.bacaAngka("  Kirim hewan nomor: ", 1, t.getTim().size());
            HasilAksi h = e.gantiPaksa(p - 1);
            if (h.isBerhasil()) return h;
            pesan = h.getAlasanGagal();
        }
    }

    // =================================================================== gacha / drafting

    /**
     * Drafting Item (Gacha): tampilkan {@code jumlahOpsi} item acak, pemain memilih {@code jumlahPilih}.
     * Dipakai di awal game (4 pilih 2) dan sebagai Kill Reward (4 pilih 1).
     */
    public void tampilkanOpsiItem(Trainer t, int jumlahOpsi, int jumlahPilih, String judul) {
        List<Item> opsi = new ArrayList<>(gacha.acakOpsi(jumlahOpsi));
        String pesan = null;
        for (int n = 0; n < jumlahPilih && !opsi.isEmpty(); n++) {
            k.bersihkanLayar();
            ui.judul(judul + " - " + t.getNama());
            k.println("  Pilih " + Teks.kuning(String.valueOf(jumlahPilih - n)) + " item lagi dari " + opsi.size() + " opsi:");
            ui.daftarItem(opsi, null);
            k.println();
            ui.daftarInventory(t, null);
            ui.pesan(pesan);
            int p = k.bacaAngka("  Ambil item nomor: ", 1, opsi.size());
            Item item = opsi.remove(p - 1);
            boolean masuk = t.tambahItem(item, penangan);
            pesan = masuk ? item.getNama() + " masuk ke inventory." : item.getNama() + " tidak diambil.";
        }
        k.println("  " + Teks.hijau(pesan == null ? "" : pesan));
        k.tungguEnter();
    }

    /** Dipanggil Trainer.tambahItem saat inventory penuh: paksa jual / buang, atau tolak. */
    private Keputusan putuskanInventoryPenuh(Trainer t, Item baru) {
        k.println();
        ui.subjudul(Teks.merah("INVENTORY PENUH"));
        k.println("  Item baru: " + ui.barisItem(baru));
        ui.daftarInventory(t, "jual");
        k.println("  [1] Jual salah satu item   [2] Buang salah satu item   [0] Tolak item baru");
        int aksi = k.bacaAngka("  Pilihan: ", 0, 2);
        if (aksi == 0) return Keputusan.tolak();
        int slot = k.bacaAngka("  Nomor item yang di" + (aksi == 1 ? "jual" : "buang") + " (0 = batal, tolak item baru): ",
                0, t.jumlahItem());
        if (slot == 0) return Keputusan.tolak();
        Item lama = t.getItem(slot - 1);
        k.println("  " + (aksi == 1 ? "Menjual " + lama.getNama() + " (+" + lama.getHargaJual() + "G)."
                : "Membuang " + lama.getNama() + "."));
        return aksi == 1 ? Keputusan.jual(slot - 1) : Keputusan.buang(slot - 1);
    }

    // =================================================================== toko

    private void faseToko(Trainer t) {
        t.tambahGold(BONUS_GOLD_FASE_TOKO);
        String pesan = "Bonus fase toko: +" + BONUS_GOLD_FASE_TOKO + "G";
        while (true) {
            k.bersihkanLayar();
            ui.judul("TOKO ANIMON - " + t.getNama());
            ui.subjudul("Stok toko (harga beli)");
            ui.daftarItem(shop.getStok(), "beli");
            ui.subjudul("Inventorymu (harga jual = 50%)");
            ui.daftarInventory(t, "jual");
            ui.garis();
            ui.pesan(pesan);
            pesan = null;
            k.println("  [1] Beli   [2] Jual   [0] Selesai belanja");
            int p = k.bacaAngka("  Pilihan: ", 0, 2);
            if (p == 0) return;
            if (p == 1) {
                int idx = k.bacaAngka("  Beli item stok nomor (0 = batal): ", 0, shop.getStok().size());
                if (idx == 0) continue;
                String alasan = shop.cekBeli(t, idx - 1);
                if (alasan != null) {
                    pesan = alasan;
                } else {
                    Item item = shop.beli(t, idx - 1);
                    pesan = "Membeli " + item.getNama() + " seharga " + item.getHargaBeli() + "G.";
                }
            } else {
                if (t.jumlahItem() == 0) {
                    pesan = "Inventory kosong, tidak ada yang bisa dijual.";
                    continue;
                }
                int slot = k.bacaAngka("  Jual item inventory nomor (0 = batal): ", 0, t.jumlahItem());
                if (slot == 0) continue;
                String nama = t.getItem(slot - 1).getNama();
                int gold = shop.jual(t, slot - 1);
                pesan = "Menjual " + nama + " seharga " + gold + "G.";
            }
        }
    }

    // =================================================================== akhir

    private void layarAkhir(BattleEngine e) {
        Trainer menang = e.getPemenang();
        k.bersihkanLayar();
        ui.judul("PERTARUNGAN SELESAI");
        k.println();
        k.println(Teks.tengah(Teks.kuning(Teks.tebal("*** " + menang.getNama().toUpperCase() + " MENANG! ***")), Tampilan.LEBAR));
        if (e.isMenyerah()) k.println(Teks.tengah(Teks.abu("(lawan menyerah)"), Tampilan.LEBAR));
        k.println(Teks.tengah(Teks.abu("Pertarungan berlangsung " + e.getRonde() + " ronde"), Tampilan.LEBAR));
        k.println();
        for (int i = 0; i < 2; i++) {
            Trainer t = e.getPemain(i);
            ui.subjudul(t.getNama() + (t == menang ? "  (PEMENANG)" : ""));
            k.println("  Kill: " + t.getJumlahKill() + "   Hewan tersisa: " + t.getJumlahHewanHidup() + "/" + t.getTim().size()
                    + "   Gold: " + t.getGold() + "G   Item: " + t.jumlahItem());
            ui.daftarTim(t);
        }
        ui.garis();
        k.tungguEnter("  Terima kasih sudah bermain ANIMON! Tekan Enter untuk keluar...");
    }
}

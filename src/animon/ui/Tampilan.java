package animon.ui;

import animon.battle.BattleEngine;
import animon.battle.EfekTertunda;
import animon.battle.ElemenChart;
import animon.battle.TypeChart;
import animon.enums.Efektivitas;
import animon.item.Item;
import animon.model.Buff;
import animon.model.Hewan;
import animon.model.Trainer;
import animon.util.Teks;

import java.util.ArrayList;
import java.util.List;

/**
 * Renderer layar CLI (hanya menampilkan, tidak membaca input).
 * Karakter grafis dipilih yang ada di code page 437 agar aman di Command Prompt.
 */
public final class Tampilan {

    public static final int LEBAR = 70;
    public static final int MAKS_LOG = 16;

    private final Konsol k;

    public Tampilan(Konsol konsol) {
        this.k = konsol;
    }

    // =================================================================== elemen dasar

    public void judul(String teks) {
        k.println(Teks.kuning("╔" + "═".repeat(LEBAR - 2) + "╗"));
        k.println(Teks.kuning("║") + Teks.tengah(Teks.tebal(teks), LEBAR - 2) + Teks.kuning("║"));
        k.println(Teks.kuning("╚" + "═".repeat(LEBAR - 2) + "╝"));
    }

    public void garis() {
        k.println(Teks.abu("─".repeat(LEBAR)));
    }

    public void subjudul(String teks) {
        String isi = "── " + teks + " ";
        k.println(Teks.cyan(isi + "─".repeat(Math.max(0, LEBAR - Teks.panjang(isi)))));
    }

    public void pesan(String s) {
        if (s != null && !s.isEmpty()) k.println("  " + Teks.kuning("! " + s));
    }

    public void logo() {
        String[] baris = {
            "    _    _   _ ___ __  __  ___  _   _ ",
            "   / \\  | \\ | |_ _|  \\/  |/ _ \\| \\ | |",
            "  / _ \\ |  \\| || || |\\/| | | | |  \\| |",
            " / ___ \\| |\\  || || |  | | |_| | |\\  |",
            "/_/   \\_\\_| \\_|___|_|  |_|\\___/|_| \\_|",
        };
        k.println();
        for (String b : baris) k.println(Teks.tengah(Teks.kuning(b), LEBAR));
        k.println(Teks.tengah(Teks.abu("Turn-based Animal Battle  ·  1v1 Hotseat"), LEBAR));
        k.println();
    }

    // =================================================================== hewan

    public String indikatorTim(Trainer t) {
        StringBuilder sb = new StringBuilder();
        for (Hewan h : t.getTim()) {
            if (h.isFainted()) sb.append(Teks.abu("·"));
            else if (h == t.getHewanAktif()) sb.append(Teks.kuning("■"));
            else sb.append(Teks.hijau("■"));
        }
        return sb.toString();
    }

    public String labelHewan(Hewan h) {
        return Teks.tebal(h.getNama()) + " Lv" + h.getLevel() + " [" + Teks.role(h.getRole()) + "] "
                + h.getTipe().getLabel() + "/" + Teks.elemen(h.getElemen());
    }

    public String statusHewan(Hewan h) {
        if (h.isFainted()) return Teks.merah("PINGSAN");
        List<String> s = new ArrayList<>();
        if (h.isDefending()) s.add(Teks.kuning("BERTAHAN"));
        if (h.getShield() > 0) s.add(Teks.cyan("Shield " + h.getShield()));
        for (Buff b : h.getBuffs()) s.add(b.isDebuff() ? Teks.merah(b.getLabel()) : Teks.hijau(b.getLabel()));
        return s.isEmpty() ? Teks.abu("-") : String.join(" ", s);
    }

    public void kartuHewan(Hewan h, boolean lengkap) {
        k.println("  " + labelHewan(h));
        k.println("  HP " + Teks.barHp(h.getHp(), h.getMaxHp(), 24) + " " + h.getHp() + "/" + h.getMaxHp());
        k.println("  MP " + Teks.barMp(h.getMana(), h.getMaxMana(), 24) + " " + h.getMana() + "/" + h.getMaxMana());
        k.println("  Status: " + statusHewan(h));
        if (lengkap) {
            k.println("  " + Teks.abu(String.format("ATK %d  DEF %d  SPD %d  EVA %d%%  CRIT %d%%",
                    h.getAttackEfektif(), h.getDefenseEfektif(), h.getSpeedEfektif(),
                    h.getEvasionEfektif(), h.getCritChance())));
            k.println("  Skill: " + Teks.tebal(h.getSkill().getNama()) + " (" + h.getSkill().getManaCost() + " MP) - "
                    + h.getSkill().getRingkasan());
            k.println("  Pasif: " + Teks.abu(h.getDeskripsiPasif()));
        }
    }

    /** Tabel ringkas hewan (dipakai saat draft tim). */
    public void tabelHewan(List<Hewan> daftar) {
        k.println(Teks.abu(String.format("  %-3s %-21s %-8s %-15s %4s %4s %4s %4s %4s %4s",
                "No", "Nama", "Role", "Tipe/Elemen", "HP", "MP", "ATK", "DEF", "SPD", "EVA")));
        for (int i = 0; i < daftar.size(); i++) {
            Hewan h = daftar.get(i);
            String te = h.getTipe().getLabel() + "/" + Teks.elemen(h.getElemen());
            k.println(String.format("  %-3s %s %s %s %4d %4d %4d %4d %4d %4d",
                    (i + 1) + ".", Teks.pad(h.getNama(), 21), Teks.pad(Teks.role(h.getRole()), 8), Teks.pad(te, 15),
                    h.getMaxHp(), h.getMaxMana(), h.getAttack(), h.getDefense(), h.getSpeed(), h.getEvasion()));
            k.println(Teks.abu("      Skill: " + h.getSkill().getNama() + " (" + h.getSkill().getManaCost() + " MP) - "
                    + h.getSkill().getRingkasan()));
        }
    }

    /** Daftar tim bernomor (1..n) untuk menu SWITCH / target item. */
    public void daftarTim(Trainer t) {
        List<Hewan> tim = t.getTim();
        for (int i = 0; i < tim.size(); i++) {
            Hewan h = tim.get(i);
            String tanda = h == t.getHewanAktif() && !h.isFainted() ? Teks.kuning(" (AKTIF)") : "";
            k.println(String.format("  [%d] %s%s", i + 1, labelHewan(h), tanda));
            k.println("      HP " + Teks.barHp(h.getHp(), h.getMaxHp(), 14) + " " + Teks.pad(h.getHp() + "/" + h.getMaxHp(), 8)
                    + " MP " + h.getMana() + "/" + h.getMaxMana() + "   " + statusHewan(h));
        }
    }

    // =================================================================== item

    public String barisItem(Item item) {
        return Teks.pad(Teks.tebal(item.getNama()), 15) + " " + Teks.pad(item.getJenis().getLabel(), 8)
                + Teks.pad(Teks.rarity(item.getRarity()), 7) + " " + item.getRingkasan();
    }

    /** Daftar item bernomor dengan kolom harga opsional ("beli" / "jual" / null). */
    public void daftarItem(List<Item> items, String kolomHarga) {
        for (int i = 0; i < items.size(); i++) {
            Item it = items.get(i);
            String harga = "";
            if ("beli".equals(kolomHarga)) harga = Teks.kuning(Teks.padKiri(it.getHargaBeli() + "G", 5)) + " ";
            if ("jual".equals(kolomHarga)) harga = Teks.kuning(Teks.padKiri(it.getHargaJual() + "G", 5)) + " ";
            k.println(String.format("  [%d] %s%s", i + 1, harga, barisItem(it)));
            if (!it.getDeskripsi().isEmpty()) k.println(Teks.abu("        " + it.getDeskripsi()));
        }
    }

    public void daftarInventory(Trainer t, String kolomHarga) {
        List<Item> isi = new ArrayList<>();
        for (int i = 0; i < t.jumlahItem(); i++) isi.add(t.getItem(i));
        k.println("  Inventory " + t.jumlahItem() + "/" + Trainer.MAKS_INVENTORY + "   Gold " + Teks.kuning(t.getGold() + "G"));
        if (isi.isEmpty()) k.println(Teks.abu("  (kosong)"));
        else daftarItem(isi, kolomHarga);
    }

    // =================================================================== layar battle

    public void layarBattle(BattleEngine e, List<String> log, String pesan) {
        Trainer aku = e.getPemainAktif();
        Trainer lawan = e.getLawan();

        judul("ANIMON BATTLE  ·  Ronde " + e.getRonde() + "  ·  Giliran: " + aku.getNama());

        k.println("  " + Teks.merah("LAWAN") + "  " + Teks.pad(Teks.tebal(lawan.getNama()), 40) + "Tim " + indikatorTim(lawan));
        kartuHewan(lawan.getHewanAktif(), false);
        k.println();
        k.println(Teks.tengah(Teks.abu("─── VS ───"), LEBAR));
        k.println();
        k.println("  " + Teks.hijau("KAMU") + "   " + Teks.pad(Teks.tebal(aku.getNama()), 22)
                + Teks.pad("Gold " + Teks.kuning(aku.getGold() + "G"), 12)
                + Teks.pad("Item " + aku.jumlahItem() + "/" + Trainer.MAKS_INVENTORY, 10)
                + "Tim " + indikatorTim(aku));
        kartuHewan(aku.getHewanAktif(), true);
        for (EfekTertunda ef : e.getAntrianUntuk(aku)) {
            k.println("  " + Teks.magenta("Tertunda: " + ef.getBuff().getSumber() + " " + ef.getBuff().getLabel()
                    + " -> " + ef.getTarget().getNama() + " (aktif " + ef.getSisaDelay() + " giliran lagi)"));
        }

        garis();
        List<String> tampil = log.size() > MAKS_LOG ? log.subList(log.size() - MAKS_LOG, log.size()) : log;
        if (tampil.isEmpty()) k.println(Teks.abu("  (belum ada kejadian)"));
        for (String baris : tampil) k.println("  " + warnaiLog(baris));
        garis();
        pesan(pesan);
    }

    /** Memberi warna pada kata kunci log pertarungan. */
    private static String warnaiLog(String b) {
        if (b.startsWith("---")) return Teks.cyan(b);
        if (b.startsWith("WEAK!")) return Teks.merah(Teks.tebal(b));
        if (b.startsWith("CRITICAL")) return Teks.kuning(Teks.tebal(b));
        if (b.startsWith("ONE MORE")) return Teks.magenta(Teks.tebal(b));
        if (b.startsWith("RESIST")) return Teks.abu(b);
        if (b.startsWith("[Delayed]")) return Teks.magenta(b);
        if (b.startsWith("[Tipe]")) return Teks.cyan(b);
        if (b.endsWith("pingsan!")) return Teks.merah(b);
        if (b.startsWith("Serangan meleset")) return Teks.abu(b);
        return b;
    }

    /** Prakiraan efektivitas serangan elemen terhadap hewan lawan (untuk menu FIGHT). */
    public static String prakiraan(Hewan penyerang, Hewan musuh, boolean pakaiElemenHewan) {
        String hasil;
        if (!pakaiElemenHewan) {
            hasil = "Normal -> netral";
        } else {
            Efektivitas ef = ElemenChart.cek(penyerang.getElemen(), musuh.getElemen());
            String label = ef == Efektivitas.LEMAH ? Teks.merah("WEAK x2")
                    : ef == Efektivitas.RESIST ? Teks.abu("RESIST x0.5") : "netral";
            hasil = Teks.elemen(penyerang.getElemen()) + " vs " + Teks.elemen(musuh.getElemen()) + " -> " + label;
        }
        String tipe = TypeChart.cek(penyerang.getTipe(), musuh.getTipe()).ringkasanAngka();
        return tipe.isEmpty() ? hasil : hasil + Teks.abu(" | tipe: " + tipe);
    }

    // =================================================================== info

    public void layarInfo(BattleEngine e) {
        judul("INFO PERTARUNGAN");
        subjudul("Elemen (ala Persona)");
        k.println("  " + ElemenChart.ringkasan() + "   (Normal selalu netral)");
        k.println("  Kena kelemahan = damage x2 + ONE MORE. Elemen sama / dikalahkan = RESIST x0.5.");
        k.println("  Serangan Biasa selalu berelemen Normal; Skill memakai elemen hewan.");
        subjudul("Type Advantage");
        for (String s : TypeChart.ringkasan()) k.println("  " + s);
        subjudul("Mekanik");
        k.println("  DEFEND: damage diterima x0.5 sampai giliranmu berikutnya.");
        k.println("  CRITICAL: damage x1.5 + ONE MORE. ONE MORE maksimal 1x per giliran.");
        k.println("  Regen MP tiap awal giliran (Support lebih cepat). Kill = +"
                + BattleEngine.GOLD_PER_KILL + " gold + pilih 1 item.");
        subjudul("Timmu: " + e.getPemainAktif().getNama());
        for (Hewan h : e.getPemainAktif().getTim()) {
            kartuHewan(h, true);
            k.println();
        }
        subjudul("Hewan lawan di arena");
        kartuHewan(e.getLawan().getHewanAktif(), true);
        garis();
    }
}

package animon.util;

import animon.enums.Elemen;
import animon.enums.Rarity;
import animon.enums.Role;

import java.util.regex.Pattern;

/**
 * Helper pewarnaan (ANSI) dan format teks untuk tampilan CLI.
 * Warna bisa dimatikan global (mis. untuk console Windows lama) lewat {@link #setWarna(boolean)}.
 *
 * Semua karakter grafis (═ ║ █ ░ ■ ·) dipilih yang tersedia di code page 437/850
 * sehingga tetap tampil benar di Command Prompt Windows.
 */
public final class Teks {

    private static final Pattern ANSI = Pattern.compile("\u001B\\[[0-9;]*m");
    private static boolean warna = true;

    private Teks() {
    }

    public static void setWarna(boolean aktif) {
        warna = aktif;
    }

    public static boolean isWarna() {
        return warna;
    }

    // ---------------------------------------------------------------- warna dasar

    private static String c(String kode, String s) {
        return warna ? "\u001B[" + kode + "m" + s + "\u001B[0m" : s;
    }

    public static String merah(String s)   { return c("91", s); }
    public static String hijau(String s)   { return c("92", s); }
    public static String kuning(String s)  { return c("93", s); }
    public static String biru(String s)    { return c("94", s); }
    public static String magenta(String s) { return c("95", s); }
    public static String cyan(String s)    { return c("96", s); }
    public static String putih(String s)   { return c("97", s); }
    public static String abu(String s)     { return c("90", s); }
    public static String coklat(String s)  { return c("33", s); }
    public static String tebal(String s)   { return c("1", s); }

    // ---------------------------------------------------------------- warna domain

    public static String elemen(Elemen e) {
        return elemen(e, e.getLabel());
    }

    public static String elemen(Elemen e, String s) {
        switch (e) {
            case API:   return merah(s);
            case AIR:   return biru(s);
            case EARTH: return coklat(s);
            case ICE:   return cyan(s);
            default:    return putih(s);
        }
    }

    public static String role(Role r) {
        switch (r) {
            case DPS:     return merah(r.getLabel());
            case SUPPORT: return hijau(r.getLabel());
            default:      return biru(r.getLabel());
        }
    }

    public static String rarity(Rarity r) {
        switch (r) {
            case EPIC: return magenta(r.getLabel());
            case RARE: return cyan(r.getLabel());
            default:   return abu(r.getLabel());
        }
    }

    // ---------------------------------------------------------------- layout

    /** Hapus kode ANSI (untuk menghitung panjang teks yang terlihat). */
    public static String strip(String s) {
        return ANSI.matcher(s).replaceAll("");
    }

    public static int panjang(String s) {
        return strip(s).length();
    }

    /** Pad kanan berdasarkan panjang yang terlihat (aman untuk teks berwarna). */
    public static String pad(String s, int lebar) {
        int kurang = lebar - panjang(s);
        return kurang > 0 ? s + " ".repeat(kurang) : s;
    }

    public static String padKiri(String s, int lebar) {
        int kurang = lebar - panjang(s);
        return kurang > 0 ? " ".repeat(kurang) + s : s;
    }

    public static String tengah(String s, int lebar) {
        int kurang = lebar - panjang(s);
        if (kurang <= 0) return s;
        int kiri = kurang / 2;
        return " ".repeat(kiri) + s + " ".repeat(kurang - kiri);
    }

    // ---------------------------------------------------------------- bar

    public static String bar(int nilai, int maks, int lebar, String kodeWarna) {
        int aman = Math.max(0, Math.min(nilai, maks));
        int isi = maks <= 0 ? 0 : (int) Math.round((double) aman / maks * lebar);
        if (aman > 0 && isi == 0) isi = 1; // tetap tampil sedikit bila masih hidup
        String penuh = "█".repeat(isi);
        String kosong = "░".repeat(lebar - isi);
        return c(kodeWarna, penuh) + abu(kosong);
    }

    public static String barHp(int hp, int maks, int lebar) {
        double rasio = maks <= 0 ? 0 : (double) hp / maks;
        String kode = rasio > 0.5 ? "92" : rasio > 0.25 ? "93" : "91";
        return bar(hp, maks, lebar, kode);
    }

    public static String barMp(int mp, int maks, int lebar) {
        return bar(mp, maks, lebar, "94");
    }
}

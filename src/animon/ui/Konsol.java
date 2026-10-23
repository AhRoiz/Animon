package animon.ui;

import animon.util.InputHabisException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;

/**
 * Pembungkus I/O konsol: membaca input dengan validasi dan membersihkan layar.
 * Semua input game lewat kelas ini, sehingga logika game tidak bergantung langsung
 * pada {@code System.in} / {@code System.out} (mudah diuji dengan input skrip).
 */
public final class Konsol {

    private static final boolean WINDOWS = System.getProperty("os.name", "").toLowerCase().contains("win");

    private final BufferedReader in;
    private final PrintStream out;
    private final boolean clearAktif;

    public Konsol(InputStream in, PrintStream out, boolean clearAktif) {
        this.in = new BufferedReader(new InputStreamReader(in, charsetKonsol()));
        this.out = out;
        this.clearAktif = clearAktif;
    }

    /** Encoding console (mis. IBM437 di Command Prompt); fallback ke default JVM. */
    private static Charset charsetKonsol() {
        String nama = System.getProperty("stdout.encoding");
        try {
            if (nama != null && Charset.isSupported(nama)) return Charset.forName(nama);
        } catch (IllegalArgumentException e) {
            // nama charset tidak valid
        }
        return Charset.defaultCharset();
    }

    // =================================================================== output

    public void println(String s) { out.println(s); }
    public void println()         { out.println(); }
    public void print(String s)   { out.print(s); out.flush(); }

    /**
     * Membersihkan layar agar pemain lawan tidak bisa menyontek (mode hotseat).
     * Windows console: perintah {@code cls}; terminal lain: kode ANSI; tanpa console
     * (mis. output IDE): dorong konten lama keluar layar dengan baris kosong.
     */
    public void bersihkanLayar() {
        out.flush();
        if (!clearAktif) {
            out.println();
            out.println("~".repeat(70));
            out.println();
            return;
        }
        if (System.console() == null) {
            out.print("\n".repeat(60));
            out.flush();
            return;
        }
        if (WINDOWS) {
            try {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                return;
            } catch (IOException e) {
                // jatuh ke ANSI
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        out.print("\u001B[H\u001B[2J\u001B[3J");
        out.flush();
    }

    // =================================================================== input

    /** Membaca satu baris (sudah di-trim). EOF -&gt; {@link InputHabisException}. */
    public String baca(String prompt) {
        print(prompt);
        try {
            String baris = in.readLine();
            if (baris == null) throw new InputHabisException();
            return baris.trim();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Membaca angka dalam rentang [min, max]; mengulang sampai valid. */
    public int bacaAngka(String prompt, int min, int max) {
        while (true) {
            String s = baca(prompt);
            try {
                int n = Integer.parseInt(s);
                if (n >= min && n <= max) return n;
            } catch (NumberFormatException e) {
                // ulangi
            }
            println("  Masukkan angka " + min + " sampai " + max + ".");
        }
    }

    /** Seperti {@link #bacaAngka(String, int, int)} tetapi input kosong = {@code bawaan}. */
    public int bacaAngka(String prompt, int min, int max, int bawaan) {
        while (true) {
            String s = baca(prompt);
            if (s.isEmpty()) return bawaan;
            try {
                int n = Integer.parseInt(s);
                if (n >= min && n <= max) return n;
            } catch (NumberFormatException e) {
                // ulangi
            }
            println("  Masukkan angka " + min + " sampai " + max + " (Enter = " + bawaan + ").");
        }
    }

    /** Teks dengan nilai bawaan bila input kosong. */
    public String bacaTeks(String prompt, String bawaan) {
        String s = baca(prompt);
        return s.isEmpty() ? bawaan : s;
    }

    public boolean konfirmasi(String prompt) {
        while (true) {
            String s = baca(prompt + " (y/n): ").toLowerCase();
            if (s.equals("y") || s.equals("ya")) return true;
            if (s.equals("n") || s.equals("tidak")) return false;
        }
    }

    public void tungguEnter(String pesan) {
        baca(pesan);
    }

    public void tungguEnter() {
        tungguEnter("  Tekan Enter untuk lanjut...");
    }
}

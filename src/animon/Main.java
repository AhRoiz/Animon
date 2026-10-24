package animon;

import animon.data.GameSeeder;
import animon.game.Permainan;
import animon.ui.Konsol;
import animon.util.Acak;
import animon.util.InputHabisException;
import animon.util.JsonException;
import animon.util.Teks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

/**
 * Titik masuk (entry point) game ANIMON.
 *
 * <p>Main sengaja dibuat tipis: TIDAK ada pembuatan objek Hewan/Item di sini.
 * Semua data dimuat oleh {@link GameSeeder} dari file JSON eksternal, lalu alur
 * permainan diserahkan ke {@link Permainan}.</p>
 *
 * <pre>
 * Penggunaan:
 *   java -cp out animon.Main [opsi]
 *
 * Opsi:
 *   --data &lt;folder&gt;   Folder berisi data_hewan.json &amp; data_item.json (bawaan: ./data)
 *   --seed &lt;angka&gt;    Seed angka acak agar pertandingan bisa direplikasi
 *   --no-color        Matikan warna ANSI (untuk console lama)
 *   --no-clear        Jangan bersihkan layar (berguna saat debugging)
 *   --help            Tampilkan bantuan
 * </pre>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Opsi opsi;
        try {
            opsi = Opsi.parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println("Argumen tidak valid: " + e.getMessage());
            cetakBantuan();
            System.exit(2);
            return;
        }
        if (opsi.bantuan) {
            cetakBantuan();
            return;
        }

        Teks.setWarna(opsi.warna);
        if (opsi.seed != null) Acak.set(new Random(opsi.seed));

        // 1) Muat database dari JSON (Factory/Seeder).
        GameSeeder seeder;
        try {
            Path folder = cariFolderData(opsi.folderData);
            seeder = GameSeeder.dariFolder(folder);
        } catch (IOException e) {
            System.err.println("Gagal membaca file data: " + e.getMessage());
            System.err.println("Pastikan folder 'data' berisi " + GameSeeder.FILE_HEWAN + " dan "
                    + GameSeeder.FILE_ITEM + ", atau gunakan --data <folder>.");
            System.exit(1);
            return;
        } catch (JsonException e) {
            System.err.println("Data JSON tidak valid -> " + e.getMessage());
            System.exit(1);
            return;
        }

        // 2) Jalankan permainan.
        Konsol konsol = new Konsol(System.in, System.out, opsi.bersihkanLayar);
        try {
            new Permainan(konsol, seeder).mulai();
        } catch (InputHabisException e) {
            System.out.println();
            System.out.println("  Input ditutup. Sampai jumpa di ANIMON!");
        }
    }

    /**
     * Mencari folder data. Bila tidak ditentukan lewat argumen, coba {@code ./data}
     * lalu {@code ../data} (agar tetap jalan meski dijalankan dari folder src/out).
     */
    private static Path cariFolderData(String folderArg) throws IOException {
        if (folderArg != null) {
            Path p = Paths.get(folderArg);
            if (!Files.isDirectory(p)) throw new IOException("folder tidak ditemukan: " + p.toAbsolutePath());
            return p;
        }
        for (String kandidat : new String[] {"data", "../data"}) {
            Path p = Paths.get(kandidat);
            if (Files.isRegularFile(p.resolve(GameSeeder.FILE_HEWAN))) return p;
        }
        throw new IOException("folder data tidak ditemukan dari " + Paths.get("").toAbsolutePath());
    }

    private static void cetakBantuan() {
        System.out.println("ANIMON - Turn-based battle CLI (hotseat 1v1)");
        System.out.println();
        System.out.println("Penggunaan: java -cp out animon.Main [opsi]");
        System.out.println("  --data <folder>   Folder data JSON (bawaan: ./data)");
        System.out.println("  --seed <angka>    Seed acak agar pertandingan bisa direplikasi");
        System.out.println("  --no-color        Matikan warna ANSI");
        System.out.println("  --no-clear        Jangan bersihkan layar");
        System.out.println("  --help            Tampilkan bantuan ini");
    }

    /** Hasil parsing argumen command line (immutable setelah dibuat). */
    private static final class Opsi {
        private String folderData;
        private Long seed;
        private boolean warna = true;
        private boolean bersihkanLayar = true;
        private boolean bantuan;

        static Opsi parse(String[] args) {
            Opsi o = new Opsi();
            for (int i = 0; i < args.length; i++) {
                String a = args[i];
                switch (a) {
                    case "--data":
                        o.folderData = nilai(args, ++i, a);
                        break;
                    case "--seed":
                        String s = nilai(args, ++i, a);
                        try {
                            o.seed = Long.parseLong(s);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("--seed harus angka, bukan '" + s + "'");
                        }
                        break;
                    case "--no-color":
                        o.warna = false;
                        break;
                    case "--no-clear":
                        o.bersihkanLayar = false;
                        break;
                    case "-h":
                    case "--help":
                        o.bantuan = true;
                        break;
                    default:
                        throw new IllegalArgumentException("opsi tidak dikenal '" + a + "'");
                }
            }
            return o;
        }

        private static String nilai(String[] args, int i, String opsi) {
            if (i >= args.length) throw new IllegalArgumentException(opsi + " membutuhkan nilai");
            return args[i];
        }
    }
}

package animon.gui.tema;

import animon.model.Hewan;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Pemuat sprite hewan dari {@code src/animon/assets/} dengan penskalaan nearest-neighbor tajam.
 * Menangani pencocokan camelCase, alias nama singkat, toleransi salah ketik Levenshtein, dan cadangan gambar.
 */
public final class SpriteLoader {

    public static final int UKURAN_ASLI = 64;

    /** Alias nama camelCase -&gt; nama dasar file (untuk file yang dinamai berbeda). */
    private static final Map<String, String> ALIAS = new HashMap<>();

    static {
        ALIAS.put("lumbaLumbaPelangi", "lumbaPelangi");
        ALIAS.put("nagaGletser", "nagaEs");
    }

    private static final Map<String, BufferedImage> CACHE_ASLI = new HashMap<>();
    private static final Map<String, ImageIcon> CACHE_ICON = new HashMap<>();

    private static File folderAset;
    private static boolean folderDicari;
    private static boolean modeGameBoy;

    private SpriteLoader() {
    }

    /** Sprite depan (dipakai untuk hewan musuh di arena atas &amp; kartu pilihan). */
    public static ImageIcon depan(Hewan h, int ukuran) {
        return icon(h.getNama(), true, ukuran);
    }

    /** Sprite belakang (dipakai untuk hewan aktif pemain di arena bawah). */
    public static ImageIcon belakang(Hewan h, int ukuran) {
        return icon(h.getNama(), false, ukuran);
    }

    /** Sprite berdasarkan nama hewan (dipakai layar replay yang hanya menyimpan nama). */
    public static ImageIcon icon(String namaHewan, boolean depan, int ukuran) {
        String kunci = namaHewan + "|" + depan + "|" + ukuran + "|" + modeGameBoy;
        ImageIcon ic = CACHE_ICON.get(kunci);
        if (ic == null) {
            BufferedImage img = gambar(namaHewan, depan);
            if (modeGameBoy) img = filterGameBoy(img);
            // Nearest-neighbor: piksel diduplikasi, bukan diinterpolasi (tidak blur).
            Image skala = img.getScaledInstance(ukuran, ukuran, Image.SCALE_REPLICATE);
            ic = new ImageIcon(skala);
            CACHE_ICON.put(kunci, ic);
        }
        return ic;
    }

    /** Mode warna sprite: false = warna asli aset, true = dipetakan ke 4 warna hijau Game Boy. */
    public static void setModeGameBoy(boolean gb) {
        if (modeGameBoy != gb) {
            modeGameBoy = gb;
            CACHE_ICON.clear();
        }
    }

    public static boolean isModeGameBoy() {
        return modeGameBoy;
    }

    /** Ubah nama hewan menjadi camelCase, mis. "Lumba-lumba Pelangi" -&gt; "lumbaLumbaPelangi". */
    public static String camelCase(String nama) {
        String[] kata = nama.trim().split("[^A-Za-z0-9]+");
        StringBuilder sb = new StringBuilder();
        for (String k : kata) {
            if (k.isEmpty()) continue;
            String kecil = k.toLowerCase(Locale.ROOT);
            if (sb.length() == 0) sb.append(kecil);
            else sb.append(Character.toUpperCase(kecil.charAt(0))).append(kecil.substring(1));
        }
        return sb.toString();
    }

    /** Folder {@code src/animon/assets} (dicari relatif terhadap working directory / classpath). */
    public static synchronized File folderAset() {
        if (folderDicari) return folderAset;
        folderDicari = true;
        String[] kandidat = {"src/animon/assets", "../src/animon/assets", "animon/assets", "assets"};
        for (String k : kandidat) {
            File f = new File(k);
            if (f.isDirectory()) {
                folderAset = f;
                return f;
            }
        }
        URL url = SpriteLoader.class.getResource("/animon/assets");
        if (url != null && "file".equals(url.getProtocol())) {
            try {
                File f = new File(url.toURI());
                if (f.isDirectory()) folderAset = f;
            } catch (URISyntaxException ignored) {
            }
        }
        return folderAset;
    }

    private static BufferedImage gambar(String namaHewan, boolean depan) {
        String kunci = namaHewan + "|" + depan;
        BufferedImage img = CACHE_ASLI.get(kunci);
        if (img == null) {
            img = cari(camelCase(namaHewan), depan ? "_depan" : "_belakang");
            if (img == null) img = spriteCadangan(depan);
            CACHE_ASLI.put(kunci, img);
        }
        return img;
    }

    private static BufferedImage cari(String camel, String akhiran) {
        BufferedImage img = baca(camel + akhiran);
        if (img == null && ALIAS.containsKey(camel)) img = baca(ALIAS.get(camel) + akhiran);
        if (img != null) return img;

        File dir = folderAset();
        if (dir == null) return null;
        File[] files = dir.listFiles((d, n) -> n.toLowerCase(Locale.ROOT).endsWith(".png"));
        if (files == null) return null;

        String target = (ALIAS.getOrDefault(camel, camel)).toLowerCase(Locale.ROOT);
        String akhiranKecil = akhiran.toLowerCase(Locale.ROOT);

        // Toleransi salah ketik kecil pada nama file aset (mis. huruf ganda).
        File terbaik = null;
        int jarakTerbaik = Integer.MAX_VALUE;
        for (File f : files) {
            String n = f.getName().toLowerCase(Locale.ROOT);
            n = n.substring(0, n.length() - 4);
            if (!n.endsWith(akhiranKecil)) continue;
            String dasar = n.substring(0, n.length() - akhiranKecil.length());
            int jarak = levenshtein(dasar, target);
            if (jarak < jarakTerbaik) {
                jarakTerbaik = jarak;
                terbaik = f;
            }
        }
        int batas = Math.max(2, target.length() / 5);
        if (terbaik != null && jarakTerbaik <= batas) return bacaFile(terbaik);

        // Aset tanpa akhiran arah dipakai sebagai sprite depan.
        if ("_depan".equals(akhiran)) {
            for (File f : files) {
                String n = f.getName().toLowerCase(Locale.ROOT);
                n = n.substring(0, n.length() - 4);
                if (n.contains("_")) continue;
                if (levenshtein(n, target) <= batas) return bacaFile(f);
            }
        }
        return null;
    }

    private static BufferedImage baca(String namaDasar) {
        String file = namaDasar + ".png";
        try (InputStream in = SpriteLoader.class.getResourceAsStream("/animon/assets/" + file)) {
            if (in != null) return ImageIO.read(in);
        } catch (IOException ignored) {
        }
        File dir = folderAset();
        if (dir == null) return null;
        File f = new File(dir, file);
        return f.isFile() ? bacaFile(f) : null;
    }

    private static BufferedImage bacaFile(File f) {
        try {
            return ImageIO.read(f);
        } catch (IOException e) {
            return null;
        }
    }

    private static int levenshtein(String a, String b) {
        int[] prev = new int[b.length() + 1];
        int[] cur = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) prev[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            cur[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int biaya = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + biaya);
            }
            int[] t = prev;
            prev = cur;
            cur = t;
        }
        return prev[b.length()];
    }

    /** Sprite "?" 64x64 sederhana bila file tidak ditemukan. */
    private static BufferedImage spriteCadangan(boolean depan) {
        BufferedImage img = new BufferedImage(UKURAN_ASLI, UKURAN_ASLI, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Tema.TUA);
        g.fillOval(12, 14, 40, 40);
        g.setColor(Tema.GELAP);
        g.fillRect(26, 22, 12, 4);
        g.fillRect(34, 26, 4, 8);
        g.fillRect(28, 34, 8, 4);
        g.fillRect(28, 38, 4, 4);
        g.fillRect(28, 44, 4, 4);
        if (!depan) {
            g.setColor(Tema.MUDA);
            g.fillRect(16, 50, 32, 4);
        }
        g.dispose();
        return img;
    }

    /** Memetakan tiap piksel ke salah satu dari 4 warna hijau berdasarkan luminansi (alpha dipertahankan). */
    private static BufferedImage filterGameBoy(BufferedImage src) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int argb = src.getRGB(x, y);
                int a = (argb >>> 24) & 0xff;
                if (a < 40) continue;
                int r = (argb >> 16) & 0xff;
                int gr = (argb >> 8) & 0xff;
                int b = argb & 0xff;
                double lum = 0.299 * r + 0.587 * gr + 0.114 * b;
                int idx = lum < 60 ? 0 : lum < 125 ? 1 : lum < 190 ? 2 : 3;
                // Warna paling terang = latar, jadi gunakan maksimal indeks 2 agar sprite tetap terlihat.
                idx = Math.min(idx, 2);
                out.setRGB(x, y, (0xff << 24) | (Tema.PALET[idx].getRGB() & 0xffffff));
            }
        }
        return out;
    }
}

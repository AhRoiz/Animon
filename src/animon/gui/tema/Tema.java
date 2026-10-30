package animon.gui.tema;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Design system GUI ANIMON bertema <b>Retro Game Boy</b>.
 *
 * <ul>
 *   <li>Palet 4 warna hijau klasik DMG-01 ({@link #GELAP} .. {@link #TERANG}).</li>
 *   <li>Font pixel: <i>Press Start 2P</i> untuk judul/tombol dan <i>VT323</i> untuk teks/log.
 *       Keduanya (lisensi OFL) dimuat dari {@code src/animon/assets/fonts}. Bila tidak ada,
 *       otomatis jatuh ke {@code Monospaced} tebal.</li>
 * </ul>
 *
 * Semua panel/komponen GUI wajib mengambil warna &amp; font dari kelas ini agar tampilan konsisten.
 */
public final class Tema {

    // =================================================================== palet Game Boy (gelap -> terang)

    /** #0f380f - warna paling gelap (teks, garis tepi). */
    public static final Color GELAP = new Color(0x0f380f);
    /** #306230 - hijau tua (bayangan, isi MP). */
    public static final Color TUA = new Color(0x306230);
    /** #8bac0f - hijau muda (aksen, panel sekunder). */
    public static final Color MUDA = new Color(0x8bac0f);
    /** #9bbc0f - warna paling terang (latar layar LCD). */
    public static final Color TERANG = new Color(0x9bbc0f);

    /** Palet berurutan dari gelap ke terang (dipakai filter sprite mode GB). */
    public static final Color[] PALET = {GELAP, TUA, MUDA, TERANG};

    // =================================================================== font

    private static Font fontJudul;
    private static Font fontTeks;
    private static boolean fontPixelTersedia;

    private Tema() {
    }

    /** Dipanggil sekali saat aplikasi start (sebelum komponen dibuat). */
    public static void pasang() {
        muatFont();
        pasangUIManager();
    }

    private static void muatFont() {
        Font judul = bacaFont("PressStart2P-Regular.ttf");
        Font teks = bacaFont("VT323-Regular.ttf");
        fontPixelTersedia = judul != null;
        fontJudul = judul != null ? judul : new Font(Font.MONOSPACED, Font.BOLD, 12);
        fontTeks = teks != null ? teks : new Font(Font.MONOSPACED, Font.BOLD, 14);
    }

    private static Font bacaFont(String file) {
        // 1) classpath (bila aset ikut tersalin ke folder out/)
        try (InputStream in = Tema.class.getResourceAsStream("/animon/assets/fonts/" + file)) {
            if (in != null) return daftarkan(Font.createFont(Font.TRUETYPE_FONT, in));
        } catch (IOException | FontFormatException ignored) {
            // lanjut ke pencarian filesystem
        }
        // 2) filesystem: src/animon/assets/fonts
        File dir = SpriteLoader.folderAset();
        if (dir != null) {
            File f = new File(new File(dir, "fonts"), file);
            if (f.isFile()) {
                try (InputStream in = Files.newInputStream(f.toPath())) {
                    return daftarkan(Font.createFont(Font.TRUETYPE_FONT, in));
                } catch (IOException | FontFormatException ignored) {
                    // jatuh ke font bawaan
                }
            }
        }
        return null;
    }

    private static Font daftarkan(Font f) {
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
        return f;
    }

    /** Font pixel untuk judul, tombol, dan label penting. Ukuran ideal kelipatan 8. */
    public static Font judul(float ukuran) {
        if (fontJudul == null) muatFont();
        // Monospaced fallback terlihat lebih kecil; perbesar sedikit agar proporsional.
        return fontJudul.deriveFont(fontPixelTersedia ? ukuran : ukuran * 1.35f);
    }

    /** Font pixel yang mudah dibaca untuk paragraf, log, dan daftar. */
    public static Font teks(float ukuran) {
        if (fontTeks == null) muatFont();
        return fontTeks.deriveFont(fontPixelTersedia ? ukuran : ukuran * 0.8f);
    }

    public static boolean isFontPixelTersedia() {
        return fontPixelTersedia;
    }

    // =================================================================== helper render

    /** Rendering tajam ala pixel art (tanpa interpolasi blur). */
    public static void pixelated(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                fontPixelTersedia ? RenderingHints.VALUE_TEXT_ANTIALIAS_OFF : RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /** Pola dither 2x2 (setengah titik) untuk efek bayangan khas Game Boy. */
    public static void dither(Graphics2D g, int x, int y, int w, int h, Color warna, int jarak) {
        g.setColor(warna);
        for (int yy = y; yy < y + h; yy += jarak) {
            int offset = ((yy - y) / jarak) % 2 == 0 ? 0 : jarak / 2;
            for (int xx = x + offset; xx < x + w; xx += jarak) g.fillRect(xx, yy, 2, 2);
        }
    }

    // =================================================================== UIManager

    private static void pasangUIManager() {
        ColorUIResource gelap = new ColorUIResource(GELAP);
        ColorUIResource terang = new ColorUIResource(TERANG);
        ColorUIResource muda = new ColorUIResource(MUDA);

        UIManager.put("Panel.background", terang);
        UIManager.put("OptionPane.background", terang);
        UIManager.put("OptionPane.messageForeground", gelap);
        UIManager.put("OptionPane.messageFont", new FontUIResource(teks(22f)));
        UIManager.put("Label.foreground", gelap);
        UIManager.put("Label.font", new FontUIResource(teks(22f)));

        UIManager.put("ToolTip.background", gelap);
        UIManager.put("ToolTip.foreground", terang);
        UIManager.put("ToolTip.font", new FontUIResource(teks(20f)));
        UIManager.put("ToolTip.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MUDA, 2), BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        UIManager.put("TextField.background", terang);
        UIManager.put("TextField.foreground", gelap);
        UIManager.put("TextField.caretForeground", gelap);
        UIManager.put("TextField.selectionBackground", gelap);
        UIManager.put("TextField.selectionForeground", terang);
        UIManager.put("TextArea.selectionBackground", gelap);
        UIManager.put("TextArea.selectionForeground", terang);

        UIManager.put("List.background", terang);
        UIManager.put("List.foreground", gelap);
        UIManager.put("List.selectionBackground", gelap);
        UIManager.put("List.selectionForeground", terang);

        UIManager.put("ScrollPane.background", terang);
        UIManager.put("Viewport.background", terang);
        UIManager.put("ScrollBar.width", 14);

        UIManager.put("Spinner.background", terang);
        UIManager.put("Spinner.foreground", gelap);
        UIManager.put("FormattedTextField.background", terang);
        UIManager.put("FormattedTextField.foreground", gelap);

        UIManager.put("Table.background", terang);
        UIManager.put("Table.foreground", gelap);
        UIManager.put("Table.selectionBackground", gelap);
        UIManager.put("Table.selectionForeground", terang);
        UIManager.put("Table.gridColor", muda);
        UIManager.put("TableHeader.background", gelap);
        UIManager.put("TableHeader.foreground", terang);
    }
}

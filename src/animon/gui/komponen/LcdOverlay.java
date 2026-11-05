package animon.gui.komponen;

import animon.gui.tema.Tema;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Glass pane tanpa listener mouse untuk garis piksel LCD dan tirai transisi antar-layar.
 * Klik mouse diteruskan penuh ke komponen aktif di bawahnya.
 */
public final class LcdOverlay extends JComponent {

    private static final Color GRID = new Color(15, 56, 15, 16);
    private static final int TINGGI_PITA = 28;

    private double progres; // 0 = terbuka, 1 = tertutup penuh
    private Timer timer;
    private boolean gridAktif = true;

    public LcdOverlay() {
        setOpaque(false);
    }

    public void setGridAktif(boolean aktif) {
        this.gridAktif = aktif;
        repaint();
    }

    public boolean isGridAktif() {
        return gridAktif;
    }

    /** Tutup layar dengan pita bergaris, jalankan {@code tengah} (ganti panel), lalu buka lagi. */
    public void transisi(Runnable tengah) {
        if (timer != null && timer.isRunning()) {
            timer.stop();
            progres = 0;
        }
        if (!Tema.isAnimasiAktif()) {
            progres = 0;
            tengah.run();
            repaint();
            return;
        }
        final boolean[] menutup = {true};
        timer = new Timer(15, null);
        timer.addActionListener(e -> {
            if (menutup[0]) {
                progres += 0.16;
                if (progres >= 1) {
                    progres = 1;
                    menutup[0] = false;
                    tengah.run();
                }
            } else {
                progres -= 0.12;
                if (progres <= 0) {
                    progres = 0;
                    timer.stop();
                }
            }
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Rectangle clip = g.getClipBounds();
        if (clip == null) clip = new Rectangle(0, 0, getWidth(), getHeight());

        if (gridAktif) {
            g.setColor(GRID);
            int x0 = clip.x - clip.x % 3;
            int y0 = clip.y - clip.y % 3;
            for (int x = x0; x < clip.x + clip.width; x += 3) g.fillRect(x, clip.y, 1, clip.height);
            for (int y = y0; y < clip.y + clip.height; y += 3) g.fillRect(clip.x, y, clip.width, 1);
        }

        if (progres > 0) {
            g.setColor(Tema.GELAP);
            int h = getHeight();
            int w = getWidth();
            int i = 0;
            for (int y = 0; y < h; y += TINGGI_PITA, i++) {
                int tinggi = (int) Math.ceil(TINGGI_PITA * progres);
                // pita ganjil masuk dari kiri, genap dari kanan
                int lebar = (int) Math.ceil(w * Math.min(1, progres * 1.3));
                int x = i % 2 == 0 ? 0 : w - lebar;
                g.fillRect(x, y + (TINGGI_PITA - tinggi) / 2, lebar, tinggi);
            }
        }
        g.dispose();
    }
}

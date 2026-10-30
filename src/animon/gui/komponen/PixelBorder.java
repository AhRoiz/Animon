package animon.gui.komponen;

import animon.gui.tema.Tema;

import javax.swing.border.AbstractBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

/**
 * Bingkai kotak dialog ala Game Boy: garis luar tebal dengan sudut "terpotong" (notched)
 * dan garis dalam tipis, seperti kotak teks pada game RPG 8-bit.
 */
public class PixelBorder extends AbstractBorder {

    private final Color luar;
    private final Color dalam;
    private final int tebal;
    private final int padding;

    public PixelBorder() {
        this(Tema.GELAP, Tema.TUA, 4, 10);
    }

    public PixelBorder(Color luar, Color dalam, int tebal, int padding) {
        this.luar = luar;
        this.dalam = dalam;
        this.tebal = tebal;
        this.padding = padding;
    }

    @Override
    public void paintBorder(Component c, Graphics g0, int x, int y, int w, int h) {
        Graphics2D g = (Graphics2D) g0.create();
        int t = tebal;
        g.setColor(luar);
        // sisi (tanpa sudut -> efek sudut membulat pixel)
        g.fillRect(x + t, y, w - 2 * t, t);
        g.fillRect(x + t, y + h - t, w - 2 * t, t);
        g.fillRect(x, y + t, t, h - 2 * t);
        g.fillRect(x + w - t, y + t, t, h - 2 * t);
        // sudut dalam
        g.fillRect(x + t, y + t, t / 2, t / 2);
        g.fillRect(x + w - t - t / 2, y + t, t / 2, t / 2);
        g.fillRect(x + t, y + h - t - t / 2, t / 2, t / 2);
        g.fillRect(x + w - t - t / 2, y + h - t - t / 2, t / 2, t / 2);
        // garis dalam
        if (dalam != null) {
            g.setColor(dalam);
            int d = t + t / 2 + 1;
            g.drawRect(x + d, y + d, w - 2 * d - 1, h - 2 * d - 1);
        }
        g.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        int i = tebal * 2 + padding;
        return new Insets(i, i, i, i);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        int i = tebal * 2 + padding;
        insets.set(i, i, i, i);
        return insets;
    }
}

package animon.gui.komponen;

import animon.gui.tema.Tema;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/** Scrollbar pixel tanpa tombol panah: track terang ber-dither, thumb hijau tua. */
public class PixelScrollBarUI extends BasicScrollBarUI {

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return tombolKosong();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return tombolKosong();
    }

    private static JButton tombolKosong() {
        JButton b = new JButton();
        b.setPreferredSize(new Dimension(0, 0));
        b.setMinimumSize(new Dimension(0, 0));
        b.setMaximumSize(new Dimension(0, 0));
        return b;
    }

    @Override
    protected void paintTrack(Graphics g0, JComponent c, Rectangle r) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setColor(Tema.TERANG);
        g.fillRect(r.x, r.y, r.width, r.height);
        Tema.dither(g, r.x + 2, r.y, r.width - 2, r.height, Tema.MUDA, 4);
        g.dispose();
    }

    @Override
    protected void paintThumb(Graphics g0, JComponent c, Rectangle r) {
        if (r.isEmpty()) return;
        Graphics2D g = (Graphics2D) g0.create();
        boolean vertikal = scrollbar.getOrientation() == JScrollBar.VERTICAL;
        g.setColor(Tema.GELAP);
        g.fillRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4);
        g.setColor(isThumbRollover() ? Tema.MUDA : Tema.TUA);
        g.fillRect(r.x + 4, r.y + 4, r.width - 8, r.height - 8);
        // gerigi pegangan
        g.setColor(Tema.GELAP);
        int cx = r.x + r.width / 2;
        int cy = r.y + r.height / 2;
        for (int i = -1; i <= 1; i++) {
            if (vertikal) g.fillRect(r.x + 5, cy + i * 4, r.width - 10, 2);
            else g.fillRect(cx + i * 4, r.y + 5, 2, r.height - 10);
        }
        g.dispose();
    }
}

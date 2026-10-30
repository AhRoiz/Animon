package animon.gui.komponen;

import animon.gui.tema.Tema;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.Timer;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * {@link JLabel} untuk sprite hewan di arena, lengkap dengan:
 * <ul>
 *   <li>platform/bayangan elips ber-dither di bawah sprite (ala arena Pokemon),</li>
 *   <li>animasi idle naik-turun 2px,</li>
 *   <li>animasi berkedip saat terkena damage ({@link #animasiKena()}),</li>
 *   <li>animasi masuk arena dari samping ({@link #animasiMasuk()}),</li>
 *   <li>animasi tumbang: sprite tenggelam ke bawah ({@link #animasiPingsan()}).</li>
 * </ul>
 */
public class SpriteView extends JLabel {

    private final int ukuran;
    private final boolean dariKanan;

    private int bob;
    private int geserX;
    private int geserY;
    private boolean tampil = true;
    private boolean pingsan;

    private final Timer timerIdle;
    private Timer timerEfek;

    public SpriteView(int ukuran, boolean dariKanan) {
        this.ukuran = ukuran;
        this.dariKanan = dariKanan;
        setHorizontalAlignment(CENTER);
        setOpaque(false);
        timerIdle = new Timer(520, e -> {
            if (!pingsan) {
                bob = bob == 0 ? -3 : 0;
                repaint();
            }
        });
        timerIdle.start();
    }

    public void setSprite(Icon icon, boolean pingsan) {
        setIcon(icon);
        this.pingsan = pingsan;
        if (timerEfek != null) timerEfek.stop();
        geserX = 0;
        geserY = pingsan ? ukuran : 0;
        tampil = true;
        repaint();
    }

    /** Sprite berkedip 4x (terkena serangan). */
    public void animasiKena() {
        if (pingsan) return;
        mulaiEfek((tick) -> {
            tampil = tick % 2 == 1;
            geserX = tick < 6 ? (tick % 2 == 0 ? 4 : -4) : 0;
            return tick >= 8;
        }, 70);
    }

    /** Sprite meluncur masuk arena. */
    public void animasiMasuk() {
        pingsan = false;
        geserY = 0;
        geserX = dariKanan ? ukuran * 2 : -ukuran * 2;
        tampil = true;
        mulaiEfek((tick) -> {
            geserX = (int) (geserX * 0.72);
            if (Math.abs(geserX) < 3) {
                geserX = 0;
                return true;
            }
            return false;
        }, 16);
    }

    /** Sprite tenggelam (pingsan). */
    public void animasiPingsan() {
        pingsan = true;
        mulaiEfek((tick) -> {
            geserY += Math.max(4, ukuran / 16);
            if (geserY >= ukuran) {
                geserY = ukuran;
                return true;
            }
            return false;
        }, 20);
    }

    private interface Langkah {
        boolean jalan(int tick);
    }

    private void mulaiEfek(Langkah langkah, int delay) {
        if (timerEfek != null) timerEfek.stop();
        final int[] tick = {0};
        timerEfek = new Timer(delay, null);
        timerEfek.addActionListener(e -> {
            tick[0]++;
            boolean selesai = langkah.jalan(tick[0]);
            if (selesai) {
                tampil = true;
                timerEfek.stop();
            }
            repaint();
        });
        timerEfek.start();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(ukuran + 60, ukuran + 24);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Tema.pixelated(g);
        int w = getWidth();
        int h = getHeight();

        // platform elips ber-dither
        int pw = ukuran + 50;
        int ph = Math.max(18, ukuran / 5);
        int px = (w - pw) / 2;
        int py = h - ph - 2;
        g.setColor(Tema.MUDA);
        g.fillOval(px, py, pw, ph);
        g.setColor(Tema.TUA);
        g.drawOval(px, py, pw, ph);
        g.drawOval(px + 1, py + 1, pw - 2, ph - 2);
        Graphics2D gc = (Graphics2D) g.create();
        gc.clipRect(px, py + ph / 2, pw, ph / 2);
        gc.clip(new java.awt.geom.Ellipse2D.Float(px + 3, py + 3, pw - 6, ph - 6));
        Tema.dither(gc, px, py + ph / 2, pw, ph / 2, Tema.TUA, 4);
        gc.dispose();

        Icon ic = getIcon();
        if (ic != null && tampil) {
            int ix = (w - ic.getIconWidth()) / 2 + geserX;
            int iy = py + ph / 2 - ic.getIconHeight() + bob + geserY + 6;
            Graphics2D gs = (Graphics2D) g.create();
            gs.clipRect(0, 0, w, py + ph / 2 + 4); // sprite tenggelam di balik platform saat pingsan
            ic.paintIcon(this, gs, ix, iy);
            gs.dispose();
        }
        g.dispose();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        timerIdle.stop();
        if (timerEfek != null) timerEfek.stop();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        timerIdle.start();
    }
}

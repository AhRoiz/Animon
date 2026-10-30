package animon.gui.komponen;

import animon.gui.tema.Tema;

import javax.swing.JButton;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Tombol bergaya menu Game Boy.
 *
 * <ul>
 *   <li>Normal: latar terang, bingkai gelap.</li>
 *   <li>Hover / fokus keyboard: warna dibalik + kursor segitiga berkedip di kiri (seperti menu RPG).</li>
 *   <li>Ditekan: bergeser 2px (efek tombol fisik).</li>
 *   <li>Nonaktif: teks hijau tua dengan pola dither.</li>
 * </ul>
 * Mendukung sub-teks kecil di bawah judul (mis. "HP 120/260" atau "20 MP").
 */
public class PixelButton extends JButton {

    public enum Gaya { TERANG, GELAP }

    private String subTeks;
    private boolean hover;
    private boolean mouseDidalam;
    private boolean kursorTampil = true;
    private Gaya gaya = Gaya.TERANG;
    private float ukuranFont;
    private final Timer kedip;

    public PixelButton(String teks) {
        this(teks, null, 14f);
    }

    public PixelButton(String teks, String subTeks) {
        this(teks, subTeks, 12f);
    }

    public PixelButton(String teks, String subTeks, float ukuranFont) {
        super(teks);
        this.subTeks = subTeks;
        this.ukuranFont = ukuranFont;
        setFont(Tema.judul(ukuranFont));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(10, 26, 10, 14));

        kedip = new Timer(420, e -> {
            kursorTampil = !kursorTampil;
            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseDidalam = true;
                setHover(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseDidalam = false;
                setHover(isFocusOwner());
            }
        });
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                setHover(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                setHover(mouseDidalam);
            }
        });
    }

    private void setHover(boolean h) {
        if (hover == h) return;
        hover = h && isEnabled();
        kursorTampil = true;
        if (hover) kedip.start();
        else kedip.stop();
        repaint();
    }

    public PixelButton gaya(Gaya g) {
        this.gaya = g;
        repaint();
        return this;
    }

    public void setSubTeks(String s) {
        this.subTeks = s;
        revalidate();
        repaint();
    }

    public String getSubTeks() {
        return subTeks;
    }

    public void setUkuranFont(float ukuran) {
        this.ukuranFont = ukuran;
        setFont(Tema.judul(ukuran));
        revalidate();
    }

    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        if (!b) setHover(false);
        setCursor(Cursor.getPredefinedCursor(b ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        Insets m = getMargin();
        int w = fm.stringWidth(getText()) + m.left + m.right + 8;
        int h = fm.getHeight() + m.top + m.bottom + 8;
        if (subTeks != null) {
            FontMetrics fs = getFontMetrics(Tema.teks(ukuranFont + 6));
            w = Math.max(w, fs.stringWidth(subTeks) + m.left + m.right + 8);
            h += fs.getHeight() - 2;
        }
        return new Dimension(w, h);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Tema.pixelated(g);
        int w = getWidth();
        int h = getHeight();
        boolean tekan = getModel().isArmed() && getModel().isPressed();
        int geser = tekan ? 2 : 0;

        boolean aktif = isEnabled();
        boolean balik = aktif && (hover || getModel().isRollover());
        Color latar;
        Color tinta;
        if (gaya == Gaya.GELAP) {
            latar = balik ? Tema.TERANG : Tema.GELAP;
            tinta = balik ? Tema.GELAP : Tema.TERANG;
        } else {
            latar = balik ? Tema.GELAP : Tema.TERANG;
            tinta = balik ? Tema.TERANG : Tema.GELAP;
        }
        if (!aktif) {
            latar = Tema.MUDA;
            tinta = Tema.TUA;
        }

        // bayangan
        if (!tekan) {
            g.setColor(Tema.TUA);
            g.fillRect(4, 4, w - 4, h - 4);
        }
        int x = geser;
        int y = geser;
        int bw = w - 4;
        int bh = h - 4;
        // isi + bingkai (sudut terpotong 2px)
        g.setColor(Tema.GELAP);
        g.fillRect(x + 2, y, bw - 4, bh);
        g.fillRect(x, y + 2, bw, bh - 4);
        g.setColor(latar);
        g.fillRect(x + 3, y + 3, bw - 6, bh - 6);
        if (!aktif) Tema.dither(g, x + 3, y + 3, bw - 6, bh - 6, Tema.TERANG, 4);

        // teks
        Font fJudul = getFont();
        FontMetrics fm = g.getFontMetrics(fJudul);
        Font fSub = Tema.teks(ukuranFont + 6);
        FontMetrics fs = g.getFontMetrics(fSub);
        int tinggiIsi = fm.getAscent() + (subTeks != null ? fs.getHeight() - 2 : 0);
        int ty = y + (bh - tinggiIsi) / 2 + fm.getAscent();
        int tw = fm.stringWidth(getText());
        int tx = x + Math.max(getMargin().left, (bw - tw) / 2 + 6);
        if (getHorizontalAlignment() == LEFT) tx = x + getMargin().left;

        g.setColor(tinta);
        g.setFont(fJudul);
        g.drawString(getText(), tx, ty);
        if (subTeks != null) {
            g.setFont(fSub);
            int sx = getHorizontalAlignment() == LEFT ? tx : x + Math.max(getMargin().left, (bw - fs.stringWidth(subTeks)) / 2 + 6);
            g.drawString(subTeks, sx, ty + fs.getHeight() - 2);
        }

        // kursor segitiga ala menu RPG
        if (balik && kursorTampil) {
            int cy = ty - fm.getAscent() / 2 - 1;
            int cx = tx - 16;
            if (cx < x + 6) cx = x + 6;
            g.setColor(tinta);
            for (int i = 0; i < 3; i++) g.fillRect(cx + i * 2, cy - 5 + i * 2, 2, 10 - i * 4);
        }
        g.dispose();
    }
}

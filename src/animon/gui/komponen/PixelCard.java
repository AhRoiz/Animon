package animon.gui.komponen;

import animon.gui.tema.Tema;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Kartu opsi interaktif dengan label rarity fungsional, respons tekan 2px, dan dukungan Enter/Space.
 * Saat nonaktif, kartu menampilkan dither dan stempel status ("DIAMBIL", "DILEWATI", "TERKUNCI").
 */
public final class PixelCard extends JComponent implements Accessible {

    private Icon icon;
    private String judul;
    private final List<String> baris = new ArrayList<>();
    private String badge;
    private String stempel;
    private boolean hover;
    private boolean tekan;
    private boolean terpilih;
    private boolean aktif = true;
    private Runnable aksi;
    private Dimension ukuranPilihan = new Dimension(200, 140);

    public PixelCard(String judul) {
        this.judul = judul;
        setOpaque(false);
        setFocusable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        getAccessibleContext().setAccessibleName(judul);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                tekan = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (aktif) {
                    tekan = true;
                    requestFocusInWindow();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (tekan) {
                    tekan = false;
                    repaint();
                    if (hover) klik();
                }
            }
        });
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
        getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "klik");
        getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "klik");
        getActionMap().put("klik", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                klik();
            }
        });
    }

    private void klik() {
        if (aktif && aksi != null) aksi.run();
    }

    public PixelCard icon(Icon i) { this.icon = i; repaint(); return this; }
    public PixelCard baris(String s) { baris.add(s); repaint(); return this; }
    public PixelCard badge(String s) { this.badge = s; repaint(); return this; }
    public PixelCard aksi(Runnable r) { this.aksi = r; return this; }
    public PixelCard ukuran(int w, int h) { this.ukuranPilihan = new Dimension(w, h); return this; }

    public void setJudul(String j) {
        this.judul = j;
        getAccessibleContext().setAccessibleName(j);
        repaint();
    }

    public void setTerpilih(boolean t) {
        this.terpilih = t;
        repaint();
    }

    public boolean isTerpilih() {
        return terpilih;
    }

    /** Nonaktifkan kartu dan beri stempel teks miring di tengah (mis. "DIAMBIL"). */
    public void nonaktif(String stempel) {
        this.aktif = false;
        this.stempel = stempel;
        setCursor(Cursor.getDefaultCursor());
        repaint();
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
                @Override
                public AccessibleRole getAccessibleRole() {
                    return AccessibleRole.PUSH_BUTTON;
                }
            };
        }
        return accessibleContext;
    }

    @Override
    public Dimension getPreferredSize() {
        return ukuranPilihan;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Tema.pixelated(g);
        int w = getWidth() - 4;
        int h = getHeight() - 4;
        int geser = (aktif && tekan) ? 2 : 0;
        boolean balik = aktif && (hover || isFocusOwner());
        Color latar = !aktif ? Tema.MUDA : balik ? Tema.GELAP : Tema.TERANG;
        Color tinta = !aktif ? Tema.TUA : balik ? Tema.TERANG : Tema.GELAP;
        Color redup = !aktif ? Tema.TUA : balik ? Tema.MUDA : Tema.GELAP;

        if (geser == 0) {
            g.setColor(Tema.TUA);
            g.fillRect(4, 4, w, h);
        }
        g.translate(geser, geser);
        g.setColor(Tema.GELAP);
        g.fillRect(2, 0, w - 4, h);
        g.fillRect(0, 2, w, h - 4);
        g.setColor(latar);
        int tb = terpilih ? 6 : 3;
        g.fillRect(tb, tb, w - tb * 2, h - tb * 2);
        if (terpilih) {
            g.setColor(Tema.MUDA);
            g.drawRect(tb - 2, tb - 2, w - (tb - 2) * 2 - 1, h - (tb - 2) * 2 - 1);
        }

        int x = 14;
        int y = 12;
        if (icon != null) {
            int ix = (w - icon.getIconWidth()) / 2;
            icon.paintIcon(this, g, ix, y);
            y += icon.getIconHeight() + 4;
        }

        g.setColor(tinta);
        g.setFont(Tema.judul(10f));
        FontMetrics fm = g.getFontMetrics();
        String j = potong(judul, fm, w - 2 * x - (badge != null && icon == null ? 50 : 0));
        int jx = icon != null ? (w - fm.stringWidth(j)) / 2 : x;
        g.drawString(j, jx, y + fm.getAscent());
        y += fm.getHeight() + 6;

        g.setFont(Tema.teks(19f));
        FontMetrics ft = g.getFontMetrics();
        for (int i = 0; i < baris.size(); i++) {
            String s = baris.get(i);
            g.setColor(i == 0 ? tinta : redup);
            for (String potongan : bungkus(s, ft, w - 2 * x)) {
                if (y + ft.getAscent() > h - 6) break;
                int bx = icon != null ? (w - ft.stringWidth(potongan)) / 2 : x;
                g.drawString(potongan, bx, y + ft.getAscent());
                y += ft.getHeight() - 4;
            }
        }

        if (badge != null) {
            g.setFont(Tema.judul(8f));
            FontMetrics fb = g.getFontMetrics();
            int bw = fb.stringWidth(badge) + 10;
            int bx = w - bw - 8;
            g.setColor(balik ? Tema.TERANG : Tema.GELAP);
            g.fillRect(bx, 8, bw, fb.getHeight() + 4);
            g.setColor(balik ? Tema.GELAP : Tema.TERANG);
            g.drawString(badge, bx + 5, 10 + fb.getAscent());
        }

        if (!aktif) Tema.dither(g, 3, 3, w - 6, h - 6, Tema.TERANG, 6);
        if (stempel != null) {
            g.setFont(Tema.judul(14f));
            FontMetrics fs = g.getFontMetrics();
            int sw = fs.stringWidth(stempel) + 20;
            int sx = (w - sw) / 2;
            int sy = (h - fs.getHeight()) / 2 - 4;
            g.setColor(Tema.GELAP);
            g.fillRect(sx, sy, sw, fs.getHeight() + 10);
            g.setColor(Tema.TERANG);
            g.drawString(stempel, sx + 10, sy + 5 + fs.getAscent());
        }
        g.dispose();
    }

    private static String potong(String s, FontMetrics fm, int maks) {
        if (fm.stringWidth(s) <= maks) return s;
        while (s.length() > 1 && fm.stringWidth(s + "..") > maks) s = s.substring(0, s.length() - 1);
        return s + "..";
    }

    /** Word-wrap sederhana. */
    public static List<String> bungkus(String s, FontMetrics fm, int maks) {
        List<String> hasil = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String kata : s.split(" ")) {
            String coba = cur.length() == 0 ? kata : cur + " " + kata;
            if (fm.stringWidth(coba) > maks && cur.length() > 0) {
                hasil.add(cur.toString());
                cur = new StringBuilder(kata);
            } else {
                cur = new StringBuilder(coba);
            }
        }
        if (cur.length() > 0) hasil.add(cur.toString());
        return hasil;
    }
}

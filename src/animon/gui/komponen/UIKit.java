package animon.gui.komponen;

import animon.gui.tema.Tema;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.util.function.Function;

/** Pabrik komponen Swing yang sudah diberi gaya Game Boy (supaya panel tidak berisi styling ad-hoc). */
public final class UIKit {

    private UIKit() {
    }

    /** Panel transparan dengan layout tertentu. */
    public static JPanel panel(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setOpaque(false);
        return p;
    }

    /** Kotak dialog berbingkai pixel dengan latar terang. */
    public static JPanel kotak(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(Tema.TERANG);
        p.setOpaque(true);
        p.setBorder(new PixelBorder());
        return p;
    }

    /** Kotak dengan judul berlabel gelap di bagian atas. */
    public static JPanel kotakBerjudul(String judul, JComponent isi) {
        JPanel p = kotak(new BorderLayout(0, 8));
        p.add(labelJudulInvers(judul), BorderLayout.NORTH);
        p.add(isi, BorderLayout.CENTER);
        return p;
    }

    public static JLabel label(String teks, float ukuran) {
        JLabel l = new JLabel(teks);
        l.setFont(Tema.teks(ukuran));
        l.setForeground(Tema.GELAP);
        return l;
    }

    public static JLabel judul(String teks, float ukuran) {
        JLabel l = new JLabel(teks);
        l.setFont(Tema.judul(ukuran));
        l.setForeground(Tema.GELAP);
        return l;
    }

    /** Label teks terang di atas pita gelap (header seksi). */
    public static JLabel labelJudulInvers(String teks) {
        JLabel l = new JLabel(teks, SwingConstants.LEFT) {
            @Override
            protected void paintComponent(Graphics g) {
                Tema.pixelated((Graphics2D) g);
                super.paintComponent(g);
            }
        };
        l.setFont(Tema.judul(10f));
        l.setForeground(Tema.TERANG);
        l.setBackground(Tema.GELAP);
        l.setOpaque(true);
        l.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        return l;
    }

    public static JTextField textField(String isi) {
        JTextField tf = new JTextField(isi);
        tf.setFont(Tema.judul(14f));
        tf.setForeground(Tema.GELAP);
        tf.setBackground(Tema.TERANG);
        tf.setCaretColor(Tema.GELAP);
        tf.setSelectionColor(Tema.GELAP);
        tf.setSelectedTextColor(Tema.TERANG);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, Tema.GELAP),
                BorderFactory.createEmptyBorder(8, 6, 6, 6)));
        tf.setOpaque(false);
        return tf;
    }

    public static JScrollPane scroll(JComponent isi) {
        JScrollPane sp = new JScrollPane(isi);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Tema.TERANG);
        sp.setBackground(Tema.TERANG);
        sp.getVerticalScrollBar().setUI(new PixelScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new PixelScrollBarUI());
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getVerticalScrollBar().setOpaque(false);
        return sp;
    }

    /** JList bergaya pixel dengan renderer dua baris (judul + keterangan). */
    public static <T> JList<T> list(Function<T, String[]> isi) {
        JList<T> l = new JList<>();
        l.setBackground(Tema.TERANG);
        l.setForeground(Tema.GELAP);
        l.setSelectionBackground(Tema.GELAP);
        l.setSelectionForeground(Tema.TERANG);
        l.setCellRenderer(renderer(isi));
        return l;
    }

    public static <T> ListCellRenderer<T> renderer(Function<T, String[]> isi) {
        return (list, value, index, selected, focus) -> {
            JPanel p = new JPanel(new BorderLayout(8, 0));
            Color bg = selected ? Tema.GELAP : (index % 2 == 0 ? Tema.TERANG : new Color(0x94b40f));
            Color fg = selected ? Tema.TERANG : Tema.GELAP;
            p.setBackground(bg);
            p.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, Tema.MUDA),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            String[] s = value == null ? new String[] {"-"} : isi.apply(value);
            JLabel atas = new JLabel(s[0]);
            atas.setFont(Tema.judul(10f));
            atas.setForeground(fg);
            p.add(atas, BorderLayout.NORTH);
            if (s.length > 1 && s[1] != null) {
                JLabel bawah = new JLabel(s[1]);
                bawah.setFont(Tema.teks(19f));
                bawah.setForeground(selected ? Tema.MUDA : Tema.TUA);
                p.add(bawah, BorderLayout.CENTER);
            }
            if (s.length > 2 && s[2] != null) {
                JLabel kanan = new JLabel(s[2]);
                kanan.setFont(Tema.judul(10f));
                kanan.setForeground(fg);
                p.add(kanan, BorderLayout.EAST);
            }
            return (Component) p;
        };
    }
}

package animon.gui.komponen;

import animon.gui.tema.Tema;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;

/**
 * Dialog modal bergaya Game Boy (pengganti JOptionPane yang tampilannya tidak menyatu dengan tema).
 * Karena modal, method-method di sini memblokir sampai pemain memilih — aman dipanggil dari
 * callback sinkron seperti {@code PenanganInventoryPenuh}.
 */
public final class DialogPixel {

    private DialogPixel() {
    }

    /** @return indeks opsi yang dipilih, atau -1 bila ditutup dengan ESC. */
    public static int pilih(Component induk, String judul, String pesan, String... opsi) {
        return pilih(induk, judul, teksArea(pesan, 26), opsi);
    }

    public static int pilih(Component induk, String judul, JComponent isi, String... opsi) {
        Window w = induk == null ? null : SwingUtilities.getWindowAncestor(induk);
        JDialog d = new JDialog(w, judul, JDialog.ModalityType.APPLICATION_MODAL);
        d.setUndecorated(true);
        final int[] hasil = {-1};

        JPanel root = UIKit.kotak(new BorderLayout(0, 14));
        root.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.TUA, 4), new PixelBorder(Tema.GELAP, Tema.TUA, 4, 16)));
        root.add(UIKit.labelJudulInvers(judul), BorderLayout.NORTH);
        root.add(isi, BorderLayout.CENTER);

        JPanel tombol = UIKit.panel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        PixelButton pertama = null;
        for (int i = 0; i < opsi.length; i++) {
            final int idx = i;
            PixelButton b = new PixelButton(opsi[i], null, 11f);
            b.addActionListener(e -> {
                hasil[0] = idx;
                d.dispose();
            });
            tombol.add(b);
            if (pertama == null) pertama = b;
        }
        root.add(tombol, BorderLayout.SOUTH);

        JRootPane rp = d.getRootPane();
        rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "tutup");
        rp.getActionMap().put("tutup", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                d.dispose();
            }
        });
        if (pertama != null) rp.setDefaultButton(pertama);

        d.setContentPane(root);
        d.pack();
        Dimension s = d.getSize();
        d.setSize(Math.max(420, s.width), s.height);
        d.setLocationRelativeTo(w);
        if (pertama != null) {
            PixelButton fokus = pertama;
            SwingUtilities.invokeLater(fokus::requestFocusInWindow);
        }
        d.setVisible(true);
        return hasil[0];
    }

    public static boolean konfirmasi(Component induk, String judul, String pesan) {
        return pilih(induk, judul, pesan, "YA", "TIDAK") == 0;
    }

    public static void info(Component induk, String judul, String pesan) {
        pilih(induk, judul, pesan, "OK");
    }

    /** Dialog teks panjang yang bisa di-scroll (mis. layar INFO pertarungan). */
    public static void teksPanjang(Component induk, String judul, String teks) {
        JTextArea ta = teksArea(teks, 60);
        ta.setFont(Tema.teks(21f));
        ta.setLineWrap(false);
        ta.setCaretPosition(0);
        javax.swing.JScrollPane sp = UIKit.scroll(ta);
        sp.setPreferredSize(new Dimension(760, 480));
        pilih(induk, judul, sp, "TUTUP");
    }

    private static JTextArea teksArea(String pesan, int kolom) {
        JTextArea ta = new JTextArea(pesan);
        ta.setEditable(false);
        ta.setFocusable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setOpaque(false);
        ta.setFont(Tema.teks(23f));
        ta.setForeground(Tema.GELAP);
        ta.setColumns(kolom);
        ta.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        // Pra-ukur lebar agar JTextArea ber-wrap menghitung tinggi yang benar saat pack().
        int lebar = Math.min(kolom * ta.getFontMetrics(ta.getFont()).charWidth('m'),
                ta.getFontMetrics(ta.getFont()).stringWidth(pesan) + 16);
        ta.setSize(Math.max(320, lebar), Short.MAX_VALUE);
        return ta;
    }
}

package animon.gui.panel;

import animon.gui.komponen.PixelBorder;
import animon.gui.komponen.PixelButton;
import animon.gui.komponen.UIKit;
import animon.gui.tema.Tema;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;

/**
 * Layar penutup penuh antar-giliran hotseat (Wireframe L.3).
 * Latar hijau gelap pekat menyembunyikan status tim dan item dari pandangan lawan.
 */
public final class BlindTransitionPanel extends JPanel {

    private final JLabel lblUtama;
    private final JLabel lblInstruksi;
    private final JLabel lblKonteks;
    private final PixelButton btnSiap;
    private Runnable aksiLanjut;

    public BlindTransitionPanel() {
        super(new GridBagLayout());
        setBackground(Tema.GELAP);
        setOpaque(true);

        JPanel kartu = new JPanel(new BorderLayout(0, 18));
        kartu.setBackground(Tema.GELAP);
        kartu.setOpaque(true);
        kartu.setBorder(BorderFactory.createCompoundBorder(
                new PixelBorder(Tema.TERANG, Tema.MUDA, 4, 24),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)));
        kartu.setPreferredSize(new Dimension(680, 320));

        lblUtama = UIKit.judul("GILIRAN PLAYER 1 SELESAI.", 16f);
        lblUtama.setForeground(Tema.TERANG);
        lblUtama.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel tengah = UIKit.panel(new GridBagLayout());
        GridBagConstraints tc = new GridBagConstraints();
        tc.gridx = 0;
        tc.gridy = 0;
        tc.insets = new Insets(6, 8, 6, 8);

        lblInstruksi = UIKit.label("Serahkan perangkat kepada pemain berikutnya.", 26f);
        lblInstruksi.setForeground(Tema.TERANG);
        lblInstruksi.setHorizontalAlignment(SwingConstants.CENTER);
        tengah.add(lblInstruksi, tc);

        tc.gridy = 1;
        lblKonteks = UIKit.label("", 22f);
        lblKonteks.setForeground(Tema.MUDA);
        lblKonteks.setHorizontalAlignment(SwingConstants.CENTER);
        tengah.add(lblKonteks, tc);

        btnSiap = new PixelButton("SIAP!", "Buka layar giliran sekarang", 16f);
        btnSiap.setMnemonic(KeyEvent.VK_S);
        btnSiap.setPreferredSize(new Dimension(260, 68));
        btnSiap.addActionListener(e -> {
            if (aksiLanjut != null) {
                Runnable r = aksiLanjut;
                aksiLanjut = null;
                r.run();
            }
        });

        JPanel bawah = UIKit.panel(new GridBagLayout());
        bawah.add(btnSiap);

        kartu.add(lblUtama, BorderLayout.NORTH);
        kartu.add(tengah, BorderLayout.CENTER);
        kartu.add(bawah, BorderLayout.SOUTH);

        add(kartu);
    }

    public void tampilkan(String judulUtama, String instruksi, String konteks, Runnable saatSiap) {
        lblUtama.setText(judulUtama);
        lblInstruksi.setText(instruksi);
        lblKonteks.setText(konteks == null ? "" : konteks);
        this.aksiLanjut = saatSiap;
        SwingUtilities.invokeLater(btnSiap::requestFocusInWindow);
    }

    public PixelButton getTombolSiap() {
        return btnSiap;
    }
}


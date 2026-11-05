package animon.gui.panel;

import animon.gui.GameManager;
import animon.gui.komponen.PixelBorder;
import animon.gui.komponen.PixelButton;
import animon.gui.komponen.UIKit;
import animon.gui.tema.SpriteLoader;
import animon.gui.tema.Tema;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;

/** Layar Menu Utama (Wireframe L.1): judul utama, pratinjau sprite kartrid, dan 4 menu navigasi. */
public final class MainMenuPanel extends JPanel {

    private final GameManager gameManager;
    private final PixelButton btnNewGame;
    private final PixelButton btnLoadGame;
    private final JLabel lblInfoData;
    private final JLabel lblSpriteKiri;
    private final JLabel lblSpriteKanan;

    public MainMenuPanel(GameManager gameManager) {
        super(new BorderLayout(0, 16));
        this.gameManager = gameManager;
        setBackground(Tema.TERANG);
        setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        JPanel atas = UIKit.kotak(new BorderLayout(0, 8));
        atas.setBackground(Tema.GELAP);
        atas.setBorder(new PixelBorder(Tema.GELAP, Tema.MUDA, 4, 14));

        JLabel lblJudul = UIKit.judul("ANIMON: ELEMENTAL CLASH", 20f);
        lblJudul.setForeground(Tema.TERANG);
        lblJudul.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblSub = UIKit.label("Pertarungan Taktis Giliran 1v1 Hotseat", 24f);
        lblSub.setForeground(Tema.MUDA);
        lblSub.setHorizontalAlignment(SwingConstants.CENTER);

        atas.add(lblJudul, BorderLayout.CENTER);
        atas.add(lblSub, BorderLayout.SOUTH);
        add(atas, BorderLayout.NORTH);

        JPanel tengah = UIKit.panel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 12, 8, 12);
        gc.fill = GridBagConstraints.BOTH;

        JPanel panggung = UIKit.kotak(new BorderLayout(0, 10));
        panggung.add(UIKit.labelJudulInvers("ARENA DUEL ELEMENTAL"), BorderLayout.NORTH);

        JPanel duel = UIKit.panel(new GridLayout(1, 3, 8, 0));
        duel.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        lblSpriteKiri = new JLabel("", SwingConstants.CENTER);
        lblSpriteKanan = new JLabel("", SwingConstants.CENTER);
        JLabel vs = UIKit.judul("VS", 16f);
        vs.setHorizontalAlignment(SwingConstants.CENTER);
        duel.add(lblSpriteKiri);
        duel.add(vs);
        duel.add(lblSpriteKanan);
        panggung.add(duel, BorderLayout.CENTER);

        lblInfoData = UIKit.label("", 20f);
        lblInfoData.setHorizontalAlignment(SwingConstants.CENTER);
        lblInfoData.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, Tema.GELAP),
                BorderFactory.createEmptyBorder(8, 6, 4, 6)));
        panggung.add(lblInfoData, BorderLayout.SOUTH);
        panggung.setPreferredSize(new Dimension(380, 290));

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0.48;
        gc.weighty = 1.0;
        tengah.add(panggung, gc);

        JPanel menuBox = UIKit.kotakBerjudul("PILIH MENU", buatPanelTombol());
        menuBox.setPreferredSize(new Dimension(400, 290));
        gc.gridx = 1;
        gc.weightx = 0.52;
        tengah.add(menuBox, gc);

        add(tengah, BorderLayout.CENTER);

        JLabel kaki = UIKit.label(
                "Navigasi keyboard: Tab untuk pindah tombol, Enter/Spasi untuk memilih, Alt+huruf untuk pintasan.",
                19f);
        kaki.setHorizontalAlignment(SwingConstants.CENTER);
        add(kaki, BorderLayout.SOUTH);

        btnNewGame = tombolTerbuat[0];
        btnLoadGame = tombolTerbuat[1];
        segarkan();
    }

    private PixelButton[] tombolTerbuat;

    private JPanel buatPanelTombol() {
        JPanel p = UIKit.panel(new GridLayout(4, 1, 0, 12));
        p.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        PixelButton bNew = new PixelButton("NEW GAME", "Mulai persiapan 2 Trainer baru", 13f);
        bNew.setHorizontalAlignment(SwingConstants.LEFT);
        bNew.setMnemonic(KeyEvent.VK_N);
        bNew.addActionListener(e -> gameManager.prosesAksi("NEW_GAME"));

        PixelButton bLoad = new PixelButton("LOAD GAME", "Belum ada sesi tersimpan", 13f);
        bLoad.setHorizontalAlignment(SwingConstants.LEFT);
        bLoad.setMnemonic(KeyEvent.VK_L);
        bLoad.addActionListener(e -> gameManager.prosesAksi("LOAD_GAME"));

        PixelButton bHist = new PixelButton("RIWAYAT PERTANDINGAN", "Buka tabel laga & pemutar Replay", 13f);
        bHist.setHorizontalAlignment(SwingConstants.LEFT);
        bHist.setMnemonic(KeyEvent.VK_R);
        bHist.addActionListener(e -> gameManager.prosesAksi("HISTORY"));

        PixelButton bExit = new PixelButton("EXIT", "Keluar dari permainan", 13f);
        bExit.setHorizontalAlignment(SwingConstants.LEFT);
        bExit.setMnemonic(KeyEvent.VK_X);
        bExit.addActionListener(e -> gameManager.prosesAksi("EXIT"));

        p.add(bNew);
        p.add(bLoad);
        p.add(bHist);
        p.add(bExit);
        tombolTerbuat = new PixelButton[] {bNew, bLoad, bHist, bExit};
        return p;
    }

    public void segarkan() {
        lblSpriteKiri.setIcon(SpriteLoader.icon("Naga Merapi", true, 96));
        lblSpriteKiri.setToolTipText("Naga Merapi (DPS Api)");
        lblSpriteKiri.getAccessibleContext().setAccessibleName("Sprite Naga Merapi");
        lblSpriteKanan.setIcon(SpriteLoader.icon("Naga Gletser", true, 96));
        lblSpriteKanan.setToolTipText("Naga Gletser (Tank Ice)");
        lblSpriteKanan.getAccessibleContext().setAccessibleName("Sprite Naga Gletser");

        lblInfoData.setText("Database aktif: " + gameManager.getJumlahKatalogHewan() + " Animon, "
                + gameManager.getJumlahKatalogItem() + " Item (JSON)");
        String simpanan = gameManager.getRingkasanSimpanan();
        btnLoadGame.setSubTeks(simpanan != null ? "Lanjutkan: " + simpanan : "Belum ada sesi tersimpan");
    }

    public void fokusAwal() {
        btnNewGame.requestFocusInWindow();
    }
}

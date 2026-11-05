package animon.gui;

import animon.data.GameSeeder;
import animon.gui.komponen.DialogPixel;
import animon.gui.komponen.LcdOverlay;
import animon.gui.komponen.PixelButton;
import animon.gui.komponen.UIKit;
import animon.gui.panel.BattlePanel;
import animon.gui.panel.BlindTransitionPanel;
import animon.gui.panel.DraftPanel;
import animon.gui.panel.MainMenuPanel;
import animon.gui.panel.SetupPanel;
import animon.gui.panel.ShopPanel;
import animon.gui.tema.SpriteLoader;
import animon.gui.tema.Tema;
import animon.item.Item;
import animon.model.Trainer;
import animon.sistem.Shop;
import animon.util.Acak;
import animon.util.JsonException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

/**
 * Jendela utama GUI ANIMON berbasis {@link CardLayout} untuk seluruh fase permainan.
 * Menyediakan pita kontrol palet sprite, garis piksel LCD, dan peredam gerak di bagian atas.
 */
public final class MainFrame extends JFrame {

    public static final String LAYAR_MENU = "MENU";
    public static final String LAYAR_SETUP = "SETUP";
    public static final String LAYAR_DRAFT = "DRAFT";
    public static final String LAYAR_TRANSISI = "TRANSISI";
    public static final String LAYAR_BATTLE = "BATTLE";
    public static final String LAYAR_SHOP = "SHOP";

    private final GameManager gameManager;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel dekPanel;
    private final LcdOverlay lcdOverlay = new LcdOverlay();

    private final MainMenuPanel mainMenuPanel;
    private final SetupPanel setupPanel;
    private final DraftPanel draftPanel;
    private final BlindTransitionPanel transitionPanel;
    private final BattlePanel battlePanel;
    private final ShopPanel shopPanel;

    private final JLabel lblLokasiLayar;
    private final PixelButton btnKeMenu;
    private final PixelButton btnModePalet;
    private final PixelButton btnGridLcd;
    private final PixelButton btnGerak;

    private String layarAktif = LAYAR_MENU;

    public MainFrame(GameManager gameManager) {
        super("ANIMON: Elemental Clash");
        this.gameManager = gameManager;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Tema.TERANG);

        // Bar atas kartrid: indikator layar + pengatur tampilan/gerak
        JPanel barAtas = new JPanel(new BorderLayout(12, 0));
        barAtas.setBackground(Tema.GELAP);
        barAtas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, Tema.TUA),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));

        lblLokasiLayar = UIKit.judul("ANIMON DMG-01  ·  MENU UTAMA", 10f);
        lblLokasiLayar.setForeground(Tema.TERANG);
        barAtas.add(lblLokasiLayar, BorderLayout.WEST);

        JPanel kananBar = UIKit.panel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnKeMenu = buatTombolBar("MENU UTAMA", () -> gameManager.prosesAksi("KEMBALI_MENU"));
        btnModePalet = buatTombolBar("SPRITE: ASLI", this::togglePaletSprite);
        btnGridLcd = buatTombolBar("LCD: ON", this::toggleGridLcd);
        btnGerak = buatTombolBar(Tema.isAnimasiAktif() ? "GERAK: ON" : "GERAK: OFF", this::toggleGerak);

        kananBar.add(btnKeMenu);
        kananBar.add(btnModePalet);
        kananBar.add(btnGridLcd);
        kananBar.add(btnGerak);
        barAtas.add(kananBar, BorderLayout.EAST);
        root.add(barAtas, BorderLayout.NORTH);

        dekPanel = new JPanel(cardLayout);
        dekPanel.setBackground(Tema.TERANG);

        mainMenuPanel = new MainMenuPanel(gameManager);
        setupPanel = new SetupPanel(gameManager);
        draftPanel = new DraftPanel();
        transitionPanel = new BlindTransitionPanel();
        battlePanel = new BattlePanel(gameManager);
        shopPanel = new ShopPanel(gameManager);

        dekPanel.add(mainMenuPanel, LAYAR_MENU);
        dekPanel.add(setupPanel, LAYAR_SETUP);
        dekPanel.add(draftPanel, LAYAR_DRAFT);
        dekPanel.add(transitionPanel, LAYAR_TRANSISI);
        dekPanel.add(battlePanel, LAYAR_BATTLE);
        dekPanel.add(shopPanel, LAYAR_SHOP);
        root.add(dekPanel, BorderLayout.CENTER);

        setContentPane(root);
        setGlassPane(lcdOverlay);
        lcdOverlay.setVisible(true);

        setSize(1040, 740);
        setMinimumSize(new Dimension(920, 660));
        setLocationRelativeTo(null);
        perbaruiBarAtas();
    }

    private static PixelButton buatTombolBar(String teks, Runnable aksi) {
        PixelButton b = new PixelButton(teks, null, 8f).gaya(PixelButton.Gaya.GELAP);
        b.setMargin(new Insets(4, 12, 4, 10));
        b.addActionListener(e -> aksi.run());
        return b;
    }

    private void togglePaletSprite() {
        boolean baru = !SpriteLoader.isModeGameBoy();
        SpriteLoader.setModeGameBoy(baru);
        btnModePalet.setText(baru ? "SPRITE: DMG" : "SPRITE: ASLI");
        mainMenuPanel.segarkan();
        repaint();
    }

    private void toggleGridLcd() {
        boolean baru = !lcdOverlay.isGridAktif();
        lcdOverlay.setGridAktif(baru);
        btnGridLcd.setText(baru ? "LCD: ON" : "LCD: OFF");
    }

    private void toggleGerak() {
        boolean baru = !Tema.isAnimasiAktif();
        Tema.setAnimasiAktif(baru);
        btnGerak.setText(baru ? "GERAK: ON" : "GERAK: OFF");
    }

    public BattlePanel getBattlePanel() {
        return battlePanel;
    }

    public void tampilkanLayar(String namaKartu) {
        if (namaKartu.equals(layarAktif)) {
            perbaruiBarAtas();
            return;
        }
        lcdOverlay.transisi(() -> {
            layarAktif = namaKartu;
            cardLayout.show(dekPanel, namaKartu);
            if (LAYAR_TRANSISI.equals(namaKartu)) {
                getRootPane().setDefaultButton(transitionPanel.getTombolSiap());
            } else {
                getRootPane().setDefaultButton(null);
            }
            perbaruiBarAtas();
        });
    }

    private void perbaruiBarAtas() {
        String nama;
        switch (layarAktif) {
            case LAYAR_SETUP:    nama = "PERSIAPAN TRAINER"; break;
            case LAYAR_DRAFT:    nama = "PILIHAN ITEM"; break;
            case LAYAR_TRANSISI: nama = "PERGANTIAN GILIRAN"; break;
            case LAYAR_BATTLE:   nama = "ARENA PERTEMPURAN"; break;
            case LAYAR_SHOP:     nama = "TOKO ANIMON"; break;
            default:             nama = "MENU UTAMA"; break;
        }
        lblLokasiLayar.setText("ANIMON DMG-01  ·  " + nama);
        btnKeMenu.setVisible(!LAYAR_MENU.equals(layarAktif) && !LAYAR_TRANSISI.equals(layarAktif));
    }

    public void kembaliKeMenuUtama() {
        mainMenuPanel.segarkan();
        tampilkanLayar(LAYAR_MENU);
        SwingUtilities.invokeLater(mainMenuPanel::fokusAwal);
    }

    public void bukaSetup() {
        setupPanel.siapkanBaru();
        tampilkanLayar(LAYAR_SETUP);
    }

    public void tampilkanTransisi(String judul, String instruksi, String konteks, Runnable saatSiap) {
        // Pindah seketika ke layar gelap penuh agar layar sebelumnya langsung tertutup rapat.
        layarAktif = LAYAR_TRANSISI;
        cardLayout.show(dekPanel, LAYAR_TRANSISI);
        getRootPane().setDefaultButton(transitionPanel.getTombolSiap());
        perbaruiBarAtas();
        transitionPanel.tampilkan(judul, instruksi, konteks, saatSiap);
    }

    public void bukaDraft(String judulMode, Trainer trainer, List<Item> opsi, int jumlahPilih, Runnable selesai) {
        draftPanel.buka(judulMode, trainer, opsi, jumlahPilih, selesai);
        tampilkanLayar(LAYAR_DRAFT);
    }

    public void bukaToko(Trainer trainer, Shop shop, String pesanAwal, Runnable selesai) {
        shopPanel.buka(trainer, shop, pesanAwal, selesai);
        tampilkanLayar(LAYAR_SHOP);
    }

    /** Entry point GUI ANIMON. Mendukung --data &lt;folder&gt;, --seed &lt;angka&gt;, dan --no-motion. */
    public static void main(String[] args) {
        String folderArg = null;
        Long seed = null;
        boolean gerak = true;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--data":
                    if (i + 1 < args.length) folderArg = args[++i];
                    break;
                case "--seed":
                    if (i + 1 < args.length) seed = Long.parseLong(args[++i]);
                    break;
                case "--no-motion":
                    gerak = false;
                    break;
                default:
                    break;
            }
        }
        if (seed != null) Acak.set(new Random(seed));
        final String folderFinal = folderArg;
        final boolean gerakFinal = gerak;

        SwingUtilities.invokeLater(() -> {
            Tema.pasang();
            Tema.setAnimasiAktif(gerakFinal);
            try {
                Path folder = cariFolderData(folderFinal);
                GameSeeder seeder = GameSeeder.dariFolder(folder);
                GameManager gm = new GameManager(seeder, folder);
                MainFrame mf = new MainFrame(gm);
                gm.pasangFrame(mf);
                mf.mainMenuPanel.segarkan();
                mf.setVisible(true);
                mf.mainMenuPanel.fokusAwal();
            } catch (IOException | JsonException ex) {
                DialogPixel.info(null, "GAGAL MEMUAT DATA",
                        "Tidak dapat membaca database JSON: " + ex.getMessage());
            }
        });
    }

    private static Path cariFolderData(String folderArg) throws IOException {
        if (folderArg != null) {
            Path p = Paths.get(folderArg);
            if (!Files.isDirectory(p)) throw new IOException("Folder data tidak ditemukan: " + p.toAbsolutePath());
            return p;
        }
        for (String kandidat : new String[] {"data", "../data"}) {
            Path p = Paths.get(kandidat);
            if (Files.isRegularFile(p.resolve(GameSeeder.FILE_HEWAN))) return p;
        }
        throw new IOException("Folder data berisi " + GameSeeder.FILE_HEWAN + " tidak ditemukan.");
    }
}

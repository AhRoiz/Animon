package animon.gui;

import animon.gui.komponen.PixelBar;
import animon.gui.komponen.PixelButton;
import animon.gui.komponen.UIKit;
import animon.gui.riwayat.LangkahReplay;
import animon.gui.riwayat.RekamanPertandingan;
import animon.gui.riwayat.RiwayatRepository;
import animon.gui.tema.SpriteLoader;
import animon.gui.tema.Tema;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Jendela terpisah Riwayat Pertandingan & Replay langkah demi langkah (Wireframe L.7).
 * Menyediakan tiga state data eksplisit (memuat, kosong, gagal) dan kontrol Timer yang bisa dijeda.
 */
public final class HistoryFrame extends JFrame {

    private static final String STATE_MEMUAT = "MEMUAT";
    private static final String STATE_KOSONG = "KOSONG";
    private static final String STATE_ERROR = "ERROR";
    private static final String STATE_DAFTAR = "DAFTAR";

    private final RiwayatRepository repository;
    private final JLabel lblSumber;
    private final CardLayout cardKiri = new CardLayout();
    private final JPanel wadahKiri;
    private final JLabel lblPesanError;
    private final DefaultListModel<RekamanPertandingan> modelPertandingan = new DefaultListModel<>();
    private final JList<RekamanPertandingan> listPertandingan;

    private final JLabel lblJudulReplay;
    private final JLabel lblInfoLangkah;
    private final JLabel lblSpritePelaku;
    private final JLabel lblNamaPelaku;
    private final JLabel lblHpPelaku;
    private final PixelBar barHpPelaku;
    private final JLabel lblSpriteTarget;
    private final JLabel lblNamaTarget;
    private final JLabel lblHpTarget;
    private final PixelBar barHpTarget;
    private final JTextArea txtLogLangkah;

    private final PixelButton btnPrev;
    private final PixelButton btnNext;
    private final PixelButton btnAuto;
    private final Timer timerAuto;

    private List<LangkahReplay> langkahAktif = new ArrayList<>();
    private int indeksLangkah = -1;

    public HistoryFrame(Window pemilik, RiwayatRepository repository) {
        super("ANIMON - Riwayat & Replay Pertandingan");
        this.repository = repository;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(16, 12));
        root.setBackground(Tema.TERANG);
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel kiri = UIKit.panel(new BorderLayout(0, 8));
        lblSumber = UIKit.label("Memeriksa koneksi database...", 19f);
        kiri.add(lblSumber, BorderLayout.NORTH);

        wadahKiri = UIKit.panel(cardKiri);

        JLabel lblMemuat = UIKit.label("Memuat daftar riwayat pertandingan...", 22f);
        lblMemuat.setHorizontalAlignment(SwingConstants.CENTER);
        wadahKiri.add(lblMemuat, STATE_MEMUAT);

        JLabel lblKosong = UIKit.label(
                "<html><center>Belum ada riwayat pertandingan.<br>"
                        + "Selesaikan satu laga lewat NEW GAME agar rekamannya tersimpan di sini.</center></html>",
                21f);
        lblKosong.setHorizontalAlignment(SwingConstants.CENTER);
        wadahKiri.add(lblKosong, STATE_KOSONG);

        JPanel panelError = UIKit.panel(new BorderLayout(0, 8));
        lblPesanError = UIKit.label("", 20f);
        lblPesanError.setHorizontalAlignment(SwingConstants.CENTER);
        panelError.add(lblPesanError, BorderLayout.CENTER);
        wadahKiri.add(panelError, STATE_ERROR);

        listPertandingan = UIKit.list(r -> new String[] {
                "#" + r.getId() + "  " + r.getTrainer1() + " vs " + r.getTrainer2(),
                "Pemenang: " + r.getPemenang() + (r.isMenyerah() ? " (menyerah)" : "")
                        + " · " + r.getLangkahList().size() + " langkah · " + r.getWaktu(),
                r.getTotalRonde() + " Rnd"
        });
        listPertandingan.setModel(modelPertandingan);
        listPertandingan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listPertandingan.getAccessibleContext().setAccessibleName("Daftar riwayat pertandingan");
        listPertandingan.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) pilihPertandingan(listPertandingan.getSelectedValue());
        });
        JScrollPane spList = UIKit.scroll(listPertandingan);
        spList.setBorder(BorderFactory.createLineBorder(Tema.GELAP, 2));
        wadahKiri.add(spList, STATE_DAFTAR);

        kiri.add(wadahKiri, BorderLayout.CENTER);

        PixelButton btnSegarkan = new PixelButton("MUAT ULANG", null, 10f);
        btnSegarkan.setMnemonic(KeyEvent.VK_U);
        btnSegarkan.addActionListener(e -> muatData());
        kiri.add(btnSegarkan, BorderLayout.SOUTH);

        JPanel kotakKiri = UIKit.kotakBerjudul("DAFTAR PERTANDINGAN", kiri);
        kotakKiri.setPreferredSize(new Dimension(370, 540));
        root.add(kotakKiri, BorderLayout.WEST);

        JPanel kanan = UIKit.panel(new BorderLayout(0, 10));

        JPanel infoAtas = UIKit.panel(new GridLayout(2, 1, 0, 2));
        lblJudulReplay = UIKit.judul("Pilih satu pertandingan di daftar sebelah kiri", 11f);
        lblInfoLangkah = UIKit.label("Langkah -/-", 21f);
        infoAtas.add(lblJudulReplay);
        infoAtas.add(lblInfoLangkah);
        kanan.add(infoAtas, BorderLayout.NORTH);

        JPanel panggung = UIKit.panel(new BorderLayout(0, 10));

        JPanel duaPetarung = UIKit.panel(new GridLayout(1, 2, 12, 0));

        lblSpritePelaku = new JLabel("", SwingConstants.CENTER);
        lblNamaPelaku = UIKit.judul("PELAKU: -", 10f);
        lblHpPelaku = UIKit.label("HP: -/-", 20f);
        barHpPelaku = new PixelBar(PixelBar.Jenis.HP);
        duaPetarung.add(buatKartuReplay("PELAKU AKSI", lblSpritePelaku, lblNamaPelaku, barHpPelaku, lblHpPelaku));

        lblSpriteTarget = new JLabel("", SwingConstants.CENTER);
        lblNamaTarget = UIKit.judul("TARGET: -", 10f);
        lblHpTarget = UIKit.label("HP: -/-", 20f);
        barHpTarget = new PixelBar(PixelBar.Jenis.HP);
        duaPetarung.add(buatKartuReplay("TARGET / LAWAN", lblSpriteTarget, lblNamaTarget, barHpTarget, lblHpTarget));

        panggung.add(duaPetarung, BorderLayout.NORTH);

        txtLogLangkah = new JTextArea();
        txtLogLangkah.setEditable(false);
        txtLogLangkah.setLineWrap(true);
        txtLogLangkah.setWrapStyleWord(true);
        txtLogLangkah.setFont(Tema.teks(22f));
        txtLogLangkah.setBackground(Tema.TERANG);
        txtLogLangkah.setForeground(Tema.GELAP);
        txtLogLangkah.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        txtLogLangkah.getAccessibleContext().setAccessibleName("Log langkah replay");
        txtLogLangkah.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                txtLogLangkah.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Tema.GELAP, 2),
                        BorderFactory.createEmptyBorder(6, 8, 6, 8)));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                txtLogLangkah.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            }
        });

        JScrollPane spLog = UIKit.scroll(txtLogLangkah);
        spLog.setBorder(BorderFactory.createLineBorder(Tema.GELAP, 2));
        panggung.add(spLog, BorderLayout.CENTER);

        kanan.add(panggung, BorderLayout.CENTER);

        JPanel kontrol = UIKit.panel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPrev = new PixelButton("< PREV", null, 11f);
        btnPrev.setMnemonic(KeyEvent.VK_P);
        btnPrev.addActionListener(e -> {
            hentikanAuto();
            tampilkanLangkah(indeksLangkah - 1);
        });

        btnNext = new PixelButton("NEXT >", null, 11f);
        btnNext.setMnemonic(KeyEvent.VK_N);
        btnNext.addActionListener(e -> {
            hentikanAuto();
            tampilkanLangkah(indeksLangkah + 1);
        });

        btnAuto = new PixelButton("AUTO PLAY", null, 11f);
        btnAuto.setMnemonic(KeyEvent.VK_A);
        btnAuto.addActionListener(e -> toggleAutoPlay());

        PixelButton btnTutup = new PixelButton("TUTUP", null, 11f);
        btnTutup.setMnemonic(KeyEvent.VK_T);
        btnTutup.addActionListener(e -> tutupJendela());

        kontrol.add(btnPrev);
        kontrol.add(btnNext);
        kontrol.add(btnAuto);
        kontrol.add(btnTutup);
        kanan.add(kontrol, BorderLayout.SOUTH);

        root.add(UIKit.kotakBerjudul("PEMUTAR REPLAY LANGKAH", kanan), BorderLayout.CENTER);

        timerAuto = new Timer(1350, e -> {
            if (indeksLangkah + 1 < langkahAktif.size()) {
                tampilkanLangkah(indeksLangkah + 1);
            } else {
                hentikanAuto();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "tutupHistory");
        getRootPane().getActionMap().put("tutupHistory", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tutupJendela();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                hentikanAuto();
            }
        });

        setContentPane(root);
        setSize(980, 640);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(pemilik);

        perbaruiTombolKontrol();
        muatData();
    }

    private static JPanel buatKartuReplay(String peran, JLabel sprite, JLabel nama, PixelBar bar, JLabel teksHp) {
        JPanel isi = UIKit.panel(new BorderLayout(10, 6));
        sprite.setPreferredSize(new Dimension(80, 80));
        isi.add(sprite, BorderLayout.WEST);

        JPanel kanan = UIKit.panel(new GridLayout(3, 1, 0, 4));
        kanan.add(nama);
        kanan.add(bar);
        kanan.add(teksHp);
        isi.add(kanan, BorderLayout.CENTER);

        return UIKit.kotakBerjudul(peran, isi);
    }

    public void muatData() {
        hentikanAuto();
        cardKiri.show(wadahKiri, STATE_MEMUAT);
        lblSumber.setText("Memuat riwayat pertandingan...");

        new SwingWorker<List<RekamanPertandingan>, Void>() {
            @Override
            protected List<RekamanPertandingan> doInBackground() throws Exception {
                return repository.muatSemua();
            }

            @Override
            protected void done() {
                try {
                    List<RekamanPertandingan> hasil = get();
                    lblSumber.setText("Sumber: " + repository.getInfoSumber());
                    modelPertandingan.clear();
                    for (RekamanPertandingan r : hasil) modelPertandingan.addElement(r);

                    if (modelPertandingan.isEmpty()) {
                        cardKiri.show(wadahKiri, STATE_KOSONG);
                        kosongkanReplay();
                    } else {
                        cardKiri.show(wadahKiri, STATE_DAFTAR);
                        listPertandingan.setSelectedIndex(0);
                    }
                } catch (Exception ex) {
                    lblSumber.setText("Gagal membaca riwayat");
                    lblPesanError.setText("<html><center>Gagal memuat arsip:<br>"
                            + ex.getMessage() + "<br>Klik MUAT ULANG untuk mencoba kembali.</center></html>");
                    cardKiri.show(wadahKiri, STATE_ERROR);
                    kosongkanReplay();
                }
            }
        }.execute();
    }

    private void pilihPertandingan(RekamanPertandingan r) {
        hentikanAuto();
        if (r == null) {
            kosongkanReplay();
            return;
        }
        lblJudulReplay.setText(r.getTrainer1() + " vs " + r.getTrainer2()
                + "  |  Pemenang: " + r.getPemenang() + " (" + r.getTotalRonde() + " ronde)");
        langkahAktif = r.getLangkahList();
        if (langkahAktif.isEmpty()) {
            indeksLangkah = -1;
            lblInfoLangkah.setText("Pertandingan ini tidak memiliki detail langkah.");
            txtLogLangkah.setText("Tidak ada log langkah yang tersimpan.");
            perbaruiTombolKontrol();
        } else {
            tampilkanLangkah(0);
        }
    }

    private void kosongkanReplay() {
        langkahAktif = List.of();
        indeksLangkah = -1;
        lblJudulReplay.setText("Belum ada pertandingan yang dipilih");
        lblInfoLangkah.setText("Langkah 0/0");
        lblSpritePelaku.setIcon(null);
        lblSpriteTarget.setIcon(null);
        lblNamaPelaku.setText("-");
        lblNamaTarget.setText("-");
        barHpPelaku.setNilai(0, 100);
        barHpTarget.setNilai(0, 100);
        lblHpPelaku.setText("HP: -/-");
        lblHpTarget.setText("HP: -/-");
        txtLogLangkah.setText("Pilih salah satu rekaman pertandingan untuk meninjau jalannya pertarungan.");
        perbaruiTombolKontrol();
    }

    private void tampilkanLangkah(int idx) {
        if (idx < 0 || idx >= langkahAktif.size()) return;
        indeksLangkah = idx;
        LangkahReplay l = langkahAktif.get(idx);

        lblInfoLangkah.setText("Langkah " + l.getNomorLangkah() + " / " + langkahAktif.size()
                + "  ·  Ronde " + l.getRonde() + "  ·  " + l.getRingkasanAksi());

        lblSpritePelaku.setIcon(SpriteLoader.icon(l.getPelakuHewan(), false, 72));
        lblSpritePelaku.getAccessibleContext().setAccessibleName("Sprite pelaku: " + l.getPelakuHewan());
        lblNamaPelaku.setText(l.getPelakuHewan() + " (" + l.getPelakuTrainer() + ")");
        barHpPelaku.setNilai(l.getHpPelakuSebelum(), l.getMaxHpPelaku());
        barHpPelaku.setNilaiAnimasi(l.getHpPelakuSesudah(), l.getMaxHpPelaku());
        lblHpPelaku.setText("HP: " + l.getHpPelakuSebelum() + " -> " + l.getHpPelakuSesudah()
                + " / " + l.getMaxHpPelaku());

        lblSpriteTarget.setIcon(SpriteLoader.icon(l.getTargetHewan(), true, 72));
        lblSpriteTarget.getAccessibleContext().setAccessibleName("Sprite target: " + l.getTargetHewan());
        lblNamaTarget.setText(l.getTargetHewan() + " (" + l.getTargetTrainer() + ")");
        barHpTarget.setNilai(l.getHpTargetSebelum(), l.getMaxHpTarget());
        barHpTarget.setNilaiAnimasi(l.getHpTargetSesudah(), l.getMaxHpTarget());
        lblHpTarget.setText("HP: " + l.getHpTargetSebelum() + " -> " + l.getHpTargetSesudah()
                + " / " + l.getMaxHpTarget());

        StringBuilder sb = new StringBuilder();
        for (String baris : l.getLog()) {
            if (sb.length() > 0) sb.append('\n');
            sb.append("> ").append(baris);
        }
        txtLogLangkah.setText(sb.toString());
        txtLogLangkah.setCaretPosition(0);
        perbaruiTombolKontrol();
    }

    private void toggleAutoPlay() {
        if (langkahAktif.isEmpty()) return;
        if (timerAuto.isRunning()) {
            hentikanAuto();
            return;
        }
        if (indeksLangkah >= langkahAktif.size() - 1) {
            tampilkanLangkah(0);
        }
        btnAuto.setText("JEDA AUTO");
        timerAuto.start();
        perbaruiTombolKontrol();
    }

    private void hentikanAuto() {
        if (timerAuto != null && timerAuto.isRunning()) {
            timerAuto.stop();
        }
        if (btnAuto != null) {
            btnAuto.setText("AUTO PLAY");
        }
        perbaruiTombolKontrol();
    }

    private void perbaruiTombolKontrol() {
        boolean ada = !langkahAktif.isEmpty();
        btnPrev.setEnabled(ada && indeksLangkah > 0);
        btnNext.setEnabled(ada && indeksLangkah < langkahAktif.size() - 1);
        btnAuto.setEnabled(ada);
    }

    private void tutupJendela() {
        hentikanAuto();
        dispose();
    }
}

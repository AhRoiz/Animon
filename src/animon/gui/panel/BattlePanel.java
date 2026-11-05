package animon.gui.panel;

import animon.battle.BattleEngine;
import animon.battle.EfekTertunda;
import animon.battle.ElemenChart;
import animon.battle.TypeChart;
import animon.enums.Efektivitas;
import animon.enums.TargetSkill;
import animon.gui.GameManager;
import animon.gui.komponen.LogArea;
import animon.gui.komponen.PixelBar;
import animon.gui.komponen.PixelButton;
import animon.gui.komponen.SpriteView;
import animon.gui.komponen.UIKit;
import animon.gui.tema.SpriteLoader;
import animon.gui.tema.Tema;
import animon.item.Item;
import animon.model.Buff;
import animon.model.Hewan;
import animon.model.Trainer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Layar Utama Pertempuran (Wireframe L.4) dengan arena dua arah (musuh di atas, pemain di bawah).
 * Menggabungkan kotak log mesin ketik di kiri dan panel aksi kontekstual di kanan.
 */
public final class BattlePanel extends JPanel {

    private static final String KARTU_UTAMA = "UTAMA";
    private static final String KARTU_FIGHT = "FIGHT";
    private static final String KARTU_HEWAN = "PILIH_HEWAN";
    private static final String KARTU_ITEM = "PILIH_ITEM";
    private static final String KARTU_PASCA = "PASCA_AKSI";

    private static final int UKURAN_SPRITE = 128;

    private final GameManager gameManager;
    private final JLabel lblRondeGiliran;

    private final JLabel lblTrainerMusuh;
    private final JLabel lblPipsMusuh;
    private final JLabel lblNamaMusuh;
    private final JLabel lblTipeMusuh;
    private final PixelBar barHpMusuh;
    private final JLabel lblAngkaHpMusuh;
    private final JLabel lblStatusMusuh;
    private final SpriteView spriteMusuh;

    private final SpriteView spritePemain;
    private final JLabel lblTrainerPemain;
    private final JLabel lblPipsPemain;
    private final JLabel lblNamaPemain;
    private final JLabel lblStatPemain;
    private final PixelBar barHpPemain;
    private final JLabel lblAngkaHpPemain;
    private final PixelBar barMpPemain;
    private final JLabel lblAngkaMpPemain;
    private final JLabel lblStatusPemain;

    private final LogArea logArea;
    private final JLabel lblPesanAksi;

    private final CardLayout cardAksi = new CardLayout();
    private final JPanel panelAksi;
    private final PixelButton btnFight;
    private final PixelButton btnSwitch;
    private final PixelButton btnItem;
    private final PixelButton btnDefend;

    private final PixelButton btnSerangBiasa;
    private final PixelButton btnSkill;
    private final JLabel lblInfoSkill;

    private final JLabel lblJudulPilihHewan;
    private final DefaultListModel<String[]> modelHewan = new DefaultListModel<>();
    private final JList<String[]> listHewan;
    private final PixelButton btnKembaliHewan;
    private IntConsumer aksiPilihHewan;
    private boolean modeWajibGanti;

    private final DefaultListModel<String[]> modelItem = new DefaultListModel<>();
    private final JList<String[]> listItem;

    private final JLabel lblInfoPasca;
    private final PixelButton btnLanjutPasca;
    private Runnable aksiPasca;

    private Hewan musuhTerakhir;
    private Hewan pemainTerakhir;

    public BattlePanel(GameManager gameManager) {
        super(new BorderLayout(0, 10));
        this.gameManager = gameManager;
        setBackground(Tema.TERANG);
        setBorder(BorderFactory.createEmptyBorder(10, 18, 12, 18));

        JPanel barAtas = UIKit.kotak(new BorderLayout(12, 0));
        barAtas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.GELAP, 3),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        lblRondeGiliran = UIKit.judul("RONDE 1  |  GILIRAN: -", 11f);
        barAtas.add(lblRondeGiliran, BorderLayout.WEST);

        JPanel utilPanel = UIKit.panel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        PixelButton btnInfo = tombolKecil("INFO", KeyEvent.VK_O, () -> gameManager.prosesAksi("INFO"));
        PixelButton btnSimpan = tombolKecil("SIMPAN", KeyEvent.VK_P, () -> gameManager.prosesAksi("SIMPAN"));
        PixelButton btnMenyerah = tombolKecil("MENYERAH", KeyEvent.VK_M, () -> gameManager.prosesAksi("MENYERAH"));
        utilPanel.add(btnInfo);
        utilPanel.add(btnSimpan);
        utilPanel.add(btnMenyerah);
        barAtas.add(utilPanel, BorderLayout.EAST);
        add(barAtas, BorderLayout.NORTH);

        JPanel arena = UIKit.panel(new GridLayout(2, 1, 0, 8));

        JPanel arenaAtas = UIKit.panel(new GridBagLayout());
        GridBagConstraints ga = new GridBagConstraints();
        ga.insets = new Insets(2, 6, 2, 6);
        ga.fill = GridBagConstraints.HORIZONTAL;

        JPanel hudMusuh = UIKit.kotak(new GridLayout(4, 1, 0, 3));
        JPanel barisTrainerMusuh = UIKit.panel(new BorderLayout());
        lblTrainerMusuh = UIKit.judul("LAWAN: -", 10f);
        lblPipsMusuh = UIKit.judul("", 10f);
        barisTrainerMusuh.add(lblTrainerMusuh, BorderLayout.WEST);
        barisTrainerMusuh.add(lblPipsMusuh, BorderLayout.EAST);

        JPanel barisNamaMusuh = UIKit.panel(new BorderLayout(8, 0));
        lblNamaMusuh = UIKit.judul("-", 12f);
        lblTipeMusuh = UIKit.label("-", 20f);
        barisNamaMusuh.add(lblNamaMusuh, BorderLayout.WEST);
        barisNamaMusuh.add(lblTipeMusuh, BorderLayout.EAST);

        barHpMusuh = new PixelBar(PixelBar.Jenis.HP);
        lblAngkaHpMusuh = UIKit.judul("HP 0/0", 9f);
        JPanel barisHpMusuh = barisBar("HP", barHpMusuh, lblAngkaHpMusuh);

        lblStatusMusuh = UIKit.label("Status: Normal", 19f);

        hudMusuh.add(barisTrainerMusuh);
        hudMusuh.add(barisNamaMusuh);
        hudMusuh.add(barisHpMusuh);
        hudMusuh.add(lblStatusMusuh);

        spriteMusuh = new SpriteView(UKURAN_SPRITE, true);

        ga.gridx = 0; ga.gridy = 0; ga.weightx = 0.62;
        arenaAtas.add(hudMusuh, ga);
        ga.gridx = 1; ga.weightx = 0.38; ga.fill = GridBagConstraints.NONE; ga.anchor = GridBagConstraints.CENTER;
        arenaAtas.add(spriteMusuh, ga);

        JPanel arenaBawah = UIKit.panel(new GridBagLayout());
        GridBagConstraints gb = new GridBagConstraints();
        gb.insets = new Insets(2, 6, 2, 6);

        spritePemain = new SpriteView(UKURAN_SPRITE, false);

        JPanel hudPemain = UIKit.kotak(new GridLayout(5, 1, 0, 2));
        JPanel barisTrainerPemain = UIKit.panel(new BorderLayout());
        lblTrainerPemain = UIKit.judul("AKTIF: -", 10f);
        lblPipsPemain = UIKit.judul("", 10f);
        barisTrainerPemain.add(lblTrainerPemain, BorderLayout.WEST);
        barisTrainerPemain.add(lblPipsPemain, BorderLayout.EAST);

        JPanel barisNamaPemain = UIKit.panel(new BorderLayout(8, 0));
        lblNamaPemain = UIKit.judul("-", 12f);
        lblStatPemain = UIKit.label("-", 19f);
        barisNamaPemain.add(lblNamaPemain, BorderLayout.WEST);
        barisNamaPemain.add(lblStatPemain, BorderLayout.EAST);

        barHpPemain = new PixelBar(PixelBar.Jenis.HP);
        lblAngkaHpPemain = UIKit.judul("HP 0/0", 9f);
        JPanel barisHpPemain = barisBar("HP", barHpPemain, lblAngkaHpPemain);

        barMpPemain = new PixelBar(PixelBar.Jenis.MP);
        lblAngkaMpPemain = UIKit.judul("MP 0/0", 9f);
        JPanel barisMpPemain = barisBar("MP", barMpPemain, lblAngkaMpPemain);

        lblStatusPemain = UIKit.label("Status: Normal", 19f);

        hudPemain.add(barisTrainerPemain);
        hudPemain.add(barisNamaPemain);
        hudPemain.add(barisHpPemain);
        hudPemain.add(barisMpPemain);
        hudPemain.add(lblStatusPemain);

        gb.gridx = 0; gb.gridy = 0; gb.weightx = 0.38; gb.fill = GridBagConstraints.NONE; gb.anchor = GridBagConstraints.CENTER;
        arenaBawah.add(spritePemain, gb);
        gb.gridx = 1; gb.weightx = 0.62; gb.fill = GridBagConstraints.HORIZONTAL;
        arenaBawah.add(hudPemain, gb);

        arena.add(arenaAtas);
        arena.add(arenaBawah);
        add(arena, BorderLayout.CENTER);

        JPanel battleBox = UIKit.panel(new GridLayout(1, 2, 12, 0));
        battleBox.setPreferredSize(new Dimension(920, 215));

        logArea = new LogArea();
        JScrollPane spLog = UIKit.scroll(logArea);
        lblPesanAksi = UIKit.judul("", 9f);
        lblPesanAksi.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));

        JPanel isiLog = UIKit.panel(new BorderLayout(0, 4));
        isiLog.add(spLog, BorderLayout.CENTER);
        isiLog.add(lblPesanAksi, BorderLayout.SOUTH);
        battleBox.add(UIKit.kotakBerjudul("DIALOG & LOG PERTEMPURAN", isiLog));

        panelAksi = UIKit.kotak(cardAksi);

        JPanel gridUtama = UIKit.panel(new GridLayout(2, 2, 10, 10));
        btnFight = new PixelButton("FIGHT", "Serangan Biasa / Skill", 13f);
        btnFight.setMnemonic(KeyEvent.VK_F);
        btnFight.addActionListener(e -> gameManager.prosesAksi("FIGHT"));

        btnSwitch = new PixelButton("SWITCH", "Tukar Animon aktif", 13f);
        btnSwitch.setMnemonic(KeyEvent.VK_S);
        btnSwitch.addActionListener(e -> gameManager.prosesAksi("SWITCH"));

        btnItem = new PixelButton("ITEM", "Pakai item dari tas", 13f);
        btnItem.setMnemonic(KeyEvent.VK_I);
        btnItem.addActionListener(e -> gameManager.prosesAksi("ITEM"));

        btnDefend = new PixelButton("DEFEND", "Tahan 50% damage", 13f);
        btnDefend.setMnemonic(KeyEvent.VK_D);
        btnDefend.addActionListener(e -> gameManager.prosesAksi("DEFEND"));

        gridUtama.add(btnFight);
        gridUtama.add(btnSwitch);
        gridUtama.add(btnItem);
        gridUtama.add(btnDefend);
        panelAksi.add(bungkusSubMenu("PILIH AKSI GILIRAN", gridUtama), KARTU_UTAMA);

        JPanel fightIsi = UIKit.panel(new BorderLayout(0, 6));
        JPanel tombolFight = UIKit.panel(new GridLayout(2, 1, 0, 8));
        btnSerangBiasa = new PixelButton("SERANGAN BIASA (0 MP)", "", 11f);
        btnSerangBiasa.setHorizontalAlignment(SwingConstants.LEFT);
        btnSerangBiasa.addActionListener(e -> gameManager.prosesAksi("SERANG_BIASA"));

        btnSkill = new PixelButton("SKILL", "", 11f);
        btnSkill.setHorizontalAlignment(SwingConstants.LEFT);
        btnSkill.addActionListener(e -> gameManager.prosesAksi("GUNAKAN_SKILL"));

        tombolFight.add(btnSerangBiasa);
        tombolFight.add(btnSkill);
        fightIsi.add(tombolFight, BorderLayout.CENTER);

        JPanel fightBawah = UIKit.panel(new BorderLayout(8, 0));
        lblInfoSkill = UIKit.label("", 18f);
        PixelButton btnBatalFight = tombolKecil("KEMBALI", KeyEvent.VK_B, this::tampilkanMenuUtama);
        fightBawah.add(lblInfoSkill, BorderLayout.CENTER);
        fightBawah.add(btnBatalFight, BorderLayout.EAST);
        fightIsi.add(fightBawah, BorderLayout.SOUTH);
        panelAksi.add(bungkusSubMenu("PILIH SERANGAN", fightIsi), KARTU_FIGHT);

        JPanel hewanIsi = UIKit.panel(new BorderLayout(0, 6));
        listHewan = UIKit.list(s -> s);
        listHewan.setModel(modelHewan);
        listHewan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listHewan.getAccessibleContext().setAccessibleName("Daftar pilihan Animon");
        listHewan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) konfirmasiPilihHewan();
            }
        });
        pasangEnterList(listHewan, this::konfirmasiPilihHewan);

        JScrollPane spHewan = UIKit.scroll(listHewan);
        spHewan.setBorder(BorderFactory.createLineBorder(Tema.GELAP, 2));
        hewanIsi.add(spHewan, BorderLayout.CENTER);

        JPanel hewanBawah = UIKit.panel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        PixelButton btnPilihHewan = tombolKecil("PILIH", KeyEvent.VK_L, this::konfirmasiPilihHewan);
        btnKembaliHewan = tombolKecil("KEMBALI", KeyEvent.VK_B, this::tampilkanMenuUtama);
        hewanBawah.add(btnPilihHewan);
        hewanBawah.add(btnKembaliHewan);
        hewanIsi.add(hewanBawah, BorderLayout.SOUTH);

        lblJudulPilihHewan = UIKit.labelJudulInvers("PILIH ANIMON");
        JPanel bungkusHewan = UIKit.panel(new BorderLayout(0, 6));
        bungkusHewan.add(lblJudulPilihHewan, BorderLayout.NORTH);
        bungkusHewan.add(hewanIsi, BorderLayout.CENTER);
        panelAksi.add(bungkusHewan, KARTU_HEWAN);

        JPanel itemIsi = UIKit.panel(new BorderLayout(0, 6));
        listItem = UIKit.list(s -> s);
        listItem.setModel(modelItem);
        listItem.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listItem.getAccessibleContext().setAccessibleName("Daftar item tas");
        listItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) konfirmasiPilihItem();
            }
        });
        pasangEnterList(listItem, this::konfirmasiPilihItem);

        JScrollPane spItem = UIKit.scroll(listItem);
        spItem.setBorder(BorderFactory.createLineBorder(Tema.GELAP, 2));
        itemIsi.add(spItem, BorderLayout.CENTER);

        JPanel itemBawah = UIKit.panel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        PixelButton btnGunakanItem = tombolKecil("GUNAKAN", KeyEvent.VK_G, this::konfirmasiPilihItem);
        PixelButton btnKembaliItem = tombolKecil("KEMBALI", KeyEvent.VK_B, this::tampilkanMenuUtama);
        itemBawah.add(btnGunakanItem);
        itemBawah.add(btnKembaliItem);
        itemIsi.add(itemBawah, BorderLayout.SOUTH);
        panelAksi.add(bungkusSubMenu("PILIH ITEM DARI TAS", itemIsi), KARTU_ITEM);

        JPanel pascaIsi = UIKit.panel(new BorderLayout(0, 10));
        lblInfoPasca = UIKit.label("", 22f);
        lblInfoPasca.setHorizontalAlignment(SwingConstants.CENTER);
        btnLanjutPasca = new PixelButton("AKHIRI GILIRAN", null, 13f);
        btnLanjutPasca.addActionListener(e -> {
            if (aksiPasca != null) aksiPasca.run();
        });
        pascaIsi.add(lblInfoPasca, BorderLayout.CENTER);
        pascaIsi.add(btnLanjutPasca, BorderLayout.SOUTH);
        panelAksi.add(bungkusSubMenu("STATUS GILIRAN", pascaIsi), KARTU_PASCA);

        battleBox.add(panelAksi);
        add(battleBox, BorderLayout.SOUTH);

        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "batalSubMenu");
        getActionMap().put("batalSubMenu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!modeWajibGanti && aksiPasca == null) {
                    tampilkanMenuUtama();
                }
            }
        });
    }

    private static JPanel bungkusSubMenu(String judul, JComponent isi) {
        JPanel p = UIKit.panel(new BorderLayout(0, 6));
        p.add(UIKit.labelJudulInvers(judul), BorderLayout.NORTH);
        p.add(isi, BorderLayout.CENTER);
        return p;
    }

    private static JPanel barisBar(String nama, PixelBar bar, JLabel angka) {
        JPanel p = UIKit.panel(new BorderLayout(8, 0));
        JLabel l = UIKit.judul(nama, 9f);
        l.setPreferredSize(new Dimension(28, 16));
        angka.setPreferredSize(new Dimension(150, 16));
        angka.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(l, BorderLayout.WEST);
        p.add(bar, BorderLayout.CENTER);
        p.add(angka, BorderLayout.EAST);
        return p;
    }

    private static PixelButton tombolKecil(String teks, int mnemonic, Runnable aksi) {
        PixelButton b = new PixelButton(teks, null, 9f);
        b.setMargin(new Insets(6, 14, 6, 10));
        b.setMnemonic(mnemonic);
        b.addActionListener(e -> aksi.run());
        return b;
    }

    private static void pasangEnterList(JList<?> list, Runnable aksi) {
        list.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "pilihBaris");
        list.getActionMap().put("pilihBaris", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aksi.run();
            }
        });
    }

    /** Perbarui seluruh HUD, sprite, dan log tanpa efek tween (dipakai saat awal giliran baru). */
    public void segarkanGiliranBaru(BattleEngine engine, List<String> log) {
        Trainer aktif = engine.getPemainAktif();
        Trainer lawan = engine.getLawan();
        Hewan hAktif = aktif.getHewanAktif();
        Hewan hMusuh = lawan.getHewanAktif();

        perbaruiTeksHud(engine);
        barHpMusuh.setNilai(hMusuh.getHp(), hMusuh.getMaxHp());
        barHpPemain.setNilai(hAktif.getHp(), hAktif.getMaxHp());
        barMpPemain.setNilai(hAktif.getMana(), hAktif.getMaxMana());

        spriteMusuh.setSprite(SpriteLoader.depan(hMusuh, UKURAN_SPRITE), hMusuh.isFainted());
        spriteMusuh.getAccessibleContext().setAccessibleName("Sprite Animon lawan: " + hMusuh.getNama());
        spritePemain.setSprite(SpriteLoader.belakang(hAktif, UKURAN_SPRITE), hAktif.isFainted());
        spritePemain.getAccessibleContext().setAccessibleName("Sprite Animon aktif: " + hAktif.getNama());
        if (musuhTerakhir != hMusuh && !hMusuh.isFainted()) spriteMusuh.animasiMasuk();
        if (pemainTerakhir != hAktif && !hAktif.isFainted()) spritePemain.animasiMasuk();
        musuhTerakhir = hMusuh;
        pemainTerakhir = hAktif;

        logArea.setBaris(log);
        lblPesanAksi.setText("");
    }

    /** Perbarui HUD dengan animasi bar HP/MP dan reaksi sprite setelah BattleEngine selesai menghitung damage. */
    public void segarkanSetelahAksi(BattleEngine engine, List<String> log,
                                    Hewan musuhSebelum, int hpMusuhSebelum,
                                    Hewan pemainSebelum) {
        Trainer aktif = engine.getPemainAktif();
        Trainer lawan = engine.getLawan();
        Hewan hAktif = aktif.getHewanAktif();
        Hewan hMusuh = lawan.getHewanAktif();

        perbaruiTeksHud(engine);
        logArea.setBaris(log);

        spriteMusuh.getAccessibleContext().setAccessibleName("Sprite Animon lawan: " + hMusuh.getNama());
        spritePemain.getAccessibleContext().setAccessibleName("Sprite Animon aktif: " + hAktif.getNama());

        if (hMusuh != musuhSebelum) {
            spriteMusuh.setSprite(SpriteLoader.depan(hMusuh, UKURAN_SPRITE), hMusuh.isFainted());
            barHpMusuh.setNilai(hMusuh.getHp(), hMusuh.getMaxHp());
            if (!hMusuh.isFainted()) spriteMusuh.animasiMasuk();
        } else {
            barHpMusuh.setNilaiAnimasi(hMusuh.getHp(), hMusuh.getMaxHp());
            if (hMusuh.getHp() < hpMusuhSebelum) {
                if (hMusuh.isFainted()) spriteMusuh.animasiPingsan();
                else spriteMusuh.animasiKena();
            }
        }

        if (hAktif != pemainSebelum) {
            spritePemain.setSprite(SpriteLoader.belakang(hAktif, UKURAN_SPRITE), hAktif.isFainted());
            barHpPemain.setNilai(hAktif.getHp(), hAktif.getMaxHp());
            barMpPemain.setNilai(hAktif.getMana(), hAktif.getMaxMana());
            if (!hAktif.isFainted()) spritePemain.animasiMasuk();
        } else {
            spritePemain.setSprite(SpriteLoader.belakang(hAktif, UKURAN_SPRITE), hAktif.isFainted());
            barHpPemain.setNilaiAnimasi(hAktif.getHp(), hAktif.getMaxHp());
            barMpPemain.setNilaiAnimasi(hAktif.getMana(), hAktif.getMaxMana());
        }

        musuhTerakhir = hMusuh;
        pemainTerakhir = hAktif;
    }

    private void perbaruiTeksHud(BattleEngine engine) {
        Trainer aktif = engine.getPemainAktif();
        Trainer lawan = engine.getLawan();
        Hewan hAktif = aktif.getHewanAktif();
        Hewan hMusuh = lawan.getHewanAktif();

        lblRondeGiliran.setText("RONDE " + engine.getRonde() + "   |   GILIRAN: " + aktif.getNama().toUpperCase());

        lblTrainerMusuh.setText("LAWAN: " + lawan.getNama());
        lblPipsMusuh.setText("TIM " + buatIndikatorTim(lawan));
        lblNamaMusuh.setText(hMusuh.getNama() + " Lv" + hMusuh.getLevel());
        lblTipeMusuh.setText("[" + hMusuh.getRole().getLabel() + "] "
                + hMusuh.getTipe().getLabel() + " / " + hMusuh.getElemen().getLabel());
        lblAngkaHpMusuh.setText(teksHp(hMusuh));
        lblStatusMusuh.setText("Status: " + ringkasStatus(hMusuh, List.of()));

        lblTrainerPemain.setText(aktif.getNama() + "  |  GOLD " + aktif.getGold() + "G  |  TAS "
                + aktif.jumlahItem() + "/" + Trainer.MAKS_INVENTORY);
        lblPipsPemain.setText("TIM " + buatIndikatorTim(aktif));
        lblNamaPemain.setText(hAktif.getNama() + " Lv" + hAktif.getLevel()
                + " (" + hAktif.getTipe().getLabel() + "/" + hAktif.getElemen().getLabel() + ")");
        lblStatPemain.setText(String.format("ATK %d  DEF %d  SPD %d  EVA %d%%",
                hAktif.getAttackEfektif(), hAktif.getDefenseEfektif(),
                hAktif.getSpeedEfektif(), hAktif.getEvasionEfektif()));
        lblAngkaHpPemain.setText(teksHp(hAktif));
        lblAngkaMpPemain.setText(hAktif.getMana() + "/" + hAktif.getMaxMana());
        lblStatusPemain.setText("Status: " + ringkasStatus(hAktif, engine.getAntrianUntuk(aktif)));

        btnSwitch.setSubTeks("Hidup: " + aktif.getJumlahHewanHidup() + "/" + aktif.getTim().size());
        btnItem.setSubTeks("Tas: " + aktif.jumlahItem() + "/" + Trainer.MAKS_INVENTORY);
    }

    private static String teksHp(Hewan h) {
        if (h.isFainted()) return "0/" + h.getMaxHp() + " [PINGSAN]";
        boolean kritis = h.getHp() * 4 < h.getMaxHp();
        return h.getHp() + "/" + h.getMaxHp() + (kritis ? " [KRITIS]" : "");
    }

    private static String buatIndikatorTim(Trainer t) {
        StringBuilder sb = new StringBuilder("[");
        List<Hewan> tim = t.getTim();
        for (int i = 0; i < tim.size(); i++) {
            if (i > 0) sb.append(' ');
            Hewan h = tim.get(i);
            if (h.isFainted()) sb.append('x');
            else if (h == t.getHewanAktif()) sb.append('*');
            else sb.append('o');
        }
        return sb.append(']').toString();
    }

    private static String ringkasStatus(Hewan h, List<EfekTertunda> tertunda) {
        if (h.isFainted()) return "PINGSAN";
        List<String> bagian = new ArrayList<>();
        if (h.isDefending()) bagian.add("BERTAHAN");
        if (h.getShield() > 0) bagian.add("Shield " + h.getShield());
        for (Buff b : h.getBuffs()) bagian.add(b.getLabel());
        for (EfekTertunda ef : tertunda) {
            if (ef.getTarget() == h) {
                bagian.add("Tunda:" + ef.getBuff().getLabel() + "[+" + ef.getSisaDelay() + "]");
            }
        }
        return bagian.isEmpty() ? "Normal" : String.join(" · ", bagian);
    }

    public void setPesan(String pesan) {
        lblPesanAksi.setText(pesan == null ? "" : pesan);
    }

    public void tampilkanMenuUtama() {
        modeWajibGanti = false;
        aksiPasca = null;
        cardAksi.show(panelAksi, KARTU_UTAMA);
        SwingUtilities.invokeLater(btnFight::requestFocusInWindow);
    }

    public void tampilkanMenuFight(BattleEngine engine) {
        Hewan aktif = engine.getHewanAktif();
        Hewan musuh = engine.getLawan().getHewanAktif();
        btnSerangBiasa.setSubTeks(prakiraanTeks(aktif, musuh, false));

        String alasan = aktif.cekSkill();
        btnSkill.setText(aktif.getSkill().getNama().toUpperCase() + " (" + aktif.getSkill().getManaCost() + " MP)");
        String ringkas = aktif.getSkill().getRingkasan();
        if (aktif.getSkill().getTarget() == TargetSkill.MUSUH) {
            ringkas += " | " + prakiraanTeks(aktif, musuh, true);
        }
        btnSkill.setSubTeks(alasan == null ? ringkas : "[" + alasan + "] " + ringkas);
        btnSkill.setEnabled(alasan == null);
        lblInfoSkill.setText(alasan == null ? aktif.getSkill().getDeskripsi() : alasan);

        cardAksi.show(panelAksi, KARTU_FIGHT);
        SwingUtilities.invokeLater(btnSerangBiasa::requestFocusInWindow);
    }

    private static String prakiraanTeks(Hewan penyerang, Hewan musuh, boolean pakaiElemenHewan) {
        String el;
        if (!pakaiElemenHewan) {
            el = "Normal -> netral";
        } else {
            Efektivitas ef = ElemenChart.cek(penyerang.getElemen(), musuh.getElemen());
            String hasil = ef == Efektivitas.LEMAH ? "WEAK x2" : ef == Efektivitas.RESIST ? "RESIST x0.5" : "netral";
            el = penyerang.getElemen().getLabel() + " vs " + musuh.getElemen().getLabel() + " -> " + hasil;
        }
        String tipe = TypeChart.cek(penyerang.getTipe(), musuh.getTipe()).ringkasanAngka();
        return tipe.isEmpty() ? el : el + " (" + tipe + ")";
    }

    public void tampilkanPemilihHewan(String judul, Trainer trainer, boolean wajib, IntConsumer saatPilih) {
        this.modeWajibGanti = wajib;
        this.aksiPilihHewan = saatPilih;
        this.aksiPasca = null;
        lblJudulPilihHewan.setText(judul);
        btnKembaliHewan.setVisible(!wajib);

        modelHewan.clear();
        List<Hewan> tim = trainer.getTim();
        int pilihAwal = 0;
        for (int i = 0; i < tim.size(); i++) {
            Hewan h = tim.get(i);
            String tag = h.isFainted() ? "[PINGSAN]" : (h == trainer.getHewanAktif() ? "[AKTIF]" : "[SIAP]");
            modelHewan.addElement(new String[] {
                    (i + 1) + ". " + h.getNama() + " Lv" + h.getLevel(),
                    h.getRole().getLabel() + " · " + h.getTipe().getLabel() + "/" + h.getElemen().getLabel()
                            + " · HP " + h.getHp() + "/" + h.getMaxHp() + " · MP " + h.getMana() + "/" + h.getMaxMana(),
                    tag
            });
            if (pilihAwal == 0 && !h.isFainted() && h != trainer.getHewanAktif()) {
                pilihAwal = i;
            }
        }
        if (!modelHewan.isEmpty()) listHewan.setSelectedIndex(pilihAwal);
        cardAksi.show(panelAksi, KARTU_HEWAN);
        SwingUtilities.invokeLater(listHewan::requestFocusInWindow);
    }

    private void konfirmasiPilihHewan() {
        int idx = listHewan.getSelectedIndex();
        if (idx >= 0 && aksiPilihHewan != null) {
            aksiPilihHewan.accept(idx);
        }
    }

    public void tampilkanPemilihItem(Trainer trainer) {
        modeWajibGanti = false;
        aksiPasca = null;
        modelItem.clear();
        for (int i = 0; i < trainer.jumlahItem(); i++) {
            Item it = trainer.getItem(i);
            modelItem.addElement(new String[] {
                    (i + 1) + ". " + it.getNama(),
                    "[" + it.getJenis().getLabel() + "] " + it.getRingkasan(),
                    it.untukHewanPingsan() ? "REVIVE" : it.getRarity().getLabel().toUpperCase()
            });
        }
        if (!modelItem.isEmpty()) listItem.setSelectedIndex(0);
        cardAksi.show(panelAksi, KARTU_ITEM);
        SwingUtilities.invokeLater(listItem::requestFocusInWindow);
    }

    private void konfirmasiPilihItem() {
        int slot = listItem.getSelectedIndex();
        if (slot >= 0) {
            gameManager.pilihSlotItemBattle(slot);
        }
    }

    public void tampilkanPascaAksi(String keterangan, String teksTombol, String subTombol, Runnable saatKlik) {
        modeWajibGanti = false;
        this.aksiPasca = saatKlik;
        lblInfoPasca.setText("<html><center>" + keterangan + "</center></html>");
        btnLanjutPasca.setText(teksTombol);
        btnLanjutPasca.setSubTeks(subTombol);
        cardAksi.show(panelAksi, KARTU_PASCA);
        SwingUtilities.invokeLater(btnLanjutPasca::requestFocusInWindow);
    }
}

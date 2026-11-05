package animon.gui.panel;

import animon.gui.komponen.InventoryPenuhDialog;
import animon.gui.komponen.PixelButton;
import animon.gui.komponen.PixelCard;
import animon.gui.komponen.UIKit;
import animon.gui.tema.Tema;
import animon.item.Item;
import animon.model.Trainer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Layar Pilihan Item (Wireframe L.5).
 * Dipakai ulang untuk Draft Awal (4 opsi pilih 2) dan Kill Reward (4 opsi pilih 1).
 */
public final class DraftPanel extends JPanel {

    private final JLabel lblHeader;
    private final JLabel lblInstruksi;
    private final JLabel lblStatusInventory;
    private final JLabel lblPesan;
    private final JPanel gridKartu;
    private final CardLayout cardInventory = new CardLayout();
    private final JPanel wadahInventory;
    private final DefaultListModel<Item> modelInventory = new DefaultListModel<>();
    private final PixelButton btnSelesai;
    private final InventoryPenuhDialog penanganPenuh;

    private Trainer trainerAktif;
    private int sisaPilihan;
    private Runnable saatSelesai;
    private final List<PixelCard> daftarKartu = new ArrayList<>();

    public DraftPanel() {
        super(new BorderLayout(16, 12));
        setBackground(Tema.TERANG);
        setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));
        this.penanganPenuh = new InventoryPenuhDialog(this);

        JPanel atas = UIKit.kotak(new BorderLayout(12, 6));
        lblHeader = UIKit.labelJudulInvers("DRAFT ITEM");
        lblInstruksi = UIKit.label("", 23f);
        atas.add(lblHeader, BorderLayout.NORTH);
        atas.add(lblInstruksi, BorderLayout.CENTER);
        add(atas, BorderLayout.NORTH);

        JPanel tengah = UIKit.panel(new BorderLayout(16, 0));

        gridKartu = UIKit.panel(new GridLayout(2, 2, 14, 14));
        JPanel kiriBox = UIKit.kotakBerjudul("PILIH OPSI ITEM GACHA", gridKartu);
        tengah.add(kiriBox, BorderLayout.CENTER);

        JPanel kananIsi = UIKit.panel(new BorderLayout(0, 8));
        lblStatusInventory = UIKit.judul("INVENTORY 0/6  |  GOLD 0G", 10f);
        kananIsi.add(lblStatusInventory, BorderLayout.NORTH);

        wadahInventory = UIKit.panel(cardInventory);
        JList<Item> listInv = UIKit.list(it -> new String[] {
                it.getNama(),
                it.getJenis().getLabel() + " (" + it.getRarity().getLabel() + ") - " + it.getRingkasan(),
                "Jual " + it.getHargaJual() + "G"
        });
        listInv.setModel(modelInventory);
        listInv.getAccessibleContext().setAccessibleName("Isi tas Trainer aktif");
        JScrollPane spInv = UIKit.scroll(listInv);
        spInv.setBorder(BorderFactory.createLineBorder(Tema.GELAP, 2));

        JLabel kosong = UIKit.label(
                "<html><center>Tas masih kosong (0/6).<br>Klik salah satu dari 4 kartu item di kiri.</center></html>",
                21f);
        kosong.setHorizontalAlignment(SwingConstants.CENTER);
        kosong.setBorder(BorderFactory.createLineBorder(Tema.GELAP, 2));

        wadahInventory.add(kosong, "KOSONG");
        wadahInventory.add(spInv, "ISI");
        kananIsi.add(wadahInventory, BorderLayout.CENTER);

        JPanel kananBox = UIKit.kotakBerjudul("TAS TRAINER AKTIF", kananIsi);
        kananBox.setPreferredSize(new Dimension(350, 380));
        tengah.add(kananBox, BorderLayout.EAST);

        add(tengah, BorderLayout.CENTER);

        JPanel bawah = UIKit.panel(new BorderLayout(12, 0));
        lblPesan = UIKit.label("", 22f);
        bawah.add(lblPesan, BorderLayout.CENTER);

        JPanel panelTombol = UIKit.panel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnSelesai = new PixelButton("LANJUTKAN", null, 12f);
        btnSelesai.setMnemonic(KeyEvent.VK_L);
        btnSelesai.addActionListener(e -> {
            if (saatSelesai != null) {
                Runnable r = saatSelesai;
                saatSelesai = null;
                r.run();
            }
        });
        panelTombol.add(btnSelesai);
        bawah.add(panelTombol, BorderLayout.EAST);
        add(bawah, BorderLayout.SOUTH);
    }

    public void buka(String judulMode, Trainer trainer, List<Item> opsi, int jumlahPilih, Runnable selesai) {
        this.trainerAktif = trainer;
        this.sisaPilihan = Math.min(jumlahPilih, opsi.size());
        this.saatSelesai = selesai;

        lblHeader.setText(judulMode.toUpperCase() + "  -  " + trainer.getNama().toUpperCase());
        lblPesan.setText("Klik kartu atau tekan Enter pada item yang ingin dibawa ke pertarungan.");

        gridKartu.removeAll();
        daftarKartu.clear();
        for (int i = 0; i < opsi.size(); i++) {
            Item item = opsi.get(i);
            PixelCard kartu = new PixelCard((i + 1) + ". " + item.getNama())
                    .badge(item.getRarity().getLabel().toUpperCase())
                    .baris("[" + item.getJenis().getLabel() + "] " + item.getRingkasan())
                    .baris(item.getDeskripsi())
                    .baris("Nilai jual: +" + item.getHargaJual() + "G")
                    .ukuran(250, 165);
            kartu.aksi(() -> pilihItem(kartu, item));
            daftarKartu.add(kartu);
            gridKartu.add(kartu);
        }

        perbaruiStatus();
        gridKartu.revalidate();
        gridKartu.repaint();

        SwingUtilities.invokeLater(() -> {
            if (!daftarKartu.isEmpty() && sisaPilihan > 0) {
                daftarKartu.get(0).requestFocusInWindow();
            } else {
                btnSelesai.requestFocusInWindow();
            }
        });
    }

    private void pilihItem(PixelCard kartu, Item item) {
        if (sisaPilihan <= 0) return;
        boolean masuk = trainerAktif.tambahItem(item, penanganPenuh);
        sisaPilihan--;
        kartu.nonaktif(masuk ? "DIAMBIL" : "DILEWATI");
        lblPesan.setText(masuk
                ? item.getNama() + " masuk ke tas " + trainerAktif.getNama() + "."
                : item.getNama() + " tidak diambil.");

        if (sisaPilihan <= 0) {
            kunciSisaKartu();
        }
        perbaruiStatus();
        if (sisaPilihan <= 0) {
            SwingUtilities.invokeLater(btnSelesai::requestFocusInWindow);
        }
    }

    private void kunciSisaKartu() {
        for (PixelCard c : daftarKartu) {
            if (c.getCursor().getType() == java.awt.Cursor.HAND_CURSOR) {
                c.nonaktif("TERKUNCI");
            }
        }
    }

    private void perbaruiStatus() {
        if (sisaPilihan > 0) {
            lblInstruksi.setText("Pilih " + sisaPilihan + " item lagi dari opsi yang tersedia.");
            btnSelesai.setEnabled(false);
            btnSelesai.setSubTeks("Pilih " + sisaPilihan + " item dulu");
        } else {
            lblInstruksi.setText("Pilihan item selesai. Klik LANJUTKAN untuk meneruskan permainan.");
            btnSelesai.setEnabled(true);
            btnSelesai.setSubTeks("Siap dilanjutkan");
        }

        lblStatusInventory.setText("TAS " + trainerAktif.jumlahItem() + "/" + Trainer.MAKS_INVENTORY
                + "  |  GOLD " + trainerAktif.getGold() + "G");
        modelInventory.clear();
        for (int i = 0; i < trainerAktif.jumlahItem(); i++) {
            modelInventory.addElement(trainerAktif.getItem(i));
        }
        cardInventory.show(wadahInventory, modelInventory.isEmpty() ? "KOSONG" : "ISI");
    }
}

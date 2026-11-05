package animon.gui.panel;

import animon.gui.GameManager;
import animon.gui.komponen.PixelButton;
import animon.gui.komponen.UIKit;
import animon.gui.tema.Tema;
import animon.item.Item;
import animon.model.Trainer;
import animon.sistem.Shop;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;

/**
 * Layar Toko Item (Wireframe L.6):
 * menampilkan Gold pemain, Daftar Beli (Stok Shop), Daftar Jual (Inventory), serta tombol BELI, JUAL, KELUAR.
 */
public final class ShopPanel extends JPanel {

    private final GameManager gameManager;
    private final JLabel lblHeader;
    private final JLabel lblGoldDanTas;
    private final JLabel lblDetail;
    private final JLabel lblPesan;
    private final DefaultListModel<Item> modelBeli = new DefaultListModel<>();
    private final DefaultListModel<Item> modelJual = new DefaultListModel<>();
    private final JList<Item> listBeli;
    private final JList<Item> listJual;
    private final CardLayout cardJual = new CardLayout();
    private final JPanel wadahJual;
    private final PixelButton btnBeli;
    private final PixelButton btnJual;
    private final PixelButton btnKeluar;

    private Trainer trainerAktif;
    private Shop shopAktif;
    private Runnable saatKeluar;

    public ShopPanel(GameManager gameManager) {
        super(new BorderLayout(0, 12));
        this.gameManager = gameManager;
        setBackground(Tema.TERANG);
        setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));

        JPanel atas = UIKit.kotak(new BorderLayout(12, 6));
        lblHeader = UIKit.labelJudulInvers("TOKO ANIMON");
        lblGoldDanTas = UIKit.judul("GOLD: 0G  |  INVENTORY: 0/6", 12f);
        atas.add(lblHeader, BorderLayout.NORTH);
        atas.add(lblGoldDanTas, BorderLayout.CENTER);
        add(atas, BorderLayout.NORTH);

        JPanel tengah = UIKit.panel(new GridLayout(1, 2, 16, 0));

        listBeli = UIKit.list(it -> new String[] {
                it.getNama(),
                "[" + it.getJenis().getLabel() + " · " + it.getRarity().getLabel() + "] " + it.getRingkasan(),
                it.getHargaBeli() + "G"
        });
        listBeli.setModel(modelBeli);
        listBeli.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listBeli.getAccessibleContext().setAccessibleName("Daftar beli stok toko");
        listBeli.addListSelectionListener(e -> perbaruiDetail(listBeli.getSelectedValue(), true));

        JScrollPane spBeli = UIKit.scroll(listBeli);
        spBeli.setBorder(BorderFactory.createLineBorder(Tema.GELAP, 2));
        tengah.add(UIKit.kotakBerjudul("DAFTAR BELI (STOK TOKO)", spBeli));

        listJual = UIKit.list(it -> new String[] {
                it.getNama(),
                "[" + it.getJenis().getLabel() + "] " + it.getRingkasan(),
                "+" + it.getHargaJual() + "G"
        });
        listJual.setModel(modelJual);
        listJual.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listJual.getAccessibleContext().setAccessibleName("Daftar jual inventory");
        listJual.addListSelectionListener(e -> perbaruiDetail(listJual.getSelectedValue(), false));

        JScrollPane spJual = UIKit.scroll(listJual);
        spJual.setBorder(BorderFactory.createLineBorder(Tema.GELAP, 2));

        JLabel kosongJual = UIKit.label(
                "<html><center>Inventory kosong (0/6).<br>Pilih item di Stok Toko lalu klik BELI.</center></html>",
                21f);
        kosongJual.setHorizontalAlignment(SwingConstants.CENTER);
        kosongJual.setBorder(BorderFactory.createLineBorder(Tema.GELAP, 2));

        wadahJual = UIKit.panel(cardJual);
        wadahJual.add(kosongJual, "KOSONG");
        wadahJual.add(spJual, "ISI");
        tengah.add(UIKit.kotakBerjudul("DAFTAR JUAL (INVENTORY - HARGA 50%)", wadahJual));

        add(tengah, BorderLayout.CENTER);

        JPanel bawah = UIKit.kotak(new BorderLayout(12, 8));
        JPanel infoPanel = UIKit.panel(new GridLayout(2, 1, 0, 2));
        lblDetail = UIKit.label("Pilih item pada salah satu daftar untuk melihat keterangan lengkap.", 21f);
        lblPesan = UIKit.judul("", 10f);
        infoPanel.add(lblDetail);
        infoPanel.add(lblPesan);
        bawah.add(infoPanel, BorderLayout.CENTER);

        JPanel tombolPanel = UIKit.panel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBeli = new PixelButton("BELI", null, 12f);
        btnBeli.setMnemonic(KeyEvent.VK_B);
        btnBeli.addActionListener(e -> aksiBeli());

        btnJual = new PixelButton("JUAL", null, 12f);
        btnJual.setMnemonic(KeyEvent.VK_J);
        btnJual.addActionListener(e -> aksiJual());

        btnKeluar = new PixelButton("KELUAR", null, 12f);
        btnKeluar.setMnemonic(KeyEvent.VK_K);
        btnKeluar.addActionListener(e -> {
            if (saatKeluar != null) {
                Runnable r = saatKeluar;
                saatKeluar = null;
                r.run();
            }
        });

        tombolPanel.add(btnBeli);
        tombolPanel.add(btnJual);
        tombolPanel.add(btnKeluar);
        bawah.add(tombolPanel, BorderLayout.EAST);

        add(bawah, BorderLayout.SOUTH);
    }

    public void buka(Trainer trainer, Shop shop, String pesanAwal, Runnable selesai) {
        this.trainerAktif = trainer;
        this.shopAktif = shop;
        this.saatKeluar = selesai;
        lblHeader.setText("TOKO ANIMON  -  " + trainer.getNama().toUpperCase());
        lblPesan.setText(pesanAwal == null ? "" : pesanAwal);

        modelBeli.clear();
        for (Item it : shop.getStok()) modelBeli.addElement(it);
        if (!modelBeli.isEmpty()) listBeli.setSelectedIndex(0);

        segarkanInventory();
        SwingUtilities.invokeLater(listBeli::requestFocusInWindow);
    }

    public void segarkanInventory() {
        if (trainerAktif == null) return;
        lblGoldDanTas.setText("TRAINER: " + trainerAktif.getNama()
                + "   |   GOLD: " + trainerAktif.getGold() + "G"
                + "   |   TAS: " + trainerAktif.jumlahItem() + "/" + Trainer.MAKS_INVENTORY);

        int idxLama = listJual.getSelectedIndex();
        modelJual.clear();
        for (int i = 0; i < trainerAktif.jumlahItem(); i++) {
            modelJual.addElement(trainerAktif.getItem(i));
        }
        boolean kosong = modelJual.isEmpty();
        cardJual.show(wadahJual, kosong ? "KOSONG" : "ISI");
        btnJual.setEnabled(!kosong);
        if (!kosong) {
            listJual.setSelectedIndex(Math.max(0, Math.min(idxLama, modelJual.size() - 1)));
        }
    }

    private void perbaruiDetail(Item item, boolean modeBeli) {
        if (item == null) return;
        String harga = modeBeli ? ("Harga beli " + item.getHargaBeli() + "G")
                : ("Harga jual +" + item.getHargaJual() + "G");
        lblDetail.setText(item.getNama() + " (" + harga + "): " + item.getDeskripsi());
    }

    private void aksiBeli() {
        int idx = listBeli.getSelectedIndex();
        if (idx < 0) {
            lblPesan.setText("Pilih item pada Daftar Beli terlebih dahulu.");
            return;
        }
        String hasil = gameManager.beliDiToko(trainerAktif, shopAktif, idx);
        lblPesan.setText(hasil);
        segarkanInventory();
    }

    private void aksiJual() {
        int slot = listJual.getSelectedIndex();
        if (slot < 0) {
            lblPesan.setText("Pilih item pada Daftar Jual terlebih dahulu.");
            return;
        }
        String hasil = gameManager.jualDiToko(trainerAktif, shopAktif, slot);
        lblPesan.setText(hasil);
        segarkanInventory();
    }
}

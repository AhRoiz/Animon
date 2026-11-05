package animon.gui.panel;

import animon.gui.GameManager;
import animon.gui.komponen.PixelButton;
import animon.gui.komponen.UIKit;
import animon.gui.tema.SpriteLoader;
import animon.gui.tema.Tema;
import animon.model.Hewan;
import animon.model.Trainer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Layar Persiapan Trainer (Wireframe L.2) untuk mengisi nama kedua Trainer, ukuran tim, dan Animon pembuka.
 * Lanjut ke Draft Item memicu validasi nama sebelum menyerahkan ke {@code GameManager}.
 */
public final class SetupPanel extends JPanel {

    private final GameManager gameManager;
    private final JTextField txtNama1;
    private final JTextField txtNama2;
    private final JComboBox<Integer> cmbUkuranTim;
    private final DefaultListModel<Hewan> modelTim1 = new DefaultListModel<>();
    private final DefaultListModel<Hewan> modelTim2 = new DefaultListModel<>();
    private final JList<Hewan> listTim1;
    private final JList<Hewan> listTim2;
    private final JLabel lblPembuka1;
    private final JLabel lblPembuka2;
    private final JLabel lblPesan;

    private List<Hewan> tim1 = new ArrayList<>();
    private List<Hewan> tim2 = new ArrayList<>();
    private int pembuka1;
    private int pembuka2;

    public SetupPanel(GameManager gameManager) {
        super(new BorderLayout(0, 12));
        this.gameManager = gameManager;
        setBackground(Tema.TERANG);
        setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));

        JPanel header = UIKit.kotak(new BorderLayout(12, 8));
        header.add(UIKit.labelJudulInvers("PERSIAPAN TRAINER & SUSUNAN TIM"), BorderLayout.NORTH);

        JPanel form = UIKit.panel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 8, 4, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel l1 = UIKit.judul("TRAINER 1:", 10f);
        txtNama1 = UIKit.textField("Player 1");
        txtNama1.setColumns(14);
        l1.setLabelFor(txtNama1);

        JLabel l2 = UIKit.judul("TRAINER 2:", 10f);
        txtNama2 = UIKit.textField("Player 2");
        txtNama2.setColumns(14);
        l2.setLabelFor(txtNama2);

        JLabel lUkuran = UIKit.judul("JUMLAH ANIMON:", 10f);
        cmbUkuranTim = new JComboBox<>();
        cmbUkuranTim.setFont(Tema.judul(11f));
        cmbUkuranTim.setBackground(Tema.TERANG);
        cmbUkuranTim.setForeground(Tema.GELAP);
        cmbUkuranTim.setPreferredSize(new java.awt.Dimension(80, 44));
        cmbUkuranTim.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        cmbUkuranTim.getAccessibleContext().setAccessibleName("Jumlah Animon per Trainer");
        javax.swing.border.Border borderCmbNormal = BorderFactory.createLineBorder(Tema.GELAP, 3);
        javax.swing.border.Border borderCmbFokus = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.GELAP, 4),
                BorderFactory.createLineBorder(Tema.MUDA, 2));
        cmbUkuranTim.setBorder(borderCmbNormal);
        cmbUkuranTim.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                cmbUkuranTim.setBorder(borderCmbFokus);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                cmbUkuranTim.setBorder(borderCmbNormal);
            }
        });
        lUkuran.setLabelFor(cmbUkuranTim);
        cmbUkuranTim.addActionListener(e -> {
            if (cmbUkuranTim.isFocusOwner() || !tim1.isEmpty()) acakUlangTim();
        });

        PixelButton btnAcak = new PixelButton("ACAK ULANG", null, 10f);
        btnAcak.setMnemonic(KeyEvent.VK_A);
        btnAcak.addActionListener(e -> acakUlangTim());

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        form.add(l1, gc);
        gc.gridx = 1; gc.weightx = 0.35;
        form.add(txtNama1, gc);
        gc.gridx = 2; gc.weightx = 0;
        form.add(l2, gc);
        gc.gridx = 3; gc.weightx = 0.35;
        form.add(txtNama2, gc);
        gc.gridx = 4; gc.weightx = 0;
        form.add(lUkuran, gc);
        gc.gridx = 5; gc.weightx = 0.1;
        form.add(cmbUkuranTim, gc);
        gc.gridx = 6; gc.weightx = 0;
        form.add(btnAcak, gc);

        header.add(form, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        JPanel tengah = UIKit.panel(new GridLayout(1, 2, 16, 0));

        lblPembuka1 = UIKit.label("", 20f);
        listTim1 = buatListTim(modelTim1, true);
        listTim1.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listTim1.getSelectedIndex() >= 0) {
                pembuka1 = listTim1.getSelectedIndex();
                perbaruiLabelPembuka();
                listTim1.repaint();
            }
        });
        tengah.add(bungkusDaftar("DAFTAR ANIMON TRAINER 1 (KLIK UNTUK PEMBUKA)", listTim1, lblPembuka1));

        lblPembuka2 = UIKit.label("", 20f);
        listTim2 = buatListTim(modelTim2, false);
        listTim2.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listTim2.getSelectedIndex() >= 0) {
                pembuka2 = listTim2.getSelectedIndex();
                perbaruiLabelPembuka();
                listTim2.repaint();
            }
        });
        tengah.add(bungkusDaftar("DAFTAR ANIMON TRAINER 2 (KLIK UNTUK PEMBUKA)", listTim2, lblPembuka2));

        add(tengah, BorderLayout.CENTER);

        JPanel bawah = UIKit.panel(new BorderLayout(12, 6));
        lblPesan = UIKit.label("Pilih baris Animon untuk menentukan petarung pembuka tiap Trainer.", 21f);
        bawah.add(lblPesan, BorderLayout.WEST);

        JPanel aksi = UIKit.panel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        PixelButton btnKembali = new PixelButton("KEMBALI", null, 11f);
        btnKembali.setMnemonic(KeyEvent.VK_K);
        btnKembali.addActionListener(e -> gameManager.prosesAksi("KEMBALI_MENU"));

        PixelButton btnMulai = new PixelButton("MULAI DRAFT ITEM", "Lanjut pilih 2 item awal tiap Trainer", 12f);
        btnMulai.setMnemonic(KeyEvent.VK_M);
        btnMulai.addActionListener(e -> konfirmasiMulai());

        aksi.add(btnKembali);
        aksi.add(btnMulai);
        bawah.add(aksi, BorderLayout.EAST);
        add(bawah, BorderLayout.SOUTH);
    }

    private JPanel bungkusDaftar(String judul, JList<Hewan> list, JLabel infoBawah) {
        JPanel isi = UIKit.panel(new BorderLayout(0, 6));
        JScrollPane sp = UIKit.scroll(list);
        sp.setBorder(BorderFactory.createLineBorder(Tema.GELAP, 2));
        isi.add(sp, BorderLayout.CENTER);
        isi.add(infoBawah, BorderLayout.SOUTH);
        return UIKit.kotakBerjudul(judul, isi);
    }

    private JList<Hewan> buatListTim(DefaultListModel<Hewan> model, boolean milikP1) {
        JList<Hewan> l = new JList<>(model);
        l.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        l.setBackground(Tema.TERANG);
        l.setForeground(Tema.GELAP);
        l.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        l.getAccessibleContext().setAccessibleName(milikP1 ? "Daftar Animon Trainer 1" : "Daftar Animon Trainer 2");
        l.setCellRenderer(rendererHewan(milikP1));
        return l;
    }

    private ListCellRenderer<Hewan> rendererHewan(boolean milikP1) {
        return (list, h, index, selected, focus) -> {
            JPanel p = new JPanel(new BorderLayout(10, 2));
            boolean pembuka = (milikP1 ? pembuka1 : pembuka2) == index;
            Color bg = selected ? Tema.GELAP : (index % 2 == 0 ? Tema.TERANG : new Color(0x94b40f));
            Color fg = selected ? Tema.TERANG : Tema.GELAP;
            Color subFg = selected ? Tema.MUDA : Tema.GELAP;
            p.setBackground(bg);
            p.setBorder(BorderFactory.createCompoundBorder(
                    focus ? BorderFactory.createLineBorder(selected ? Tema.MUDA : Tema.GELAP, 2)
                          : BorderFactory.createMatteBorder(0, 0, 2, 0, Tema.MUDA),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)));

            JLabel sprite = new JLabel(SpriteLoader.depan(h, 48));
            sprite.getAccessibleContext().setAccessibleName("Sprite " + h.getNama());
            p.add(sprite, BorderLayout.WEST);

            JPanel teks = UIKit.panel(new GridLayout(3, 1, 0, 1));
            String tagPembuka = pembuka ? "  [PEMBUKA]" : "";
            JLabel baris1 = new JLabel((index + 1) + ". " + h.getNama() + " Lv" + h.getLevel() + tagPembuka);
            baris1.setFont(Tema.judul(10f));
            baris1.setForeground(fg);

            JLabel baris2 = new JLabel(String.format("[%s] %s/%s  |  HP %d  MP %d  ATK %d  DEF %d  SPD %d",
                    h.getRole().getLabel(), h.getTipe().getLabel(), h.getElemen().getLabel(),
                    h.getMaxHp(), h.getMaxMana(), h.getAttack(), h.getDefense(), h.getSpeed()));
            baris2.setFont(Tema.teks(19f));
            baris2.setForeground(subFg);

            JLabel baris3 = new JLabel("Skill: " + h.getSkill().getNama()
                    + " (" + h.getSkill().getManaCost() + " MP) - " + h.getSkill().getRingkasan());
            baris3.setFont(Tema.teks(18f));
            baris3.setForeground(subFg);

            teks.add(baris1);
            teks.add(baris2);
            teks.add(baris3);
            p.add(teks, BorderLayout.CENTER);
            return (Component) p;
        };
    }

    public void siapkanBaru() {
        int maks = Math.min(Trainer.MAKS_TIM, gameManager.getJumlahKatalogHewan() / 2);
        cmbUkuranTim.removeAllItems();
        for (int i = 1; i <= maks; i++) cmbUkuranTim.addItem(i);
        cmbUkuranTim.setSelectedItem(maks);

        if (txtNama1.getText().isBlank()) txtNama1.setText("Player 1");
        if (txtNama2.getText().isBlank()) txtNama2.setText("Player 2");
        acakUlangTim();
        lblPesan.setText("Pilih baris Animon untuk menentukan petarung pembuka tiap Trainer.");
        SwingUtilities.invokeLater(() -> {
            txtNama1.requestFocusInWindow();
            txtNama1.selectAll();
        });
    }

    private void acakUlangTim() {
        Integer u = (Integer) cmbUkuranTim.getSelectedItem();
        int ukuran = u == null ? Trainer.MAKS_TIM : u;
        List<List<Hewan>> hasil = gameManager.buatPasanganTimAcak(ukuran);
        tim1 = hasil.get(0);
        tim2 = hasil.get(1);
        pembuka1 = 0;
        pembuka2 = 0;

        modelTim1.clear();
        for (Hewan h : tim1) modelTim1.addElement(h);
        modelTim2.clear();
        for (Hewan h : tim2) modelTim2.addElement(h);

        if (!tim1.isEmpty()) listTim1.setSelectedIndex(0);
        if (!tim2.isEmpty()) listTim2.setSelectedIndex(0);
        perbaruiLabelPembuka();
    }

    private void perbaruiLabelPembuka() {
        if (!tim1.isEmpty()) {
            Hewan h1 = tim1.get(pembuka1);
            lblPembuka1.setText("Pembuka P1: " + h1.getNama() + " (SPD " + h1.getSpeed() + ")");
        }
        if (!tim2.isEmpty()) {
            Hewan h2 = tim2.get(pembuka2);
            lblPembuka2.setText("Pembuka P2: " + h2.getNama() + " (SPD " + h2.getSpeed() + ")");
        }
    }

    private void konfirmasiMulai() {
        String n1 = txtNama1.getText().trim();
        String n2 = txtNama2.getText().trim();
        if (n1.isEmpty() || n2.isEmpty()) {
            lblPesan.setText("PERINGATAN: Nama Trainer 1 dan Trainer 2 wajib diisi sebelum lanjut.");
            if (n1.isEmpty()) txtNama1.requestFocusInWindow();
            else txtNama2.requestFocusInWindow();
            return;
        }
        if (n1.equalsIgnoreCase(n2)) {
            n2 = n2 + " (2)";
            txtNama2.setText(n2);
        }
        gameManager.mulaiDraftAwal(n1, n2, tim1, pembuka1, tim2, pembuka2);
    }
}

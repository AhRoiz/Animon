package animon.gui.komponen;

import animon.gui.tema.Tema;
import animon.item.Item;
import animon.kontrak.PenanganInventoryPenuh;
import animon.model.Trainer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

/**
 * Implementasi GUI {@link PenanganInventoryPenuh} saat tas mencapai kapasitas 6/6.
 * Meminta pemain memilih jual (50% harga), buang slot lama, atau tolak item baru.
 */
public final class InventoryPenuhDialog implements PenanganInventoryPenuh {

    private final Component induk;

    public InventoryPenuhDialog(Component induk) {
        this.induk = induk;
    }

    @Override
    public Keputusan putuskan(Trainer t, Item baru) {
        while (true) {
            DefaultListModel<Item> model = new DefaultListModel<>();
            for (int i = 0; i < t.jumlahItem(); i++) model.addElement(t.getItem(i));
            JList<Item> list = UIKit.list(it -> new String[] {
                    it.getNama(), it.getJenis().getLabel() + " - " + it.getRingkasan(), "+" + it.getHargaJual() + "G"});
            list.setModel(model);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setSelectedIndex(0);

            JPanel isi = UIKit.panel(new BorderLayout(0, 10));
            isi.add(UIKit.label("Item baru: " + baru.getNama() + " (" + baru.getRingkasan() + ")", 23f), BorderLayout.NORTH);
            JScrollPane sp = UIKit.scroll(list);
            sp.setPreferredSize(new Dimension(520, 260));
            sp.setBorder(BorderFactory.createLineBorder(Tema.GELAP, 3));
            isi.add(sp, BorderLayout.CENTER);
            isi.add(UIKit.label("Pilih item lama, lalu JUAL (dapat 50% harga) atau BUANG.", 20f), BorderLayout.SOUTH);

            int p = DialogPixel.pilih(induk, "INVENTORY PENUH - " + t.getNama(), isi, "JUAL", "BUANG", "TOLAK ITEM BARU");
            if (p == 2 || p < 0) return Keputusan.tolak();
            int slot = list.getSelectedIndex();
            if (slot < 0) {
                DialogPixel.info(induk, "PILIH ITEM", "Pilih dulu item lama yang ingin di" + (p == 0 ? "jual." : "buang."));
                continue;
            }
            return p == 0 ? Keputusan.jual(slot) : Keputusan.buang(slot);
        }
    }
}

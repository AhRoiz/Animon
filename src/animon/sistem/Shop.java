package animon.sistem;

import animon.item.Item;
import animon.model.Trainer;
import animon.util.Acak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Toko item. Setiap fase toko dibuka, stok di-{@link #restok()} secara acak dari katalog.
 * Stok tidak habis (satu jenis item boleh dibeli berkali-kali selama gold cukup).
 *
 * Toko hanya berisi aturan ekonomi; tampilan/menu ada di layer UI.
 */
public final class Shop {

    public static final int JUMLAH_STOK = 6;

    private final List<Item> katalog;
    private final List<Item> stok = new ArrayList<>();

    public Shop(List<Item> katalog) {
        Objects.requireNonNull(katalog, "katalog");
        if (katalog.isEmpty()) throw new IllegalArgumentException("Katalog toko kosong");
        this.katalog = new ArrayList<>(katalog);
        restok();
    }

    /** Mengacak ulang stok toko (diurutkan dari yang termurah). */
    public void restok() {
        List<Item> acak = new ArrayList<>(katalog);
        Collections.shuffle(acak, Acak.get());
        stok.clear();
        stok.addAll(acak.subList(0, Math.min(JUMLAH_STOK, acak.size())));
        stok.sort(Comparator.comparingInt(Item::getHargaBeli));
    }

    public List<Item> getStok() {
        return Collections.unmodifiableList(stok);
    }

    /** @return null bila trainer boleh membeli stok ke-{@code index}, atau alasan bila tidak. */
    public String cekBeli(Trainer trainer, int index) {
        if (index < 0 || index >= stok.size()) return "Nomor item tidak valid.";
        Item item = stok.get(index);
        if (trainer.isInventoryPenuh()) return "Inventory penuh (" + Trainer.MAKS_INVENTORY + "/" + Trainer.MAKS_INVENTORY + ")! Jual item terlebih dahulu.";
        if (trainer.getGold() < item.getHargaBeli()) {
            return "Gold tidak cukup (" + trainer.getGold() + "/" + item.getHargaBeli() + ").";
        }
        return null;
    }

    /** Beli item dengan gold. @return item yang dibeli. */
    public Item beli(Trainer trainer, int index) {
        String alasan = cekBeli(trainer, index);
        if (alasan != null) throw new IllegalStateException(alasan);
        Item item = stok.get(index);
        trainer.bayar(item.getHargaBeli());
        trainer.tambahItem(item);
        return item;
    }

    /** Jual item dari inventory seharga 50% harga beli. @return gold yang diterima. */
    public int jual(Trainer trainer, int slot) {
        if (trainer.getItem(slot) == null) throw new IllegalArgumentException("Slot inventory kosong.");
        return trainer.jualItem(slot);
    }
}

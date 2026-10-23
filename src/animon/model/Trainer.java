package animon.model;

import animon.battle.HasilAksi;
import animon.item.Item;
import animon.kontrak.PenanganInventoryPenuh;
import animon.kontrak.PenanganInventoryPenuh.Keputusan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Pemain / Trainer. Membawa maksimal 6 hewan dan 6 item.
 *
 * <b>Encapsulation</b>: tim, inventory, dan gold tidak bisa diubah langsung dari luar.
 * Semua perubahan melewati method yang menjaga aturan (batas 6, gold tidak negatif,
 * tidak bisa switch ke hewan pingsan, dsb.). Inventory selalu dirapatkan ke kiri
 * sehingga slot 0..jumlahItem()-1 selalu terisi.
 */
public class Trainer {

    public static final int MAKS_TIM = 6;
    public static final int MAKS_INVENTORY = 6;

    private final String nama;
    private final List<Hewan> tim = new ArrayList<>();
    private final Item[] inventory = new Item[MAKS_INVENTORY];
    private int gold;
    private int indexAktif;
    private int jumlahKill;

    public Trainer(String nama, int goldAwal) {
        if (nama == null || nama.isBlank()) throw new IllegalArgumentException("Nama trainer wajib diisi");
        if (goldAwal < 0) throw new IllegalArgumentException("Gold awal tidak boleh negatif");
        this.nama = nama.trim();
        this.gold = goldAwal;
    }

    // =================================================================== tim

    public void tambahHewan(Hewan hewan) {
        Objects.requireNonNull(hewan, "hewan");
        if (tim.size() >= MAKS_TIM) throw new IllegalStateException("Tim " + nama + " sudah berisi " + MAKS_TIM + " hewan");
        tim.add(hewan);
    }

    public Hewan getHewanAktif() {
        return tim.isEmpty() ? null : tim.get(indexAktif);
    }

    public int getIndexAktif() {
        return indexAktif;
    }

    /** @return null bila boleh switch ke {@code index}, atau alasan bila tidak. */
    public String cekSwitch(int index) {
        if (index < 0 || index >= tim.size()) return "Nomor hewan tidak valid.";
        if (index == indexAktif) return tim.get(index).getNama() + " sudah berada di arena.";
        if (tim.get(index).isFainted()) return tim.get(index).getNama() + " sedang pingsan.";
        return null;
    }

    /** Menukar hewan aktif. Status bertahan hewan yang ditarik dilepas. */
    public HasilAksi switchHewan(int index) {
        String alasan = cekSwitch(index);
        if (alasan != null) return HasilAksi.gagal(alasan);
        Hewan lama = getHewanAktif();
        lama.setDefending(false);
        indexAktif = index;
        Hewan baru = getHewanAktif();
        HasilAksi h = HasilAksi.baru();
        if (!lama.isFainted()) h.log(nama + " menarik " + lama.getNama() + ".");
        return h.log(nama + " mengirim " + baru.getNama() + " ke arena!");
    }

    public int getJumlahHewanHidup() {
        int n = 0;
        for (Hewan h : tim) if (!h.isFainted()) n++;
        return n;
    }

    /** Spesifikasi: siapa yang kehabisan hewan duluan akan kalah. */
    public boolean masihPunyaHewan() {
        return getJumlahHewanHidup() > 0;
    }

    public List<Hewan> getTim() {
        return Collections.unmodifiableList(tim);
    }

    // =================================================================== inventory

    public int jumlahItem() {
        int n = 0;
        while (n < MAKS_INVENTORY && inventory[n] != null) n++;
        return n;
    }

    public boolean isInventoryPenuh() {
        return jumlahItem() >= MAKS_INVENTORY;
    }

    public Item getItem(int slot) {
        return slot >= 0 && slot < MAKS_INVENTORY ? inventory[slot] : null;
    }

    /** Salinan array inventory (perubahan pada salinan tidak mempengaruhi trainer). */
    public Item[] getInventory() {
        return inventory.clone();
    }

    /**
     * Menambah item dengan validasi batas 6. Bila penuh, {@code penangan} dipaksa memilih:
     * jual / buang salah satu item, atau menolak item baru.
     *
     * @return true bila item baru berhasil masuk inventory.
     */
    public boolean tambahItem(Item item, PenanganInventoryPenuh penangan) {
        Objects.requireNonNull(item, "item");
        if (!isInventoryPenuh()) {
            inventory[jumlahItem()] = item;
            return true;
        }
        if (penangan == null) return false;
        Keputusan k = penangan.putuskan(this, item);
        if (k == null || k.getAksi() == Keputusan.Aksi.TOLAK) return false;
        if (getItem(k.getSlot()) == null) throw new IllegalArgumentException("Slot inventory tidak valid: " + k.getSlot());
        if (k.getAksi() == Keputusan.Aksi.JUAL) {
            jualItem(k.getSlot());
        } else {
            hapusItem(k.getSlot());
        }
        inventory[jumlahItem()] = item;
        return true;
    }

    /** Versi tanpa penangan: hanya berhasil bila inventory belum penuh. */
    public boolean tambahItem(Item item) {
        return tambahItem(item, null);
    }

    /** Mengeluarkan item dari slot dan merapatkan inventory. @return item yang dikeluarkan. */
    public Item hapusItem(int slot) {
        Item item = getItem(slot);
        if (item == null) throw new IllegalArgumentException("Slot inventory kosong: " + slot);
        for (int i = slot; i < MAKS_INVENTORY - 1; i++) inventory[i] = inventory[i + 1];
        inventory[MAKS_INVENTORY - 1] = null;
        return item;
    }

    /** Menjual item di slot. @return gold yang diterima (50% harga beli). */
    public int jualItem(int slot) {
        Item item = hapusItem(slot);
        gold += item.getHargaJual();
        return item.getHargaJual();
    }

    /** @return null bila item di slot boleh dipakai ke target, atau alasan bila tidak. */
    public String cekGunakanItem(int slot, Hewan target) {
        Item item = getItem(slot);
        if (item == null) return "Slot item kosong.";
        if (target != null && !tim.contains(target)) return "Item hanya bisa dipakai pada hewan di tim sendiri.";
        return item.cekPenggunaan(target);
    }

    /** Menggunakan item (polymorphism: Item.gunakan). Item hanya habis bila pemakaian berhasil. */
    public HasilAksi gunakanItem(int slot, Hewan target) {
        String alasan = cekGunakanItem(slot, target);
        if (alasan != null) return HasilAksi.gagal(alasan);
        HasilAksi hasil = getItem(slot).gunakan(target);
        if (hasil.isBerhasil()) hapusItem(slot);
        return hasil;
    }

    // =================================================================== gold & statistik

    public int getGold() {
        return gold;
    }

    public void tambahGold(int jumlah) {
        if (jumlah < 0) throw new IllegalArgumentException("Jumlah gold tidak boleh negatif");
        gold += jumlah;
    }

    /** @return true bila gold cukup dan sudah dipotong. */
    public boolean bayar(int jumlah) {
        if (jumlah < 0) throw new IllegalArgumentException("Jumlah bayar tidak boleh negatif");
        if (gold < jumlah) return false;
        gold -= jumlah;
        return true;
    }

    public void catatKill() {
        jumlahKill++;
    }

    public int getJumlahKill() {
        return jumlahKill;
    }

    public String getNama() {
        return nama;
    }

    @Override
    public String toString() {
        return nama + " (" + getJumlahHewanHidup() + "/" + tim.size() + " hewan, " + gold + "G)";
    }
}

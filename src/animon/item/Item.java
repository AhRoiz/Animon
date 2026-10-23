package animon.item;

import animon.battle.HasilAksi;
import animon.enums.JenisItem;
import animon.enums.Rarity;
import animon.kontrak.IUsable;
import animon.model.Hewan;

import java.util.Objects;

/**
 * ABSTRACT CLASS semua item (inventory &amp; ekonomi). Mengimplementasikan {@link IUsable}.
 *
 * <ul>
 *   <li><b>Encapsulation</b>: harga private &amp; final; {@code hargaJual} TIDAK bisa diset dari luar,
 *       selalu dihitung otomatis = 50% {@code hargaBeli}.</li>
 *   <li><b>Abstraction</b>: {@link #gunakan(Hewan)}, {@link #cekPenggunaan(Hewan)},
 *       {@link #getJenis()} dan {@link #getRingkasan()} abstract.</li>
 *   <li><b>Polymorphism</b>: Trainer / BattleEngine cukup memanggil {@code item.gunakan(target)}
 *       tanpa tahu apakah item itu Heal, Buff, atau Evolusi.</li>
 * </ul>
 *
 * Item bersifat immutable, sehingga satu objek aman dipakai bersama (katalog Shop, Gacha, inventory).
 */
public abstract class Item implements IUsable {

    public static final int PERSEN_HARGA_JUAL = 50;

    private final String id;
    private final String nama;
    private final int hargaBeli;
    private final int hargaJual;
    private final String deskripsi;

    protected Item(String id, String nama, int hargaBeli, String deskripsi) {
        this.id = Objects.requireNonNull(id, "id item");
        this.nama = Objects.requireNonNull(nama, "nama item");
        if (hargaBeli < 0) throw new IllegalArgumentException("hargaBeli tidak boleh negatif");
        this.hargaBeli = hargaBeli;
        this.hargaJual = hargaBeli * PERSEN_HARGA_JUAL / 100; // otomatis 50%
        this.deskripsi = deskripsi == null ? "" : deskripsi;
    }

    // =================================================================== abstract

    /** Terapkan efek item ke target. Wajib memanggil {@link #cekPenggunaan(Hewan)} lebih dulu. */
    @Override
    public abstract HasilAksi gunakan(Hewan target);

    @Override
    public abstract String cekPenggunaan(Hewan target);

    public abstract JenisItem getJenis();

    /** Ringkasan efek singkat untuk menu, mis. "HP +50". */
    public abstract String getRingkasan();

    /**
     * Apakah target item ini hewan yang sedang pingsan (mis. Bulu Phoenix).
     * Dipakai UI untuk menyaring daftar target. Default: false.
     */
    public boolean untukHewanPingsan() {
        return false;
    }

    // =================================================================== helper untuk child class

    /** Validasi umum: target harus ada dan tidak pingsan. */
    protected final String cekTargetHidup(Hewan target) {
        if (target == null) return "Pilih hewan target.";
        if (target.isFainted()) return target.getNama() + " sedang pingsan.";
        return null;
    }

    /** Log pembuka standar. */
    protected final HasilAksi awalLog(Hewan target) {
        return HasilAksi.baru().log("Menggunakan " + nama + " pada " + target.getNama() + "!");
    }

    // =================================================================== getter

    public String getId()        { return id; }
    public String getNama()      { return nama; }
    public int getHargaBeli()    { return hargaBeli; }
    public int getHargaJual()    { return hargaJual; }
    public String getDeskripsi() { return deskripsi; }

    public Rarity getRarity() {
        return Rarity.dariHarga(hargaBeli);
    }

    @Override
    public String toString() {
        return nama + " (" + getJenis().getLabel() + ": " + getRingkasan() + ")";
    }
}

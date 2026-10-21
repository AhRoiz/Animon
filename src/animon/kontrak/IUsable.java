package animon.kontrak;

import animon.battle.HasilAksi;
import animon.model.Hewan;

/**
 * Kontrak untuk sesuatu yang bisa "dipakai" pada seekor hewan (item, dsb.).
 *
 * Validasi dipisahkan dari eksekusi: UI dapat memanggil {@link #cekPenggunaan(Hewan)}
 * untuk memberi pesan yang jelas sebelum item benar-benar dikonsumsi.
 */
public interface IUsable {

    /** Terapkan efek pada target. Bila tidak valid, kembalikan {@link HasilAksi#gagal(String)}. */
    HasilAksi gunakan(Hewan target);

    /** @return {@code null} bila boleh dipakai, atau alasan penolakan bila tidak. */
    String cekPenggunaan(Hewan target);

    /** Default method: turunan dari {@link #cekPenggunaan(Hewan)}. */
    default boolean bisaDigunakanPada(Hewan target) {
        return cekPenggunaan(target) == null;
    }
}

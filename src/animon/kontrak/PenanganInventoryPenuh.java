package animon.kontrak;

import animon.item.Item;
import animon.model.Trainer;

import java.util.Objects;

/**
 * Kontrak (callback) yang dipanggil {@link Trainer#tambahItem(Item, PenanganInventoryPenuh)}
 * ketika inventory sudah berisi 6 item. Model {@code Trainer} tidak melakukan I/O sendiri;
 * keputusan "jual / buang / tolak" diserahkan ke implementasi (UI konsol, AI, atau test).
 */
@FunctionalInterface
public interface PenanganInventoryPenuh {

    Keputusan putuskan(Trainer trainer, Item baru);

    /** Keputusan atas inventory yang penuh (immutable). */
    final class Keputusan {

        public enum Aksi {
            /** Jual item di slot tertentu (dapat gold 50% harga), lalu item baru masuk. */
            JUAL,
            /** Buang item di slot tertentu tanpa kompensasi, lalu item baru masuk. */
            BUANG,
            /** Item baru tidak diambil. */
            TOLAK
        }

        private final Aksi aksi;
        private final int slot;

        private Keputusan(Aksi aksi, int slot) {
            this.aksi = Objects.requireNonNull(aksi);
            this.slot = slot;
        }

        public static Keputusan jual(int slot)  { return new Keputusan(Aksi.JUAL, slot); }
        public static Keputusan buang(int slot) { return new Keputusan(Aksi.BUANG, slot); }
        public static Keputusan tolak()         { return new Keputusan(Aksi.TOLAK, -1); }

        public Aksi getAksi() { return aksi; }
        public int getSlot()  { return slot; }
    }
}

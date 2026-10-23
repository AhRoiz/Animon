package animon.sistem;

import animon.item.Item;
import animon.util.Acak;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Pengundi item untuk Drafting (awal game) dan Kill Reward.
 * Peluang muncul berbobot berdasarkan {@link animon.enums.Rarity} (Common 6 : Rare 3 : Epic 1),
 * dan satu kali undian tidak pernah berisi item yang sama dua kali.
 */
public final class Gacha {

    private final List<Item> katalog;

    public Gacha(List<Item> katalog) {
        Objects.requireNonNull(katalog, "katalog");
        if (katalog.isEmpty()) throw new IllegalArgumentException("Katalog gacha kosong");
        this.katalog = new ArrayList<>(katalog);
    }

    /** @return {@code jumlah} item acak berbeda (atau seluruh katalog bila lebih kecil). */
    public List<Item> acakOpsi(int jumlah) {
        List<Item> kolam = new ArrayList<>(katalog);
        List<Item> hasil = new ArrayList<>();
        while (hasil.size() < jumlah && !kolam.isEmpty()) {
            Item terpilih = undiBerbobot(kolam);
            hasil.add(terpilih);
            kolam.remove(terpilih);
        }
        return hasil;
    }

    private static Item undiBerbobot(List<Item> kolam) {
        int total = 0;
        for (Item i : kolam) total += i.getRarity().getBobot();
        int r = Acak.antara(1, total);
        for (Item i : kolam) {
            r -= i.getRarity().getBobot();
            if (r <= 0) return i;
        }
        return kolam.get(kolam.size() - 1);
    }
}

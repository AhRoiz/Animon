package animon.util;

import java.util.List;
import java.util.Random;

/**
 * Sumber angka acak global (bisa di-seed lewat argumen --seed agar pertandingan
 * dapat direplikasi). Dipakai oleh DamageCalculator, Gacha, dan Shop.
 */
public final class Acak {

    private static Random rng = new Random();

    private Acak() {
    }

    public static void set(Random random) {
        rng = random;
    }

    public static Random get() {
        return rng;
    }

    /** @return true dengan peluang {@code persen}% (0..100). */
    public static boolean peluang(int persen) {
        if (persen <= 0) return false;
        if (persen >= 100) return true;
        return rng.nextInt(100) < persen;
    }

    /** Bilangan bulat acak inklusif [min, max]. */
    public static int antara(int min, int max) {
        return min + rng.nextInt(max - min + 1);
    }

    /** Bilangan desimal acak [0, 1). */
    public static double desimal() {
        return rng.nextDouble();
    }

    public static <T> T pilih(List<T> list) {
        return list.get(rng.nextInt(list.size()));
    }
}

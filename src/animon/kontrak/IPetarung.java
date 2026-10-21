package animon.kontrak;

/**
 * Kontrak (Abstraction) untuk semua entitas yang dapat bertarung:
 * menerima damage, dipulihkan, dan bisa pingsan.
 *
 * Diimplementasikan oleh abstract class {@link animon.model.Hewan}. Dengan memisahkan
 * kontrak ini, {@code DamageCalculator} cukup bergantung pada perilaku "petarung",
 * bukan pada detail implementasi kelas hewan tertentu.
 */
public interface IPetarung {

    /**
     * Menerima damage final (sudah melalui kalkulasi). Shield menyerap lebih dulu.
     *
     * @return HP yang benar-benar berkurang.
     */
    int terimaDamage(int damage);

    /** @return HP yang benar-benar bertambah (tidak melebihi maxHp). */
    int pulihkanHp(int jumlah);

    /** @return MP yang benar-benar bertambah (tidak melebihi maxMana). */
    int pulihkanMana(int jumlah);

    /** @return true bila HP sudah habis (hp &lt;= 0). */
    boolean isFainted();
}

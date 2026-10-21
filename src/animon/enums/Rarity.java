package animon.enums;

/**
 * Kelangkaan item untuk sistem Gacha. Diturunkan otomatis dari harga beli,
 * sehingga tidak perlu ditulis di JSON. Bobot = peluang relatif muncul di gacha.
 */
public enum Rarity {
    COMMON("Common", 6),
    RARE("Rare", 3),
    EPIC("Epic", 1);

    private final String label;
    private final int bobot;

    Rarity(String label, int bobot) {
        this.label = label;
        this.bobot = bobot;
    }

    public String getLabel() {
        return label;
    }

    public int getBobot() {
        return bobot;
    }

    public static Rarity dariHarga(int hargaBeli) {
        if (hargaBeli >= 170) return EPIC;
        if (hargaBeli >= 80) return RARE;
        return COMMON;
    }
}

package animon.model;

/**
 * Paket stat dasar (immutable) hasil pembacaan JSON. Dipakai sebagai parameter
 * konstruktor {@link Hewan} agar konstruktor tidak menerima belasan angka lepas.
 */
public final class Stat {

    private final int maxHp;
    private final int maxMana;
    private final int attack;
    private final int defense;
    private final int speed;
    private final int evasion;

    public Stat(int maxHp, int maxMana, int attack, int defense, int speed, int evasion) {
        if (maxHp <= 0) throw new IllegalArgumentException("maxHp harus > 0");
        if (maxMana < 0) throw new IllegalArgumentException("maxMana tidak boleh negatif");
        if (attack <= 0) throw new IllegalArgumentException("attack harus > 0");
        if (defense < 0) throw new IllegalArgumentException("defense tidak boleh negatif");
        if (speed < 0) throw new IllegalArgumentException("speed tidak boleh negatif");
        if (evasion < 0 || evasion > 60) throw new IllegalArgumentException("evasion harus 0..60");
        this.maxHp = maxHp;
        this.maxMana = maxMana;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.evasion = evasion;
    }

    public int getMaxHp()   { return maxHp; }
    public int getMaxMana() { return maxMana; }
    public int getAttack()  { return attack; }
    public int getDefense() { return defense; }
    public int getSpeed()   { return speed; }
    public int getEvasion() { return evasion; }
}

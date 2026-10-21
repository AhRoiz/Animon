package animon.enums;

/** Sasaran sebuah skill. Menentukan apakah engine perlu meminta pemain memilih target. */
public enum TargetSkill {
    /** Hewan aktif milik lawan. */
    MUSUH,
    /** Salah satu hewan di tim sendiri (dipilih pemain). */
    KAWAN,
    /** Diri sendiri. */
    DIRI
}

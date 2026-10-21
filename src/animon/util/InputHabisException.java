package animon.util;

/** Dilempar ketika stdin ditutup (EOF / Ctrl+Z / Ctrl+D) agar game keluar dengan rapi. */
public class InputHabisException extends RuntimeException {
    public InputHabisException() {
        super("Input berakhir");
    }
}

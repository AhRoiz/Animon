package animon.util;

/** Error saat parsing / membaca nilai JSON. Pesan menyertakan posisi baris & kolom. */
public class JsonException extends RuntimeException {
    public JsonException(String pesan) {
        super(pesan);
    }
}

package animon.battle;

import animon.model.Hewan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Hasil satu aksi (skill, serangan, item, switch, defend).
 * Berisi log untuk ditampilkan, status "One More", dan efek tertunda yang harus
 * dimasukkan ke antrian {@link BattleEngine}.
 */
public final class HasilAksi {

    private final List<String> log = new ArrayList<>();
    private final List<EfekTertunda> tertunda = new ArrayList<>();
    private final List<Hewan> korban = new ArrayList<>();
    private boolean oneMore;
    private boolean berhasil = true;
    private String alasanGagal;

    private HasilAksi() {
    }

    public static HasilAksi baru() {
        return new HasilAksi();
    }

    /** Aksi ditolak (mis. MP kurang). Giliran tidak terpakai. */
    public static HasilAksi gagal(String alasan) {
        HasilAksi h = new HasilAksi();
        h.berhasil = false;
        h.alasanGagal = alasan;
        return h;
    }

    public static HasilAksi dariSerangan(HasilSerangan hs) {
        HasilAksi h = new HasilAksi();
        h.log.addAll(hs.getLog());
        h.oneMore = hs.isOneMore();
        return h;
    }

    public HasilAksi log(String baris) {
        log.add(baris);
        return this;
    }

    public HasilAksi logDepan(String baris) {
        log.add(0, baris);
        return this;
    }

    public HasilAksi logSemua(List<String> baris) {
        log.addAll(baris);
        return this;
    }

    public HasilAksi tunda(EfekTertunda efek) {
        tertunda.add(efek);
        return this;
    }

    /** Dicatat oleh {@link BattleEngine} saat hewan lawan pingsan akibat aksi ini (Kill Reward). */
    void tambahKorban(Hewan hewan) {
        korban.add(hewan);
    }

    public boolean isBerhasil()          { return berhasil; }
    public String getAlasanGagal()       { return alasanGagal; }
    public boolean isOneMore()           { return oneMore; }
    public List<String> getLog()         { return Collections.unmodifiableList(log); }
    public List<EfekTertunda> getTertunda() { return Collections.unmodifiableList(tertunda); }
    public List<Hewan> getKorban()       { return Collections.unmodifiableList(korban); }
}

package animon.gui.riwayat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Satu langkah aksi yang terekam untuk diputar ulang pada layar Replay. */
public final class LangkahReplay {

    private final int nomorLangkah;
    private final int ronde;
    private final String pelakuTrainer;
    private final String pelakuHewan;
    private final int hpPelakuSebelum;
    private final int hpPelakuSesudah;
    private final int maxHpPelaku;
    private final String targetTrainer;
    private final String targetHewan;
    private final int hpTargetSebelum;
    private final int hpTargetSesudah;
    private final int maxHpTarget;
    private final String ringkasanAksi;
    private final List<String> log;

    public LangkahReplay(int nomorLangkah, int ronde,
                         String pelakuTrainer, String pelakuHewan,
                         int hpPelakuSebelum, int hpPelakuSesudah, int maxHpPelaku,
                         String targetTrainer, String targetHewan,
                         int hpTargetSebelum, int hpTargetSesudah, int maxHpTarget,
                         String ringkasanAksi, List<String> log) {
        this.nomorLangkah = nomorLangkah;
        this.ronde = ronde;
        this.pelakuTrainer = Objects.requireNonNull(pelakuTrainer);
        this.pelakuHewan = Objects.requireNonNull(pelakuHewan);
        this.hpPelakuSebelum = hpPelakuSebelum;
        this.hpPelakuSesudah = hpPelakuSesudah;
        this.maxHpPelaku = Math.max(1, maxHpPelaku);
        this.targetTrainer = Objects.requireNonNull(targetTrainer);
        this.targetHewan = Objects.requireNonNull(targetHewan);
        this.hpTargetSebelum = hpTargetSebelum;
        this.hpTargetSesudah = hpTargetSesudah;
        this.maxHpTarget = Math.max(1, maxHpTarget);
        this.ringkasanAksi = Objects.requireNonNull(ringkasanAksi);
        this.log = Collections.unmodifiableList(new ArrayList<>(log));
    }

    public int getNomorLangkah()    { return nomorLangkah; }
    public int getRonde()           { return ronde; }
    public String getPelakuTrainer(){ return pelakuTrainer; }
    public String getPelakuHewan()  { return pelakuHewan; }
    public int getHpPelakuSebelum() { return hpPelakuSebelum; }
    public int getHpPelakuSesudah() { return hpPelakuSesudah; }
    public int getMaxHpPelaku()     { return maxHpPelaku; }
    public String getTargetTrainer(){ return targetTrainer; }
    public String getTargetHewan()  { return targetHewan; }
    public int getHpTargetSebelum() { return hpTargetSebelum; }
    public int getHpTargetSesudah() { return hpTargetSesudah; }
    public int getMaxHpTarget()     { return maxHpTarget; }
    public String getRingkasanAksi(){ return ringkasanAksi; }
    public List<String> getLog()    { return log; }
}

